package qa.tests.ui;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.regex.Pattern;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import org.testng.annotations.Test;

import qa.app.api.models.Product;
import qa.app.base.BaseUiTest;
import qa.app.base.Groups;
import qa.app.ui.pages.ProductDetailsPage;
import qa.app.ui.pages.ProductsPage;

@Epic("Storefront UI")
@Feature("Catalog")
public class CatalogUiTest extends BaseUiTest {

    @Test(groups = {Groups.UI, Groups.SMOKE, Groups.REGRESSION},
            description = "UI search shows exactly the products the search API returns")
    @Severity(SeverityLevel.CRITICAL)
    public void uiSearchMatchesSearchApi() {
        String term = "top";
        List<String> expected = productsApi.search(term).body().products().stream().map(Product::name).toList();

        ProductsPage results = ui().catalog().search(term);

        assertThat(results.gridTitle()).hasText(Pattern.compile("searched products", Pattern.CASE_INSENSITIVE));
        assertThat(results.productCards()).hasCount(expected.size());
        then(results.visibleProductNames()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test(groups = {Groups.UI, Groups.REGRESSION},
            description = "Product details page shows the same name and price as the catalog API")
    public void productDetailsMatchCatalogApi() {
        Product product = productsApi.getAllProducts().body().products().getFirst();

        ProductDetailsPage details = ui().productDetailsPage().open(product.id());

        assertThat(details.name()).hasText(product.name());
        assertThat(details.price()).hasText(product.price());
    }
}
