package qa.tests.base;

import java.lang.reflect.Method;

import com.microsoft.playwright.Page;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import qa.app.ui.Storefront;
import qa.core.browser.UiSession;

/**
 * Gives every test method its own browser context and page.
 * <p>
 * TestNG shares one test-class instance across threads in {@code parallel="methods"}, so the
 * session is ThreadLocal, never an instance field. Order of teardown: evidence and context
 * close first, then data cleanup from {@link BaseTest}.
 */
public abstract class BaseUiTest extends BaseTest {

    private static final ThreadLocal<UiSession> SESSION = new ThreadLocal<>();

    @BeforeMethod(alwaysRun = true)
    public void openBrowserContext(Method method) {
        SESSION.set(UiSession.start(method.getDeclaringClass().getSimpleName() + "." + method.getName()));
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowserContext(ITestResult result) {
        UiSession session = SESSION.get();
        if (session != null) {
            session.finish(!result.isSuccess());
            SESSION.remove();
        }
    }

    protected Storefront ui() {
        return new Storefront(page());
    }

    protected Page page() {
        UiSession session = SESSION.get();
        if (session == null) {
            throw new IllegalStateException("No UI session on this thread; did @BeforeMethod run?");
        }
        return session.page();
    }
}
