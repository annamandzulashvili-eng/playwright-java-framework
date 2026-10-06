package qa.app.data;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import net.datafaker.Faker;

/**
 * Generates unique, valid customers. Unique e-mails make tests independent and parallel-safe:
 * no two tests ever share an account.
 */
public final class UserFactory {

    /** Values accepted by the sign-up form's country select. */
    private static final List<String> COUNTRIES =
            List.of("India", "United States", "Canada", "Australia", "Israel", "New Zealand", "Singapore");

    private static final ThreadLocal<Faker> FAKER = ThreadLocal.withInitial(() -> new Faker(Locale.ENGLISH));

    private UserFactory() {
    }

    public static UserAccount randomUser() {
        Faker f = FAKER.get();
        String first = f.name().firstName();
        String last = f.name().lastName();
        String unique = UUID.randomUUID().toString().substring(0, 8);
        return new UserAccount(
                f.bool().bool() ? UserAccount.Title.MR : UserAccount.Title.MRS,
                first + " " + last,
                ("qa." + first + "." + unique + "@example.com").toLowerCase(Locale.ROOT),
                "Pw-" + UUID.randomUUID().toString().substring(0, 12),
                f.number().numberBetween(1, 29),
                f.number().numberBetween(1, 13),
                f.number().numberBetween(1970, 2004),
                first,
                last,
                f.company().name(),
                f.address().streetAddress(),
                f.address().secondaryAddress(),
                COUNTRIES.get(f.number().numberBetween(0, COUNTRIES.size())),
                f.address().state(),
                f.address().city(),
                f.address().zipCode(),
                f.numerify("5##########"));
    }
}
