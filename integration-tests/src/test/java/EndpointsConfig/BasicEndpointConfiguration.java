package EndpointsConfig;

import io.restassured.response.Response;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.*;

public abstract class BasicEndpointConfiguration {
    private static String url = "http://localhost/api/v1/";
    private static String basePath = "api/v1";
    private static String uri = "http://localhost";
    private static int port = 3000;
    protected final String contentType = "application/json";
    protected Response lastResponse;

    public Response getLastResponse() {
        return lastResponse;
    }

    public int getLastStatusCode(){
        return lastResponse.statusCode();
    }

    public BasicEndpointConfiguration() {
        baseURI = uri;
        RestAssured.port = port;
        RestAssured.basePath = basePath;
    }

}
