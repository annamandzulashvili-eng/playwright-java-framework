package qa.core.data;

import java.util.ArrayDeque;
import java.util.Deque;

import io.qameta.allure.Allure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Per-test cleanup registry.
 * <p>
 * Register the undo action right after creating test data; the base test runs all actions
 * in reverse order (LIFO) after the test, whether it passed or failed. One failing cleanup
 * does not stop the others.
 * <pre>{@code
 * users.create(user);
 * Cleanup.register("Delete user " + user.email(), () -> users.delete(user.email(), user.password()));
 * }</pre>
 */
public final class Cleanup {

    private static final Logger log = LoggerFactory.getLogger(Cleanup.class);
    private static final ThreadLocal<Deque<Action>> ACTIONS = ThreadLocal.withInitial(ArrayDeque::new);

    private Cleanup() {
    }

    public static void register(String description, Runnable action) {
        ACTIONS.get().push(new Action(description, action));
    }

    public static void runAll() {
        Deque<Action> actions = ACTIONS.get();
        try {
            while (!actions.isEmpty()) {
                Action a = actions.pop();
                try {
                    Allure.step("Cleanup: " + a.description(), a.action()::run);
                } catch (Exception e) { // Allure.step can rethrow checked exceptions sneakily
                    log.warn("Cleanup '{}' failed: {}", a.description(), e.getMessage());
                }
            }
        } finally {
            ACTIONS.remove();
        }
    }

    private record Action(String description, Runnable action) {
    }
}
