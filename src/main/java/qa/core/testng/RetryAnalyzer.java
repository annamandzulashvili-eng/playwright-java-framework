package qa.core.testng;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import qa.core.config.FrameworkConfig;

/**
 * Opt-in retry, limited by {@code retry.max} (0 by default, so local runs never hide flakiness).
 * Only attach it to idempotent tests: {@code @Test(retryAnalyzer = RetryAnalyzer.class)}.
 * Retried attempts stay visible in Allure as "retries".
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(RetryAnalyzer.class);
    private final AtomicInteger attempts = new AtomicInteger();

    @Override
    public boolean retry(ITestResult result) {
        int max = FrameworkConfig.get().retryMax();
        if (attempts.incrementAndGet() <= max) {
            log.warn("Retrying {} (attempt {}/{}) after: {}", result.getName(), attempts.get(), max,
                    result.getThrowable() == null ? "unknown" : result.getThrowable().getMessage());
            return true;
        }
        return false;
    }
}
