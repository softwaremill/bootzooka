import EndpointsConfig.MailHogEndpoint;
import EndpointsConfig.PasswordResetEndpoint;
import EndpointsConfig.UserEndpoint;
import Objects.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

public class BaseApiTest {
    protected static UserEndpoint userEndpoint;
    protected static PasswordResetEndpoint passwordResetEndpoint;
    protected static MailHogEndpoint mailHogEndpoint;

    @BeforeAll
    public static void setup(){
        userEndpoint = new UserEndpoint();
        passwordResetEndpoint = new PasswordResetEndpoint();
    }

    protected String registerUser(User user) {
        userEndpoint.userRegister(user);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        return userEndpoint.getApiKeyFromResponse();
    }
}
