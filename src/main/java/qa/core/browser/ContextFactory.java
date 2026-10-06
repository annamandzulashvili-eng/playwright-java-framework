package qa.core.browser;

import java.nio.file.Path;
import java.util.regex.Pattern;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qa.core.config.BrowserName;
import qa.core.config.DeviceProfile;
import qa.core.config.FrameworkConfig;

/** Creates an isolated, fully configured {@link BrowserContext} for one test. */
final class ContextFactory {

    private static final Logger log = LoggerFactory.getLogger(ContextFactory.class);

    /**
     * Ad, analytics and consent-management hosts. Public demo sites inject these; they slow tests
     * down and cause overlay-related flakiness without adding test value.
     */
    private static final Pattern THIRD_PARTY = Pattern.compile(
            "^https?://[^/]*(googlesyndication|doubleclick|googleadservices|google-analytics|googletagmanager"
                    + "|googletagservices|adservice\\.google|fundingchoicesmessages|amazon-adsystem|adnxs|criteo"
                    + "|taboola|outbrain)[^/]*/.*");

    private ContextFactory() {
    }

    static BrowserContext create(Browser browser, FrameworkConfig cfg, Path artifactsDir) {
        DeviceProfile device = cfg.device();
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setBaseURL(cfg.baseUrl())
                .setViewportSize(device.width(), device.height())
                .setDeviceScaleFactor(device.scale())
                .setHasTouch(device.touch())
                .setLocale("en-US");

        if (device.mobile()) {
            if (cfg.browser() == BrowserName.FIREFOX) {
                log.warn("Firefox does not support isMobile emulation; using viewport/touch only for {}", device);
            } else {
                options.setIsMobile(true);
            }
        }
        if (device.userAgent() != null) {
            options.setUserAgent(device.userAgent());
        }
        if (cfg.video().enabled()) {
            options.setRecordVideoDir(artifactsDir.resolve("video"));
        }

        BrowserContext context = browser.newContext(options);
        context.setDefaultTimeout(cfg.defaultTimeoutMs());
        context.setDefaultNavigationTimeout(cfg.navigationTimeoutMs());

        if (cfg.blockThirdParty()) {
            context.route(THIRD_PARTY, route -> route.abort());
        }
        if (cfg.trace().enabled()) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(false));
        }
        return context;
    }
}
