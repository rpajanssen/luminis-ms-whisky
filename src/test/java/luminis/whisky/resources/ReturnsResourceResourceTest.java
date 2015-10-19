package luminis.whisky.resources;


import com.jayway.restassured.RestAssured;
import luminis.whisky.util.Services;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class ReturnsResourceResourceTest {

    @Before
    public void setup() {
        if("local".equalsIgnoreCase(System.getProperty("test-env"))) {
            // Local manual
            RestAssured.baseURI = "http://127.0.0.1";
            RestAssured.port = 8888;
        } else {
            // CI
            RestAssured.baseURI = System.getProperty("instanceUrl");
            //RestAssured.port = 80;
        }
    }

    @Test
    public void should_return_result() {
        given()
                .contentType("application/json")
                .body("{\"orderNumber\":\"55\"}")

        .post(Services.RETURNS.getServicePath())

        .then().assertThat().content(equalTo("{\"orderNumber\":\"55\"}"));
    }

    @Test
    public void should_return_error() {
        given()
                .contentType("application/json")
                .body("{\"orderNumber\":\"666\"}")

        .post(Services.RETURNS.getServicePath())

        .then().assertThat().content(equalTo("{\"code\":404,\"description\":\"No shipments for order 666 found.\"}"));
    }
}
