package qa.core.config;

import java.util.List;

/** Thrown at startup when configuration is missing or invalid; lists every problem at once. */
public class ConfigException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConfigException(List<String> problems) {
        super("Invalid test configuration:\n  - " + String.join("\n  - ", problems));
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
