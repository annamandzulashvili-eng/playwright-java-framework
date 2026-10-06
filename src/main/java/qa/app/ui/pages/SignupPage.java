package qa.app.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import qa.app.data.UserAccount;

/** "/signup" — account information form shown after the initial sign-up step. */
public class SignupPage extends BasePage {

    public SignupPage(Page page) {
        super(page);
    }

    public Locator titleRadio(UserAccount.Title title) {
        return page.locator(title == UserAccount.Title.MR ? "#id_gender1" : "#id_gender2");
    }

    public Locator createAccountButton() { return page.getByTestId("create-account"); }

    public void fillAccountInformation(UserAccount user) {
        titleRadio(user.title()).check();
        page.getByTestId("password").fill(user.password());
        page.getByTestId("days").selectOption(String.valueOf(user.birthDay()));
        page.getByTestId("months").selectOption(String.valueOf(user.birthMonth()));
        page.getByTestId("years").selectOption(String.valueOf(user.birthYear()));
        page.getByTestId("first_name").fill(user.firstName());
        page.getByTestId("last_name").fill(user.lastName());
        page.getByTestId("company").fill(user.company());
        page.getByTestId("address").fill(user.address1());
        page.getByTestId("address2").fill(user.address2());
        page.getByTestId("country").selectOption(user.country());
        page.getByTestId("state").fill(user.state());
        page.getByTestId("city").fill(user.city());
        page.getByTestId("zipcode").fill(user.zipcode());
        page.getByTestId("mobile_number").fill(user.mobileNumber());
    }

    public void submit() {
        createAccountButton().click();
    }
}
