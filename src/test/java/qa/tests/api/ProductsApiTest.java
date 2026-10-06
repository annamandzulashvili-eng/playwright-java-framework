package qa.tests.api;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import org.hamcrest.MatcherAssert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import qa.app.api.models.ApiMessage;
import qa.app.api.models.Product;
import qa.app.api.models.ProductsResponse;
import qa.core.api.ApiResult;
import qa.tests.base.BaseTest;
import qa.tests.base.Groups;

@Epic("Storefront API")
@Feature("Catalog")
public class ProductsApiTest extends BaseTest {

    @Test(groups = {Groups.API, Groups.SMOKE, Groups.REGRESSION},
            description = "Product list returns a non-empty catalog that matches the contract")
    @Severity(SeverityLevel.BLOCKER)
    public void productListMatchesContract() {
        ApiResult<ProductsResponse> result = productsApi.getAllProducts();

        assertThat(result.httpStatus()).isEqualTo(200);
        assertThat(result.body().responseCode()).isEqualTo(200);
        MatcherAssert.assertThat(result.rawBody(), matchesJsonSchemaInClasspath("schemas/products-list.schema.json"));

        List<Product> products = result.body().products();
        assertThat(products).isNotEmpty();
        assertThat(products).extracting(Product::id).doesNotHaveDuplicates();
        assertThat(products).allSatisfy(p -> {
            assertThat(p.name()).isNotBlank();
            assertThat(p.price()).matches("Rs\\. \\d+");
            assertThat(p.priceAmount()).isPositive();
        });
    }

    @Test(groups = {Groups.API, Groups.REGRESSION},
            description = "POST to the product list is rejected as an unsupported method")
    public void postToProductListIsNotSupported() {
        ApiMessage body = productsApi.postToProductsList().body();

        assertThat(body.responseCode()).isEqualTo(405);
        assertThat(body.message()).isEqualTo("This request method is not supported.");
    }

    @DataProvider(name = "searchTerms", parallel = true)
    public Object[][] searchTerms() {
        return new Object[][] {{"top"}, {"jean"}, {"dress"}, {"tshirt"}};
    }

    @Test(dataProvider = "searchTerms", groups = {Groups.API, Groups.REGRESSION},
            description = "Search returns only products related to the term")
    public void searchReturnsOnlyRelatedProducts(String term) {
        ApiResult<ProductsResponse> result = productsApi.search(term);

        assertThat(result.body().responseCode()).isEqualTo(200);
        assertThat(result.body().products())
                .as("search results for '%s'", term)
                .isNotEmpty()
                .allSatisfy(p -> assertThat(searchableText(p)).contains(term.toLowerCase(Locale.ROOT)));
    }

    @Test(groups = {Groups.API, Groups.REGRESSION},
            description = "Search without the search_product parameter is a bad request")
    public void searchWithoutTermIsBadRequest() {
        ApiMessage body = productsApi.searchWithoutTerm().body();

        assertThat(body.responseCode()).isEqualTo(400);
        assertThat(body.message()).isEqualTo("Bad request, search_product parameter is missing in POST request.");
    }

    /** Search matches name, category and audience; compare against all of them, ignoring "-" and case. */
    private static String searchableText(Product p) {
        String text = p.name() + " " + p.category().category() + " " + p.category().usertype().usertype();
        return text.toLowerCase(Locale.ROOT).replace("-", "");
    }
}
