package qa.app.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A storefront customer. Single source of truth for UI sign-up forms and the createAccount API,
 * so both layers always use identical data.
 */
public record UserAccount(
        Title title,
        String name,
        String email,
        String password,
        int birthDay,
        int birthMonth,
        int birthYear,
        String firstName,
        String lastName,
        String company,
        String address1,
        String address2,
        String country,
        String state,
        String city,
        String zipcode,
        String mobileNumber) {

    public enum Title {
        MR("Mr"), MRS("Mrs");

        private final String label;

        Title(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public UserAccount withPassword(String newPassword) {
        return new UserAccount(title, name, email, newPassword, birthDay, birthMonth, birthYear, firstName,
                lastName, company, address1, address2, country, state, city, zipcode, mobileNumber);
    }

    /** Form fields expected by {@code POST /api/createAccount}. */
    public Map<String, String> toCreateAccountForm() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("name", name);
        form.put("email", email);
        form.put("password", password);
        form.put("title", title.label());
        form.put("birth_date", String.valueOf(birthDay));
        form.put("birth_month", String.valueOf(birthMonth));
        form.put("birth_year", String.valueOf(birthYear));
        form.put("firstname", firstName);
        form.put("lastname", lastName);
        form.put("company", company);
        form.put("address1", address1);
        form.put("address2", address2);
        form.put("country", country);
        form.put("zipcode", zipcode);
        form.put("state", state);
        form.put("city", city);
        form.put("mobile_number", mobileNumber);
        return form;
    }

    /** Never print passwords in logs or reports. */
    @Override
    public String toString() {
        return "UserAccount[name=" + name + ", email=" + email + "]";
    }
}
