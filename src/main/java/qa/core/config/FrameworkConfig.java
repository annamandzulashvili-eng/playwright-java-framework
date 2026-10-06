package qa.core.config;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable, validated framework configuration.
 * <p>
 * Resolved once per JVM by {@link ConfigLoader} with a single precedence chain:
 * JVM system property (-Dkey) → environment variable (KEY_NAME) → env-&lt;env&gt;.properties → default.properties.
 * Invalid or missing values fail fast at startup with every problem listed at once.
 */
public record FrameworkConfig(
        String env,
        String baseUrl,
        String apiUrl,
        BrowserName browser,
        boolean headless,
        int slowMoMs,
        DeviceProfile device,
        String testIdAttribute,
        int defaultTimeoutMs,
        int navigationTimeoutMs,
        int assertionTimeoutMs,
        ArtifactMode trace,
        ArtifactMode video,
        boolean screenshotOnFailure,
        boolean blockThirdParty,
        int apiTimeoutMs,
        int threads,
        int retryMax,
        Path artifactsDir) {

    public static FrameworkConfig get() {
        return Holder.INSTANCE;
    }

    /** Non-secret values shown in the Allure "Environment" widget. */
    public Map<String, String> describe() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("Environment", env);
        m.put("Base URL", baseUrl);
        m.put("API URL", apiUrl);
        m.put("Browser", browser.name().toLowerCase());
        m.put("Device", device.name().toLowerCase());
        m.put("Headless", String.valueOf(headless));
        m.put("Trace", trace.name().toLowerCase());
        m.put("Video", video.name().toLowerCase());
        m.put("Retry max", String.valueOf(retryMax));
        m.put("Java", System.getProperty("java.version"));
        m.put("OS", System.getProperty("os.name"));
        return m;
    }

    private static final class Holder {
        private static final FrameworkConfig INSTANCE = ConfigLoader.load();
    }
}
