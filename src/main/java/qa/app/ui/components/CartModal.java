package qa.app.ui.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** "Added!" confirmation dialog shown after adding a product to the cart. */
public class CartModal {

    private final Locator root;

    public CartModal(Page page) {
        this.root = page.locator("#cartModal");
    }

    public Locator root() { return root; }
    public Locator viewCartLink() { return root.locator("a[href='/view_cart']"); }
    public Locator continueShoppingButton() { return root.locator("button.close-modal"); }
}
