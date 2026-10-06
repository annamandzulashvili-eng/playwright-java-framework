package qa.app.api.models;

/**
 * Generic service reply. Note: this service returns HTTP 200 for most calls and reports the
 * real outcome in {@code responseCode} inside the body — tests assert on that field.
 */
public record ApiMessage(int responseCode, String message) {
}
