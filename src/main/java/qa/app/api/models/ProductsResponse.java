package qa.app.api.models;

import java.util.List;

public record ProductsResponse(int responseCode, List<Product> products) {

    public ProductsResponse {
        products = products == null ? List.of() : List.copyOf(products);
    }
}
