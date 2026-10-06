package qa.core.api;

import static io.restassured.RestAssured.given;

import java.net.URI;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import qa.core.config.FrameworkConfig;
import qa.core.json.Json;

/**
 * Base for service clients. Subclasses expose one method per endpoint and return typed
 * {@link ApiResult}s; they never assert — assertions live in tests.
 */
public abstract class BaseApiClient {

    private final RequestSpecification spec;

    protected BaseApiClient() {
        FrameworkConfig cfg = FrameworkConfig.get();
        int timeout = cfg.apiTimeoutMs();
        RestAssuredConfig raConfig = RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", timeout)
                .setParam("http.socket.timeout", timeout));

        URI api = URI.create(cfg.apiUrl());
        this.spec = new RequestSpecBuilder()
                .setBaseUri(api.getScheme() + "://" + api.getAuthority())
                .setBasePath(api.getPath() == null ? "" : api.getPath())
                .setConfig(raConfig)
                .addFilter(new AllureRestAssured())
                .addFilter(new Slf4jLoggingFilter())
                .build();
    }

    protected RequestSpecification request() {
        return given().spec(spec);
    }

    /**
     * Parses the body with Jackson regardless of Content-Type
     * (some services return JSON labelled as text/html).
     */
    protected <T> ApiResult<T> toResult(Response response, Class<T> type) {
        String raw = response.asString();
        return new ApiResult<>(response.statusCode(), raw, Json.read(raw, type), response.time());
    }
}
