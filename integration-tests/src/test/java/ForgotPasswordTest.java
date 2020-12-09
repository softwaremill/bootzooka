import EndpointsConfig.MailHogEndpoint;
import Objects.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPasswordTest extends BaseApiTest {
    private String login = RandomStringUtils.random(6, true, true);
    private String email = login+"@bootzooka.com";
    private String password = RandomStringUtils.random(8, true, true);
    MailHogEndpoint mailHogEndpoint = new MailHogEndpoint();

    @Test
    public void resetForgottenPasswordForLogin() {
        User user = new User(login, email, password);
        registerUser(user);

        passwordResetEndpoint.forgotPassword(login);
        Assertions.assertEquals(200, passwordResetEndpoint.getLastStatusCode());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mailHogEndpoint.getEmailsTo(email);
        Assertions.assertEquals(200, mailHogEndpoint.getLastStatusCode());

        String emailWithCode = mailHogEndpoint.getLastResponse().jsonPath().get("items[0]").toString();
        Matcher m = Pattern.compile("code=(.+)").matcher(emailWithCode);
        m.find();
        String code = m.group(1);

        String newPassword = "new password";
        passwordResetEndpoint.resetPassword(code, newPassword);
        Assertions.assertEquals(200, passwordResetEndpoint.getLastStatusCode());
        user.setPassword(newPassword);

        userEndpoint.userLogin(user.getLogin(), password, 1);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());

        userEndpoint.userLogin(user.getLogin(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
    }

    @Test
    public void resetForgottenPasswordForEmail() {
        User user = new User(login, email, password);
        registerUser(user);

        passwordResetEndpoint.forgotPassword(email);
        Assertions.assertEquals(200, passwordResetEndpoint.getLastStatusCode());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mailHogEndpoint.getEmailsTo(email);
        Assertions.assertEquals(200, mailHogEndpoint.getLastStatusCode());

        String emailWithCode = mailHogEndpoint.getLastResponse().jsonPath().get("items[0]").toString();
        Matcher m = Pattern.compile("code=(.+)").matcher(emailWithCode);
        m.find();
        String code = m.group(1);

        String newPassword = "new password";
        passwordResetEndpoint.resetPassword(code, newPassword);
        Assertions.assertEquals(200, passwordResetEndpoint.getLastStatusCode());
        user.setPassword(newPassword);

        userEndpoint.userLogin(user.getEmail(), password, 1);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());

        userEndpoint.userLogin(user.getEmail(), user.getPassword(), 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());
    }

    @Test
    public void cantResetForgottenPasswordForLoginUsingWrongCode() {
        User user = new User(login, email, password);
        registerUser(user);

        passwordResetEndpoint.forgotPassword(login);
        Assertions.assertEquals(200, passwordResetEndpoint.getLastStatusCode());

        String newPassword = "new password";
        passwordResetEndpoint.resetPassword("wrong code", newPassword);
        Assertions.assertEquals(401, passwordResetEndpoint.getLastStatusCode());
        user.setPassword(newPassword);

        userEndpoint.userLogin(user.getLogin(), password, 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());

        userEndpoint.userLogin(user.getLogin(), user.getPassword(), 1);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());
    }

    @Test
    public void cantResetForgottenPasswordForEmailUsingWrongCode() {
        User user = new User(login, email, password);
        registerUser(user);

        passwordResetEndpoint.forgotPassword(email);
        Assertions.assertEquals(200, passwordResetEndpoint.getLastStatusCode());

        String newPassword = "new password";
        passwordResetEndpoint.resetPassword("wrong code", newPassword);
        Assertions.assertEquals(401, passwordResetEndpoint.getLastStatusCode());
        user.setPassword(newPassword);

        userEndpoint.userLogin(user.getEmail(), password, 1);
        Assertions.assertEquals(200, userEndpoint.getLastStatusCode());

        userEndpoint.userLogin(user.getEmail(), user.getPassword(), 1);
        Assertions.assertEquals(401, userEndpoint.getLastStatusCode());
    }
}
