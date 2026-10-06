package qa.core.config;

/** Browser engine (and optional branded channel) to launch. */
public enum BrowserName {
    /** Bundled Chromium downloaded by Playwright. */
    CHROMIUM,
    /** Locally installed Google Chrome (channel "chrome"). */
    CHROME,
    /** Locally installed Microsoft Edge (channel "msedge"). */
    MSEDGE,
    FIREFOX,
    WEBKIT;

    public boolean isChromiumBased() {
        return this == CHROMIUM || this == CHROME || this == MSEDGE;
    }

    /** Playwright launch channel, or {@code null} for the bundled engine. */
    public String channel() {
        return switch (this) {
            case CHROME -> "chrome";
            case MSEDGE -> "msedge";
            default -> null;
        };
    }
}
