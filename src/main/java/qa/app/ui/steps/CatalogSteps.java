package qa.app.ui.steps;

import java.util.regex.Pattern;

import com.microsoft.playwright.Page;

import io.qameta.allure.Step;

import qa.app.ui.pages.ProductDetailsPage;
import qa.app.ui.pages.ProductsPage;

public class CatalogSteps {

    private static final Pattern SEARCH_RESULTS_URL = Pattern.compile(".*/products\\?search=.*");

    private final Page page;

    public CatalogSteps(Page page) {
        this.page = page;
    }

    @Step("Search the catalog for '{term}'")
    public ProductsPage search(String term) {
        ProductsPage products = new ProductsPage(page).open();
        products.search(term);
        page.waitForURL(SEARCH_RESULTS_URL);
        return products;
    }

    @Step("Open product details of '{productName}'")
    public ProductDetailsPage openDetails(String productName) {
        ProductsPage products = new ProductsPage(page).open();
        products.openDetails(productName);
        page.waitForURL("**/product_details/*");
        return new ProductDetailsPage(page);
    }
}
