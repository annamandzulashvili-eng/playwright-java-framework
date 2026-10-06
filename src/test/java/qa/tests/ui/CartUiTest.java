package qa.tests.ui;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qase.commons.annotation.QaseId;

import org.testng.annotations.Test;

import qa.app.api.models.Product;
import qa.app.base.BaseUiTest;
import qa.app.base.Groups;
import qa.app.ui.pages.CartPage.CartItem;
import qa.app.ui.pages.CartPage;

@Epic("Storefront UI")
@Feature("Cart")
public class CartUiTest extends BaseUiTest {

    @Test(groups = {Groups.UI, Groups.SMOKE, Groups.REGRESSION},
            description = "Products added from the catalog appear in the cart with correct prices and totals")
    @Severity(SeverityLevel.BLOCKER)
    @QaseId(9)
    public void addedProductsAppearWithCorrectTotals() {
        List<Product> chosen = productsApi.getAllProducts().body().products().subList(0, 2);
        List<String> names = chosen.stream().map(Product::name).toList();

        ui().cart().addFromCatalog(names);
        List<CartItem> items = ui().cart().openCart().items();

        then(items).extracting(CartItem::name).containsExactlyElementsOf(names);
        for (Product p : chosen) {
            CartItem item = items.stream().filter(i -> i.name().equals(p.name())).findFirst().orElseThrow();
            then(item.price()).as("price of %s", p.name()).isEqualTo(p.priceAmount());
            then(item.quantity()).as("quantity of %s", p.name()).isEqualTo(1);
            then(item.total()).as("total of %s", p.name()).isEqualTo(p.priceAmount());
        }
    }

    @Test(groups = {Groups.UI, Groups.REGRESSION},
            description = "Quantity chosen on the details page is reflected in the cart total")
    @QaseId(10)
    public void quantityFromDetailsIsReflectedInTotal() {
        Product product = productsApi.getAllProducts().body().products().getFirst();
        int quantity = 3;

        ui().cart().addFromDetails(product.id(), quantity);
        List<CartItem> items = ui().cart().openCart().items();

        then(items).singleElement().satisfies(item -> {
            then(item.name()).isEqualTo(product.name());
            then(item.quantity()).isEqualTo(quantity);
            then(item.total()).isEqualTo(quantity * product.priceAmount());
        });
    }

    @Test(groups = {Groups.UI, Groups.REGRESSION},
            description = "Removing the only product leaves the cart empty")
    @QaseId(11)
    public void removingLastProductEmptiesCart() {
        String name = productsApi.getAllProducts().body().products().getFirst().name();
        ui().cart().addFromCatalog(List.of(name));

        CartPage cart = ui().cart().remove(name);

        assertThat(cart.row(name)).hasCount(0);
        assertThat(cart.emptyCartMessage()).isVisible();
    }
}
