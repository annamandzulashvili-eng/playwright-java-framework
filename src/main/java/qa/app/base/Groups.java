package qa.app.base;

/** TestNG group names. Constants prevent typos that would silently exclude tests from a suite. */
public final class Groups {

    public static final String SMOKE = "smoke";
    public static final String REGRESSION = "regression";
    public static final String API = "api";
    public static final String UI = "ui";

    private Groups() {
    }
}
