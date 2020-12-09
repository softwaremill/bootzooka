package EndpointsConfig;

import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class MailHogEndpoint {
    protected Response lastResponse;

    public Response getLastResponse() {
        return lastResponse;
    }

    public int getLastStatusCode(){
        return lastResponse.statusCode();
    }

    public void getEmailsTo(String email){
        lastResponse = given()
                .queryParam("kind", "to")
                .queryParam("query", email)
                .when()
                .get("http://localhost:18025/api/v2/search?kind=to&query="+email);
    }
}
