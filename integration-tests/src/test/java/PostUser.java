import Objects.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PostUser extends BaseApiTest {
    private String login = RandomStringUtils.random(6, true, true);
    private String email = login+"@bootzooka.com";
    private String password = RandomStringUtils.random(8, true, true);

    @Test
    public void changeUserEmailWithRegistrationKey() {
        User user = new User(login, email, password);
        String registrationApiKey = registerUser(user);

        String newEmail = "new" + email;
        userEndpoint.postUser(registrationApiKey, login, newEmail);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        user.setEmail(newEmail);

        userEndpoint.userLogin(email, password, 1);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());

        userEndpoint.userLogin(user.getEmail(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
    }

    @Test
    public void changeUserLoginWithLoginKey() {
        User user = new User(login, email, password);
        registerUser(user);
        userEndpoint.userLogin(user.getEmail(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        String loggedUserApiKey = userEndpoint.getApiKeyFromResponse();

        String newLogin = "new" + login;
        userEndpoint.postUser(loggedUserApiKey, newLogin, email);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        user.setLogin(newLogin);

        userEndpoint.userLogin(login, password, 1);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());

        userEndpoint.userLogin(user.getLogin(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
    }

    @Test
    public void changeUserEmailAndLogin() {
        User user = new User(login, email, password);
        String registrationApiKey = registerUser(user);

        String newEmail = "new" + email;
        String newLogin = "new" + login;
        userEndpoint.postUser(registrationApiKey, newLogin, newEmail);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
        user.setEmail(newEmail);
        user.setLogin(newLogin);

        userEndpoint.userLogin(email, password, 1);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());

        userEndpoint.userLogin(login, password, 1);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());

        userEndpoint.userLogin(user.getEmail(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());

        userEndpoint.userLogin(user.getLogin(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
    }

    @Test
    public void cantChangeLoginToRegisteredOne() {
        User user = new User(login, email, password);
        String registrationApiKey = registerUser(user);

        String user2Login = "2"+login;
        String user2Email = "2"+email;
        User user2 = new User(user2Login, user2Email, password);
        registerUser(user2);

        userEndpoint.postUser(registrationApiKey, user2Login, user2Email);
        Assertions.assertEquals(400, userEndpoint.getLastStatusCode());
    }
}
