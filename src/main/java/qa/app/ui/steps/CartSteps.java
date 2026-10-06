package qa.app.ui.steps;

import java.util.List;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.Locator;

import io.qameta.allure.Step;

import qa.app.ui.components.CartModal;
import qa.app.ui.pages.CartPage;
import qa.app.ui.pages.ProductDetailsPage;
import qa.app.ui.pages.ProductsPage;

public class CartSteps {

    private final Page page;

    public CartSteps(Page page) {
        this.page = page;
    }

    @Step("Add products to the cart: {productNames}")
    public CartSteps addFromCatalog(List<String> productNames) {
        ProductsPage products = new ProductsPage(page).open();
        for (String name : productNames) {
            products.addToCart(name);
            closeModal(products.cartModal());
        }
        return this;
    }

    @Step("Add {quantity} x product #{productId} from its details page")
    public CartSteps addFromDetails(int productId, int quantity) {
        ProductDetailsPage details = new ProductDetailsPage(page).open(productId);
        details.addToCart(quantity);
        closeModal(details.cartModal());
        return this;
    }

    @Step("Open the cart")
    public CartPage openCart() {
        return new CartPage(page).open();
    }

    @Step("Remove '{productName}' from the cart")
    public CartPage remove(String productName) {
        CartPage cart = new CartPage(page).open();
        Locator row = cart.row(productName);
        cart.remove(productName);
        row.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED));
        return cart;
    }

    private static void closeModal(CartModal modal) {
        modal.continueShoppingButton().click();
        modal.root().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }
}
