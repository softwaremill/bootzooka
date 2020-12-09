package EndpointsConfig;

import static io.restassured.RestAssured.given;

public class PasswordResetEndpoint extends BasicEndpointConfiguration{
    private final String passwordResetEndpoint = "passwordreset";
    private final String resetEndpoint = passwordResetEndpoint+"/reset";
    private final String forgotEndpoint = passwordResetEndpoint+"/forgot";

    public void resetPassword(String code, String newPassword){
        String body =
                "{\"code\": \""+code+"\", " +
                "\"password\": \""+newPassword+"\"}";
        lastResponse = given()
                .contentType(contentType)
                .body(body)
                .when()
                .post(resetEndpoint);
    }

    public void forgotPassword(String loginOrEmail){
        String body = "{\"loginOrEmail\": \""+loginOrEmail+"\"}";
        lastResponse = given()
                .contentType(contentType)
                .body(body)
                .when()
                .post(forgotEndpoint);
    }
}
