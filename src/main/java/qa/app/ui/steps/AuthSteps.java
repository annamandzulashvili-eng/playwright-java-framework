package qa.app.ui.steps;

import com.microsoft.playwright.Page;

import io.qameta.allure.Param;
import io.qameta.allure.Step;
import io.qameta.allure.model.Parameter;

import qa.app.data.UserAccount;
import qa.app.ui.pages.AccountStatusPage;
import qa.app.ui.pages.HomePage;
import qa.app.ui.pages.LoginPage;
import qa.app.ui.pages.SignupPage;

/**
 * Business-level flows composed from page objects. Each public method is one Allure step,
 * synchronizes on the result of its own action and returns the page the user lands on.
 */
public class AuthSteps {

    private final Page page;

    public AuthSteps(Page page) {
        this.page = page;
    }

    @Step("Log in as {user.email}")
    public HomePage loginAs(UserAccount user) {
        LoginPage login = new LoginPage(page).open();
        login.login(user.email(), user.password());
        login.header().logoutLink().waitFor();
        return new HomePage(page);
    }

    @Step("Attempt login with {email}")
    public LoginPage attemptLogin(String email,
                                  @Param(name = "password", mode = Parameter.Mode.MASKED) String password) {
        LoginPage login = new LoginPage(page).open();
        login.login(email, password);
        return login;
    }

    @Step("Register {user.email} through the UI")
    public AccountStatusPage registerViaUi(UserAccount user) {
        LoginPage login = new LoginPage(page).open();
        login.startSignup(user.name(), user.email());
        SignupPage signup = new SignupPage(page);
        signup.createAccountButton().waitFor();
        signup.fillAccountInformation(user);
        signup.submit();
        return new AccountStatusPage(page);
    }

    @Step("Start sign-up with {email}")
    public LoginPage startSignup(String name, String email) {
        LoginPage login = new LoginPage(page).open();
        login.startSignup(name, email);
        return login;
    }

    @Step("Log out")
    public LoginPage logout() {
        HomePage home = new HomePage(page);
        home.header().logoutLink().click();
        page.waitForURL("**/login");
        return new LoginPage(page);
    }
}
