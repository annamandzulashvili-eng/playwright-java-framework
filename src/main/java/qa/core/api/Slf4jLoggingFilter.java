package qa.core.api;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One concise log line per HTTP call. Full request/response bodies go to Allure via
 * {@code AllureRestAssured}, so console output stays readable in parallel runs.
 */
public class Slf4jLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger("http");

    @Override
    public Response filter(FilterableRequestSpecification request,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        Response response = ctx.next(request, responseSpec);
        log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getURI(), response.statusCode(), response.time());
        return response;
    }
}
