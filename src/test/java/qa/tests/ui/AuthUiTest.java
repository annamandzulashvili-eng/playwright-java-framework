package qa.tests.ui;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.regex.Pattern;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qase.commons.annotation.QaseId;

import org.testng.annotations.Test;

import qa.app.base.BaseUiTest;
import qa.app.base.Groups;
import qa.app.data.UserAccount;
import qa.app.data.UserFactory;
import qa.app.ui.pages.AccountStatusPage;
import qa.app.ui.pages.HomePage;
import qa.app.ui.pages.LoginPage;

@Epic("Storefront UI")
@Feature("Authentication")
public class AuthUiTest extends BaseUiTest {

    @Test(groups = {Groups.UI, Groups.SMOKE, Groups.REGRESSION},
            description = "Registered user can log in and sees their name in the header")
    @Severity(SeverityLevel.BLOCKER)
    @QaseId(17)
    public void validLoginShowsUserName() {
        UserAccount user = createUserViaApi();

        HomePage home = ui().auth().loginAs(user);

        assertThat(home.header().loggedInAs()).containsText(user.name());
    }

    @Test(groups = {Groups.UI, Groups.REGRESSION},
            description = "Wrong password shows an error and the user stays logged out")
    @QaseId(18)
    public void wrongPasswordShowsError() {
        UserAccount user = createUserViaApi();

        LoginPage login = ui().auth().attemptLogin(user.email(), "wrong-" + user.password());

        assertThat(login.loginError()).hasText("Your email or password is incorrect!");
        assertThat(login.header().logoutLink()).isHidden();
    }

    @Test(groups = {Groups.UI, Groups.REGRESSION},
            description = "New user registers through the UI and the account exists in the backend")
    @Severity(SeverityLevel.CRITICAL)
    @QaseId(14)
    public void registrationThroughUiCreatesAccount() {
        UserAccount user = UserFactory.randomUser();
        deleteAfterTest(user);

        AccountStatusPage status = ui().auth().registerViaUi(user);

        assertThat(status.accountCreatedTitle()).hasText(Pattern.compile("account created!", Pattern.CASE_INSENSITIVE));
        status.continueButton().click();
        assertThat(status.header().loggedInAs()).containsText(user.name());
        // Cross-layer check: what the UI created is real in the backend
        then(usersApi.verifyLogin(user.email(), user.password()).body().responseCode()).isEqualTo(200);
    }

    @Test(groups = {Groups.UI, Groups.REGRESSION},
            description = "Sign-up with an already registered e-mail is rejected")
    @QaseId(16)
    public void signupWithExistingEmailShowsError() {
        UserAccount existing = createUserViaApi();

        LoginPage login = ui().auth().startSignup("Another Name", existing.email());

        assertThat(login.signupError()).hasText("Email Address already exist!");
    }

    @Test(groups = {Groups.UI, Groups.REGRESSION},
            description = "Logout ends the session and returns to the login page")
    @QaseId(12)
    public void logoutReturnsToLoginPage() {
        UserAccount user = createUserViaApi();
        ui().auth().loginAs(user);

        LoginPage login = ui().auth().logout();

        assertThat(page()).hasURL(Pattern.compile(".*/login$"));
        assertThat(login.header().signupLoginLink()).isVisible();
        assertThat(login.header().loggedInAs()).isHidden();
    }
}
