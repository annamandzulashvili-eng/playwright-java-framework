package qa.app.ui.pages;

import com.microsoft.playwright.Page;

import qa.core.browser.Navigation;

public class HomePage extends BasePage {

    public HomePage(Page page) {
        super(page);
    }

    public HomePage open() {
        Navigation.open(page, "/");
        return this;
    }
}
