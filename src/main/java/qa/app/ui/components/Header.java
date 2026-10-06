package qa.app.ui.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Top navigation bar, shared by every page. Scoped to #header so links are unambiguous. */
public class Header {

    private final Locator root;

    public Header(Page page) {
        this.root = page.locator("#header");
    }

    public Locator homeLink() { return root.locator("a[href='/']").first(); }
    public Locator productsLink() { return root.locator("a[href='/products']"); }
    public Locator cartLink() { return root.locator("a[href='/view_cart']"); }
    public Locator signupLoginLink() { return root.locator("a[href='/login']"); }
    public Locator logoutLink() { return root.locator("a[href='/logout']"); }
    public Locator deleteAccountLink() { return root.locator("a[href='/delete_account']"); }
    public Locator loggedInAs() { return root.getByText("Logged in as"); }
}
