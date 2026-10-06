package qa.core.api;

/**
 * Typed response plus the raw body (for schema validation and debugging) and timing.
 *
 * @param httpStatus transport-level status code
 * @param rawBody    body exactly as received
 * @param body       body deserialized into the expected model
 * @param timeMs     response time in milliseconds
 */
public record ApiResult<T>(int httpStatus, String rawBody, T body, long timeMs) {
}
