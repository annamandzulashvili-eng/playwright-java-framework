package qa.core.http;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qa.core.config.FrameworkConfig;

/**
 * Retries a call while the system under test reports that it is overloaded.
 * <p>
 * Public demo sites answer bursts of traffic with 502/503/504 or a "heavy load" page instead of real content.
 * Such a response means the request was not processed, so repeating it is safe for any HTTP method.
 * This is <b>not</b> a test retry: assertions never trigger it, and when the service stays overloaded
 * the call fails with {@link ServiceOverloadedException}, which Allure groups under infrastructure problems.
 * <p>
 * Attempts are limited by {@code overload.retries} (default 2) with a growing pause of 1 s, 3 s, 5 s ...
 */
public final class Overload {

    private static final Logger log = LoggerFactory.getLogger(Overload.class);

    /** Text of the queue-full page served by automationexercise.com under load. */
    static final String HEAVY_LOAD_MARKER = "under heavy load";

    private Overload() {
    }

    public static boolean isOverloaded(int status, String body) {
        return status == 502 || status == 503 || status == 504
                || (body != null && body.contains(HEAVY_LOAD_MARKER));
    }

    /**
     * @param what       short description for logs and the error, e.g. "Open /login"
     * @param call       the action to perform (re-invoked on every attempt)
     * @param overloaded decides whether a result is an overload response
     * @param describe   short description of a result for the error message, e.g. "HTTP 503"
     */
    public static <T> T retry(String what, Supplier<T> call, Predicate<T> overloaded, Function<T, String> describe) {
        int retries = FrameworkConfig.get().overloadRetries();
        for (int attempt = 0; ; attempt++) {
            T result = call.get();
            if (!overloaded.test(result)) {
                if (attempt > 0) {
                    log.info("{}: succeeded on attempt {}", what, attempt + 1);
                }
                return result;
            }
            if (attempt >= retries) {
                throw new ServiceOverloadedException(String.format(
                        "Service overloaded (%s) for '%s' after %d attempt(s)", describe.apply(result), what, attempt + 1));
            }
            long pauseMs = 1_000L * (2L * attempt + 1);
            log.warn("{}: service overloaded ({}), retry {}/{} in {} ms",
                    what, describe.apply(result), attempt + 1, retries, pauseMs);
            pause(pauseMs);
        }
    }

    /** Backoff between attempts against an overloaded server; the only intentional sleep in the framework. */
    private static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceOverloadedException("Interrupted while waiting for an overloaded service");
        }
    }
}
