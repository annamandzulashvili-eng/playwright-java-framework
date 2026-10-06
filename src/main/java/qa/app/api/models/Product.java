package qa.app.api.models;

import qa.app.data.Money;

public record Product(int id, String name, String price, String brand, Category category) {

    public int priceAmount() {
        return Money.parse(price);
    }

    public record Category(UserType usertype, String category) {
    }

    public record UserType(String usertype) {
    }
}
