package qa.tests.base;

import org.testng.annotations.AfterMethod;

import qa.app.api.clients.ProductsApiClient;
import qa.app.api.clients.UserApiClient;
import qa.app.data.UserAccount;
import qa.app.data.UserFactory;
import qa.core.data.Cleanup;

/**
 * Shared by API and UI tests: API clients, test-data helpers and guaranteed cleanup.
 * <p>
 * Clients are stateless and thread-safe, so a single instance per test class is fine even
 * with {@code parallel="methods"}. Anything stateful must be ThreadLocal.
 */
public abstract class BaseTest {

    protected final UserApiClient usersApi = new UserApiClient();
    protected final ProductsApiClient productsApi = new ProductsApiClient();

    /**
     * API-assisted setup: creates a fresh account through the API (fast, no UI) and registers
     * its deletion, so the test only exercises the behaviour it is actually about.
     */
    protected UserAccount createUserViaApi() {
        UserAccount user = UserFactory.randomUser();
        int code = usersApi.create(user).body().responseCode();
        if (code != 201) {
            throw new IllegalStateException("Precondition failed: could not create user via API, responseCode=" + code);
        }
        deleteAfterTest(user);
        return user;
    }

    /** Registers account deletion for a user the test creates by other means (e.g. via UI). */
    protected void deleteAfterTest(UserAccount user) {
        Cleanup.register("delete account " + user.email(), () -> usersApi.delete(user.email(), user.password()));
    }

    @AfterMethod(alwaysRun = true)
    public void runCleanup() {
        Cleanup.runAll();
    }
}
