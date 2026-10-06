package qa.app.ui.pages;

import com.microsoft.playwright.Page;

public class HomePage extends BasePage {

    public HomePage(Page page) {
        super(page);
    }

    public HomePage open() {
        page.navigate("/");
        return this;
    }
}
