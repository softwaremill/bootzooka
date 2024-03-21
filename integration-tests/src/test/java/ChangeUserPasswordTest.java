import Objects.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChangeUserPasswordTest extends BaseApiTest {
    private String login = RandomStringUtils.random(6, true, true);
    private String email = login+"@bootzooka.com";
    private String password = RandomStringUtils.random(8, true, true);

    @Test
    public void changeUserPasswordWithRegistrationKey() {
        User user = new User(login, email, password);
        String registrationApiKey = registerUser(user);

        String newPassword = RandomStringUtils.random(8, true, true);;
        userEndpoint.userChangePassword(registrationApiKey, password, newPassword);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        user.setPassword(newPassword);

        userEndpoint.userLogin(user.getLogin(), password, 1);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());

        userEndpoint.userLogin(user.getLogin(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
    }

    @Test
    public void changeUserPasswordWithLoginApiKey() {
        User user = new User(login, email, password);
        registerUser(user);
        userEndpoint.userLogin(user.getLogin(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        String loggedUserApiKey = userEndpoint.getApiKeyFromResponse();

        String newPassword = RandomStringUtils.random(8, true, true);;
        userEndpoint.userChangePassword(loggedUserApiKey, password, newPassword);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        user.setPassword(newPassword);

        userEndpoint.userLogin(user.getLogin(), password, 1);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());

        userEndpoint.userLogin(user.getLogin(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
    }

    @Test
    public void cantChangeUserPasswordWithExpiredKey() {
        User user = new User(login, email, password);
        registerUser(user);
        userEndpoint.userLogin(user.getLogin(), user.getPassword(), 0);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        String loggedUserApiKey = userEndpoint.getApiKeyFromResponse();

        String newPassword = RandomStringUtils.random(8, true, true);;
        userEndpoint.userChangePassword(loggedUserApiKey, password, newPassword);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());
    }

    @Test
    public void cantChangeUserPasswordWithInvalidKey() {
        User user = new User(login, email, password);
        registerUser(user);

        String newPassword = RandomStringUtils.random(8, true, true);;
        userEndpoint.userChangePassword("wrong key", password, newPassword);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());
    }
}
