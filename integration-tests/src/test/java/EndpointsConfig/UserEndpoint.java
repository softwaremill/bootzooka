package EndpointsConfig;

import Objects.User;

import static io.restassured.RestAssured.given;

public class UserEndpoint extends BasicEndpointConfiguration{
    private final String userEndpoint = "user";
    private final String registerEndpoint = userEndpoint+"/register";
    private final String loginEndpoint = userEndpoint+"/login";
    private final String changePasswordEndpoint = userEndpoint+"/changepassword";

    public void userRegister(User user){
        lastResponse = given()
                .contentType(contentType)
                .body(user)
                .when()
                .post(registerEndpoint);
    }

    public void userLogin(String loginOrEmail, String password, int apiKeyValidHours){
        String body =
                "{\"loginOrEmail\": \""+loginOrEmail+"\", " +
                "\"password\": \""+password+"\", " +
                "\"apiKeyValidHours\": \""+apiKeyValidHours+"\"}";
        lastResponse = given()
                .contentType(contentType)
                .body(body)
                .when()
                .post(loginEndpoint);
    }

    public void userChangePassword(String apiKey, String currentPassword, String newPassword){
        String body =
                "{\"currentPassword\": \""+currentPassword+"\"," +
                " \"newPassword\": \""+newPassword+"\"}";
        lastResponse = given()
                .headers("Authorization", "Bearer "+apiKey)
                .contentType(contentType)
                .body(body)
                .when()
                .post(changePasswordEndpoint);
    }

    public void getUser(String apiKey){
        lastResponse = given()
                .headers("Authorization", "Bearer "+apiKey)
                .when()
                .get(userEndpoint);
    }

    public void postUser(String apiKey,String login, String email){
        String body =
                "{\"login\": \""+login+"\"," +
                        " \"email\": \""+email+"\"}";
        lastResponse = given()
                .headers("Authorization", "Bearer "+apiKey)
                .contentType(contentType)
                .body(body)
                .when()
                .post(userEndpoint);
    }

    public String getApiKeyFromResponse() {
        return lastResponse.jsonPath().get("apiKey");
    }
}
