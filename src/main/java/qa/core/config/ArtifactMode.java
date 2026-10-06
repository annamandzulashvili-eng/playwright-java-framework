package qa.core.config;

/** When to keep heavy debugging artifacts (trace, video). */
public enum ArtifactMode {
    OFF,
    ON,
    RETAIN_ON_FAILURE;

    public boolean enabled() {
        return this != OFF;
    }

    public boolean keep(boolean failed) {
        return this == ON || (this == RETAIN_ON_FAILURE && failed);
    }
}
