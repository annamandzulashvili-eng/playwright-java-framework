package qa.app.ui.pages;

import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import qa.app.ui.components.CartModal;

/** "/products" — catalog grid and search. */
public class ProductsPage extends BasePage {

    public ProductsPage(Page page) {
        super(page);
    }

    public ProductsPage open() {
        page.navigate("/products");
        return this;
    }

    public Locator searchInput() { return page.locator("#search_product"); }
    public Locator searchButton() { return page.locator("#submit_search"); }
    public Locator gridTitle() { return page.locator(".features_items h2.title"); }
    public Locator productCards() { return page.locator(".features_items .product-image-wrapper"); }

    public Locator card(String productName) {
        return productCards().filter(new Locator.FilterOptions()
                .setHas(page.locator(".productinfo p").getByText(productName, new Locator.GetByTextOptions().setExact(true))));
    }

    public CartModal cartModal() {
        return new CartModal(page);
    }

    public void search(String term) {
        searchInput().fill(term);
        searchButton().click();
    }

    public List<String> visibleProductNames() {
        return page.locator(".features_items .productinfo p").allInnerTexts();
    }

    public void addToCart(String productName) {
        card(productName).locator(".productinfo a.add-to-cart").click();
    }

    public void openDetails(String productName) {
        card(productName).locator("a[href^='/product_details/']").click();
    }
}
