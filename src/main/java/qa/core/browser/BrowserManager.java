package qa.core.browser;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qa.core.config.FrameworkConfig;

/**
 * One {@link Playwright} + {@link Browser} per worker thread, reused across tests on that thread.
 * <p>
 * Playwright Java objects are not thread-safe, so every thread owns its own instance.
 * Launching a browser is the expensive part; isolation between tests comes from a fresh
 * {@code BrowserContext} per test (see {@link UiSession}), which is cheap.
 */
public final class BrowserManager {

    private static final Logger log = LoggerFactory.getLogger(BrowserManager.class);
    private static final ThreadLocal<WorkerBrowser> CURRENT = new ThreadLocal<>();
    private static final Queue<WorkerBrowser> ALL = new ConcurrentLinkedQueue<>();
    /** Serializes Playwright.create(): the first call may download browsers and must not race. */
    private static final Object CREATE_LOCK = new Object();

    static {
        PlaywrightAssertions.setDefaultAssertionTimeout(FrameworkConfig.get().assertionTimeoutMs());
    }

    private BrowserManager() {
    }

    public static Browser browser() {
        WorkerBrowser current = CURRENT.get();
        if (current == null || !current.browser().isConnected()) {
            current = launch(FrameworkConfig.get());
            CURRENT.set(current);
            ALL.add(current);
        }
        return current.browser();
    }

    /** Closes every browser launched by any worker. Call once, after all tests finished. */
    public static void closeAll() {
        WorkerBrowser wb;
        while ((wb = ALL.poll()) != null) {
            wb.close();
        }
        CURRENT.remove();
    }

    private static WorkerBrowser launch(FrameworkConfig cfg) {
        Playwright playwright;
        synchronized (CREATE_LOCK) {
            playwright = Playwright.create();
        }
        playwright.selectors().setTestIdAttribute(cfg.testIdAttribute());

        BrowserType type = switch (cfg.browser()) {
            case CHROMIUM, CHROME, MSEDGE -> playwright.chromium();
            case FIREFOX -> playwright.firefox();
            case WEBKIT -> playwright.webkit();
        };

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(cfg.headless())
                .setSlowMo(cfg.slowMoMs());
        if (cfg.browser().channel() != null) {
            options.setChannel(cfg.browser().channel());
        }

        Browser browser = type.launch(options);
        log.info("Launched {} {} on thread {}", cfg.browser(), browser.version(), Thread.currentThread().getName());
        return new WorkerBrowser(playwright, browser);
    }

    private record WorkerBrowser(Playwright playwright, Browser browser) {
        void close() {
            try {
                browser.close();
            } catch (RuntimeException e) {
                log.warn("Browser close failed: {}", e.getMessage());
            } finally {
                playwright.close();
            }
        }
    }
}
