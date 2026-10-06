package qa.app.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Confirmation pages: "Account Created!" and "Account Deleted!". */
public class AccountStatusPage extends BasePage {

    public AccountStatusPage(Page page) {
        super(page);
    }

    public Locator accountCreatedTitle() { return page.getByTestId("account-created"); }
    public Locator accountDeletedTitle() { return page.getByTestId("account-deleted"); }
    public Locator continueButton() { return page.getByTestId("continue-button"); }
}
