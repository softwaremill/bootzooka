import Objects.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GetUserTest extends BaseApiTest {
    private String login = RandomStringUtils.random(6, true, true);
    private String email = login+"@bootzooka.com";
    private String password = RandomStringUtils.random(8, true, true);

    @Test
    public void getUserUsingRegistrationApiKey() {
        User user = new User(login, email, password);
        String registrationApiKey = registerUser(user);

        userEndpoint.getUser(registrationApiKey);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
    }

    @Test
    public void getUserUsingLoginApiKey() {
        User user = new User(login, email, password);
        String registrationApiKey = registerUser(user);

        userEndpoint.userLogin(user.getLogin(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        String loggedUserApiKey = userEndpoint.getApiKeyFromResponse();

        userEndpoint.getUser(loggedUserApiKey);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
    }

    @Test
    public void cantGetUserWithExpiredApiKey() {
        User user = new User(login, email, password);
        registerUser(user);

        userEndpoint.userLogin(user.getLogin(), user.getPassword(), 0);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        String loggedUserApiKey = userEndpoint.getApiKeyFromResponse();

        userEndpoint.getUser(loggedUserApiKey);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());
    }
}
