package qa.app.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** "/login" — holds both the login form and the new-user sign-up form. */
public class LoginPage extends BasePage {

    public LoginPage(Page page) {
        super(page);
    }

    public LoginPage open() {
        page.navigate("/login");
        return this;
    }

    // --- Login form ---
    public Locator loginEmail() { return page.getByTestId("login-email"); }
    public Locator loginPassword() { return page.getByTestId("login-password"); }
    public Locator loginButton() { return page.getByTestId("login-button"); }
    public Locator loginError() { return page.locator(".login-form p"); }

    // --- Sign-up form ---
    public Locator signupName() { return page.getByTestId("signup-name"); }
    public Locator signupEmail() { return page.getByTestId("signup-email"); }
    public Locator signupButton() { return page.getByTestId("signup-button"); }
    public Locator signupError() { return page.locator(".signup-form p"); }

    public void login(String email, String password) {
        loginEmail().fill(email);
        loginPassword().fill(password);
        loginButton().click();
    }

    public void startSignup(String name, String email) {
        signupName().fill(name);
        signupEmail().fill(email);
        signupButton().click();
    }
}
