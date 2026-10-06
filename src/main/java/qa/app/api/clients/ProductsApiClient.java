package qa.app.api.clients;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;

import qa.app.api.models.ApiMessage;
import qa.app.api.models.ProductsResponse;
import qa.core.api.ApiResult;
import qa.core.api.BaseApiClient;

/** Catalog endpoints. One method per endpoint/variant; no assertions here. */
public class ProductsApiClient extends BaseApiClient {

    @Step("API: get all products")
    public ApiResult<ProductsResponse> getAllProducts() {
        return toResult(request().get("/productsList"), ProductsResponse.class);
    }

    @Step("API: POST to products list (unsupported method)")
    public ApiResult<ApiMessage> postToProductsList() {
        return toResult(request().post("/productsList"), ApiMessage.class);
    }

    @Step("API: search products by '{term}'")
    public ApiResult<ProductsResponse> search(String term) {
        return toResult(request()
                .contentType(ContentType.URLENC)
                .formParam("search_product", term)
                .post("/searchProduct"), ProductsResponse.class);
    }

    @Step("API: search products without the search parameter")
    public ApiResult<ApiMessage> searchWithoutTerm() {
        return toResult(request().post("/searchProduct"), ApiMessage.class);
    }
}
