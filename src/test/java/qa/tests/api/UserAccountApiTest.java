package qa.tests.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import org.testng.annotations.Test;

import qa.app.api.models.ApiMessage;
import qa.app.api.models.UserDetailResponse.UserDetail;
import qa.app.data.UserAccount;
import qa.app.data.UserFactory;
import qa.tests.base.BaseTest;
import qa.tests.base.Groups;

@Epic("Storefront API")
@Feature("User accounts")
public class UserAccountApiTest extends BaseTest {

    @Test(groups = {Groups.API, Groups.SMOKE, Groups.REGRESSION},
            description = "Account lifecycle: create, verify login, read details, delete")
    @Severity(SeverityLevel.CRITICAL)
    public void accountLifecycle() {
        UserAccount user = UserFactory.randomUser();
        deleteAfterTest(user); // safety net if an assertion below fails mid-way

        Allure.step("Account is created", () -> {
            ApiMessage created = usersApi.create(user).body();
            assertThat(created.responseCode()).isEqualTo(201);
            assertThat(created.message()).isEqualTo("User created!");
        });

        Allure.step("Credentials are accepted", () -> {
            assertThat(usersApi.verifyLogin(user.email(), user.password()).body())
                    .isEqualTo(new ApiMessage(200, "User exists!"));
        });

        Allure.step("Stored details match what was submitted", () -> {
            UserDetail details = usersApi.getByEmail(user.email()).body().user();
            assertThat(details.email()).isEqualTo(user.email());
            assertThat(details.name()).isEqualTo(user.name());
            assertThat(details.firstName()).isEqualTo(user.firstName());
            assertThat(details.lastName()).isEqualTo(user.lastName());
            assertThat(details.city()).isEqualTo(user.city());
            assertThat(details.country()).isEqualTo(user.country());
        });

        Allure.step("Account is deleted and can no longer log in", () -> {
            assertThat(usersApi.delete(user.email(), user.password()).body())
                    .isEqualTo(new ApiMessage(200, "Account deleted!"));
            assertThat(usersApi.verifyLogin(user.email(), user.password()).body())
                    .isEqualTo(new ApiMessage(404, "User not found!"));
        });
    }

    @Test(groups = {Groups.API, Groups.REGRESSION},
            description = "Creating an account with an existing e-mail is rejected")
    public void duplicateEmailIsRejected() {
        UserAccount existing = createUserViaApi();

        ApiMessage body = usersApi.create(existing).body();

        assertThat(body.responseCode()).isEqualTo(400);
        assertThat(body.message()).isEqualTo("Email already exists!");
    }

    @Test(groups = {Groups.API, Groups.REGRESSION},
            description = "Login verification with a wrong password returns 'not found'")
    public void wrongPasswordIsRejected() {
        UserAccount user = createUserViaApi();

        ApiMessage body = usersApi.verifyLogin(user.email(), "wrong-" + user.password()).body();

        assertThat(body).isEqualTo(new ApiMessage(404, "User not found!"));
    }

    @Test(groups = {Groups.API, Groups.REGRESSION},
            description = "Login verification without e-mail is a bad request")
    public void loginVerificationWithoutEmailIsBadRequest() {
        ApiMessage body = usersApi.verifyLoginWithoutEmail("any-password").body();

        assertThat(body.responseCode()).isEqualTo(400);
        assertThat(body.message()).isEqualTo("Bad request, email or password parameter is missing in POST request.");
    }
}
