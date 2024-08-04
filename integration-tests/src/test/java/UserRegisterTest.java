import Objects.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserRegisterTest extends BaseApiTest {
    private String login = RandomStringUtils.random(10, true, true);
    private String email = login+"@bootzooka.com";
    private String password = "testing";

    @Test
    public void userRegister() {
        User user = new User(login, email, password);
        userEndpoint.userRegister(user);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());

        String registrationApiKey = userEndpoint.getApiKeyFromResponse();

        userEndpoint.getUser(registrationApiKey);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
    }

    @Test
    public void emailCantRegisterTwice() {
        User user = new User(login, email, password);
        userEndpoint.userRegister(user);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());

        String user2Email = 2+email;
        User user2 = new User(login, user2Email, password);
        userEndpoint.userRegister(user2);
        Assertions.assertEquals(400, userEndpoint.getLastStatusCode());
    }

    @Test
    public void loginCantRegisterTwice() {
        User user = new User(login, email, password);
        userEndpoint.userRegister(user);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());

        String user2Login = 2+login;
        User user2 = new User(user2Login, email, password);
        userEndpoint.userRegister(user2);
        Assertions.assertEquals(400, userEndpoint.getLastStatusCode());
    }

    @Test
    public void tooShortLoginCantRegister() {
        User user = new User("1", email, password);
        userEndpoint.userRegister(user);
        Assertions.assertEquals(400, userEndpoint.getLastStatusCode());
    }

    @Test
    public void invalidEmailCantRegister() {
        User user = new User(login, "invalid email", password);
        userEndpoint.userRegister(user);
        Assertions.assertEquals(400, userEndpoint.getLastStatusCode());
    }

    @Test
    public void CantRegisterWithEmptyLogin() {
        User user = new User("", email, password);
        userEndpoint.userRegister(user);
        Assertions.assertEquals(400, userEndpoint.getLastStatusCode());
    }

    @Test
    public void CantRegisterWithEmptyEmail() {
        User user = new User(login, "", password);
        userEndpoint.userRegister(user);
        Assertions.assertEquals(400, userEndpoint.getLastStatusCode());
    }

    @Test
    public void CantRegisterWithEmptyPassword() {
        User user = new User(login, email, "");
        userEndpoint.userRegister(user);
        Assertions.assertEquals(400, userEndpoint.getLastStatusCode());
    }
}
