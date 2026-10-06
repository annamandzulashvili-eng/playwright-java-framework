package qa.app.ui;

import com.microsoft.playwright.Page;

import qa.app.ui.pages.AccountStatusPage;
import qa.app.ui.pages.CartPage;
import qa.app.ui.pages.HomePage;
import qa.app.ui.pages.LoginPage;
import qa.app.ui.pages.ProductDetailsPage;
import qa.app.ui.pages.ProductsPage;
import qa.app.ui.steps.AuthSteps;
import qa.app.ui.steps.CartSteps;
import qa.app.ui.steps.CatalogSteps;

/**
 * Single entry point to the UI layer for one test's page. Tests read like the business:
 * {@code ui.auth().loginAs(user)}, {@code ui.cart().openCart().items()}.
 */
public final class Storefront {

    private final Page page;

    public Storefront(Page page) {
        this.page = page;
    }

    // --- Flows ---
    public AuthSteps auth() { return new AuthSteps(page); }
    public CatalogSteps catalog() { return new CatalogSteps(page); }
    public CartSteps cart() { return new CartSteps(page); }

    // --- Pages ---
    public HomePage home() { return new HomePage(page); }
    public LoginPage loginPage() { return new LoginPage(page); }
    public ProductsPage productsPage() { return new ProductsPage(page); }
    public ProductDetailsPage productDetailsPage() { return new ProductDetailsPage(page); }
    public CartPage cartPage() { return new CartPage(page); }
    public AccountStatusPage accountStatusPage() { return new AccountStatusPage(page); }

    public Page page() { return page; }
}
