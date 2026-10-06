package qa.core.browser;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import qa.core.http.Overload;

/** Page navigation that reloads when the site answers with an overload page instead of content. */
public final class Navigation {

    private Navigation() {
    }

    /** Navigates to {@code path} (relative to the context base URL) and returns the main response. */
    public static Response open(Page page, String path) {
        return Overload.retry("Open " + path,
                () -> page.navigate(path),
                response -> Overload.isOverloaded(response == null ? 200 : response.status(), page.content()),
                response -> response == null ? "no response" : "HTTP " + response.status());
    }
}
