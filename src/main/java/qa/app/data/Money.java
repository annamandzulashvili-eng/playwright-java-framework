package qa.app.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses storefront prices such as {@code "Rs. 1500"}. */
public final class Money {

    private static final Pattern AMOUNT = Pattern.compile("(\\d[\\d,]*)");

    private Money() {
    }

    public static int parse(String text) {
        Matcher m = AMOUNT.matcher(text == null ? "" : text);
        if (!m.find()) {
            throw new IllegalArgumentException("No amount in: '" + text + "'");
        }
        return Integer.parseInt(m.group(1).replace(",", ""));
    }
}
