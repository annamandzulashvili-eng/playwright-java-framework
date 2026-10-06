package qa.core.http;

/** The system under test stayed overloaded after all retries: an environment problem, not a product defect. */
public class ServiceOverloadedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ServiceOverloadedException(String message) {
        super(message);
    }
}
