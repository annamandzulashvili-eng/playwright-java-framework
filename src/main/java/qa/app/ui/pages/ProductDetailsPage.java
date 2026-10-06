package qa.app.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import qa.app.ui.components.CartModal;

/** "/product_details/{id}". */
public class ProductDetailsPage extends BasePage {

    public ProductDetailsPage(Page page) {
        super(page);
    }

    public ProductDetailsPage open(int productId) {
        page.navigate("/product_details/" + productId);
        return this;
    }

    public Locator name() { return page.locator(".product-information h2"); }
    public Locator price() { return page.locator(".product-information span span").first(); }
    public Locator quantity() { return page.locator("#quantity"); }
    public Locator addToCartButton() { return page.locator(".product-information button.cart"); }

    public CartModal cartModal() {
        return new CartModal(page);
    }

    public void addToCart(int qty) {
        quantity().fill(String.valueOf(qty));
        addToCartButton().click();
    }
}
