package qa.app.api.clients;

import io.qameta.allure.Param;
import io.qameta.allure.Step;
import io.qameta.allure.model.Parameter;
import io.restassured.http.ContentType;

import qa.app.api.models.ApiMessage;
import qa.app.api.models.UserDetailResponse;
import qa.app.data.UserAccount;
import qa.core.api.ApiResult;
import qa.core.api.BaseApiClient;

/**
 * Account endpoints. Also the backbone of API-assisted setup: UI tests create and delete
 * their users here instead of clicking through sign-up every time.
 */
public class UserApiClient extends BaseApiClient {

    @Step("API: create account {user.email}")
    public ApiResult<ApiMessage> create(UserAccount user) {
        return toResult(() -> request()
                .contentType(ContentType.URLENC)
                .formParams(user.toCreateAccountForm())
                .post("/createAccount"), ApiMessage.class);
    }

    @Step("API: delete account {email}")
    public ApiResult<ApiMessage> delete(String email,
                                        @Param(name = "password", mode = Parameter.Mode.MASKED) String password) {
        return toResult(() -> request()
                .contentType(ContentType.URLENC)
                .formParam("email", email)
                .formParam("password", password)
                .delete("/deleteAccount"), ApiMessage.class);
    }

    @Step("API: verify login for {email}")
    public ApiResult<ApiMessage> verifyLogin(String email,
                                             @Param(name = "password", mode = Parameter.Mode.MASKED) String password) {
        return toResult(() -> request()
                .contentType(ContentType.URLENC)
                .formParam("email", email)
                .formParam("password", password)
                .post("/verifyLogin"), ApiMessage.class);
    }

    @Step("API: verify login without e-mail")
    public ApiResult<ApiMessage> verifyLoginWithoutEmail(
            @Param(name = "password", mode = Parameter.Mode.MASKED) String password) {
        return toResult(() -> request()
                .contentType(ContentType.URLENC)
                .formParam("password", password)
                .post("/verifyLogin"), ApiMessage.class);
    }

    @Step("API: get user details for {email}")
    public ApiResult<UserDetailResponse> getByEmail(String email) {
        return toResult(() -> request()
                .queryParam("email", email)
                .get("/getUserDetailByEmail"), UserDetailResponse.class);
    }
}
