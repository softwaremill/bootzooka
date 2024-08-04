import Objects.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserLoginTest extends BaseApiTest{
    private String login = RandomStringUtils.random(6, true, true);
    private String email = login+"@bootzooka.com";
    private String password = RandomStringUtils.random(8, true, true);

    @Test
    public void loginRegisteredUserWithLogin() {
        User user = new User(login, email, password);
        registerUser(user);

        userEndpoint.userLogin(user.getLogin(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        String loggedUserApiKey = userEndpoint.getApiKeyFromResponse();

        userEndpoint.getUser(loggedUserApiKey);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
    }

    @Test
    public void loginRegisteredUserWithEmail() {
        User user = new User(login, email, password);
        registerUser(user);

        userEndpoint.userLogin(user.getEmail(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        String loggedUserApiKey = userEndpoint.getApiKeyFromResponse();

        userEndpoint.getUser(loggedUserApiKey);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
    }

    @Test
    public void cantLoginWithInvalidPassword() {
        User user = new User(login, email, password);
        registerUser(user);

        userEndpoint.userLogin(user.getEmail(), "invalid password", 1);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());
    }

    @Test
    public void cantLoginUnknownUser() {
        User user = new User(login, email, password);

        userEndpoint.userLogin(user.getEmail(), "invalid password", 1);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());
    }

    @Test
    public void cantLoginWithExpiredKey() {

    }
}
