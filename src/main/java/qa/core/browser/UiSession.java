package qa.core.browser;

import java.nio.file.Files;
import java.nio.file.Path;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.Video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qa.core.config.FrameworkConfig;
import qa.core.reporting.Attachments;

/**
 * Browser state for exactly one test: its own context (cookies, storage, cart) and page.
 * Owns evidence collection on finish: screenshot, Playwright trace and video, kept per config.
 */
public final class UiSession {

    private static final Logger log = LoggerFactory.getLogger(UiSession.class);

    private final FrameworkConfig cfg;
    private final Path artifactsDir;
    private final BrowserContext context;
    private final Page page;

    private UiSession(FrameworkConfig cfg, Path artifactsDir, BrowserContext context, Page page) {
        this.cfg = cfg;
        this.artifactsDir = artifactsDir;
        this.context = context;
        this.page = page;
    }

    public static UiSession start(String testName) {
        FrameworkConfig cfg = FrameworkConfig.get();
        Path dir = cfg.artifactsDir().resolve(safe(testName) + "-" + System.nanoTime());
        BrowserContext context = ContextFactory.create(BrowserManager.browser(), cfg, dir);
        Page page = context.newPage();
        return new UiSession(cfg, dir, context, page);
    }

    public Page page() {
        return page;
    }

    public BrowserContext context() {
        return context;
    }

    /** Collects evidence and closes the context. Never throws: teardown must not mask the test result. */
    public void finish(boolean failed) {
        try {
            if (failed && cfg.screenshotOnFailure() && !page.isClosed()) {
                Attachments.png("Screenshot on failure", page.screenshot(new Page.ScreenshotOptions().setFullPage(true)));
                Attachments.text("URL on failure", page.url());
            }
            stopTracing(failed);
        } catch (RuntimeException e) {
            log.warn("Evidence collection failed: {}", e.getMessage());
        }

        Video video = page.video();
        closeQuietly();
        handleVideo(video, failed);
    }

    private void stopTracing(boolean failed) {
        if (!cfg.trace().enabled()) {
            return;
        }
        if (cfg.trace().keep(failed)) {
            createDirs();
            Path trace = artifactsDir.resolve("trace.zip");
            context.tracing().stop(new Tracing.StopOptions().setPath(trace));
            Attachments.file("Playwright trace (open with: npx playwright show-trace)", "application/zip", trace, "zip");
        } else {
            context.tracing().stop();
        }
    }

    private void handleVideo(Video video, boolean failed) {
        if (video == null) {
            return;
        }
        try {
            if (cfg.video().keep(failed)) {
                Path path = video.path();
                if (Files.exists(path)) {
                    Attachments.file("Video", "video/webm", path, "webm");
                }
            } else {
                video.delete();
            }
        } catch (RuntimeException e) {
            log.warn("Video handling failed: {}", e.getMessage());
        }
    }

    private void closeQuietly() {
        try {
            context.close();
        } catch (RuntimeException e) {
            log.warn("Context close failed: {}", e.getMessage());
        }
    }

    private void createDirs() {
        try {
            Files.createDirectories(artifactsDir);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    private static String safe(String name) {
        return name.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
