package qa.app.ui.pages;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import qa.core.browser.Navigation;

import qa.app.data.Money;

/** "/view_cart". */
public class CartPage extends BasePage {

    public CartPage(Page page) {
        super(page);
    }

    public CartPage open() {
        Navigation.open(page, "/view_cart");
        return this;
    }

    public Locator rows() { return page.locator("#cart_info_table tbody tr"); }
    public Locator emptyCartMessage() { return page.locator("#empty_cart"); }

    public Locator row(String productName) {
        return rows().filter(new Locator.FilterOptions().setHas(
                page.locator(".cart_description h4 a").getByText(productName, new Locator.GetByTextOptions().setExact(true))));
    }

    public void remove(String productName) {
        row(productName).locator("a.cart_quantity_delete").click();
    }

    /** Reads the cart table into data, so tests compare values instead of poking at DOM. */
    public List<CartItem> items() {
        List<CartItem> items = new ArrayList<>();
        int count = rows().count();
        for (int i = 0; i < count; i++) {
            Locator r = rows().nth(i);
            items.add(new CartItem(
                    r.locator(".cart_description h4 a").innerText().trim(),
                    Money.parse(r.locator(".cart_price").innerText()),
                    Integer.parseInt(r.locator(".cart_quantity").innerText().trim()),
                    Money.parse(r.locator(".cart_total_price").innerText())));
        }
        return items;
    }

    public record CartItem(String name, int price, int quantity, int total) {
    }
}
