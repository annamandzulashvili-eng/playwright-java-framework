package qa.app.ui.pages;

import com.microsoft.playwright.Page;

import qa.app.ui.components.Header;

/**
 * Page objects own locators and atomic interactions only.
 * No assertions, no business flows (those live in steps), no sleeps.
 */
public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    public Header header() {
        return new Header(page);
    }

    public String url() {
        return page.url();
    }
}
