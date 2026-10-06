package qa.core.api;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.function.Supplier;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import qa.core.config.FrameworkConfig;
import qa.core.http.Overload;
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
     * Sends the request (again, while the service reports overload) and parses the body with Jackson
     * regardless of Content-Type (some services return JSON labelled as text/html).
     * Every attempt goes through the full filter chain, so each one is visible in Allure and the log.
     */
    protected <T> ApiResult<T> toResult(Supplier<Response> call, Class<T> type) {
        Response response = Overload.retry("API call -> " + type.getSimpleName(), call,
                r -> Overload.isOverloaded(r.statusCode(), r.asString()),
                r -> "HTTP " + r.statusCode());
        String raw = response.asString();
        return new ApiResult<>(response.statusCode(), raw, Json.read(raw, type), response.time());
    }
}
