package qa.core.config;

/**
 * Browser emulation profiles.
 * <p>
 * These change viewport, scale, touch and user agent of a desktop browser engine.
 * They are <b>responsive-layout emulation, not real-device coverage</b> — native Android/iOS
 * testing belongs to the separate Appium framework.
 */
public enum DeviceProfile {
    DESKTOP(1920, 1080, 1.0, false, false, null),
    LAPTOP(1366, 768, 1.0, false, false, null),
    TABLET(820, 1180, 2.0, true, true,
            "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"),
    IPHONE_14(390, 844, 3.0, true, true,
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"),
    PIXEL_7(412, 915, 2.625, true, true,
            "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36");

    private final int width;
    private final int height;
    private final double scale;
    private final boolean mobile;
    private final boolean touch;
    private final String userAgent;

    DeviceProfile(int width, int height, double scale, boolean mobile, boolean touch, String userAgent) {
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.mobile = mobile;
        this.touch = touch;
        this.userAgent = userAgent;
    }

    public int width() { return width; }
    public int height() { return height; }
    public double scale() { return scale; }
    public boolean mobile() { return mobile; }
    public boolean touch() { return touch; }
    /** Custom user agent, or {@code null} to keep the engine default. */
    public String userAgent() { return userAgent; }
}
