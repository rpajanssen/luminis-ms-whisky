package luminis.whisky.resources;


import com.jayway.restassured.RestAssured;
import luminis.whisky.util.Service;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class ReturnsResourceIntegrationTest {

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

        .post(Service.RETURNS.getServicePath())

        .then().assertThat().content(equalTo("{\"orderNumber\":\"55\"}"));
    }

    @Test
    public void should_return_404() {
        given()
                .contentType("application/json")
                .body("{\"orderNumber\":\"666\"}")

        .post(Service.RETURNS.getServicePath())

        .then().assertThat().content(equalTo("{\"code\":404,\"description\":\"No shipments for order 666 found.\"}"));
    }

    @Test
    public void should_return_billing_unavailable() {
        given()
                .contentType("application/json")
                .body("{\"orderNumber\":\"007\"}")

                .post(Service.RETURNS.getServicePath())

                .then().assertThat().content(equalTo(" {\"code\":4501,\"description\":\"unexpected exception occurred : RestPostCommand timed-out and no fallback available. \"}"));
    }

    @Test
    public void should_return_shipping_unavailable() {
        given()
                .contentType("application/json")
                .body("{\"orderNumber\":\"006\"}")

                .post(Service.RETURNS.getServicePath())

                .then().assertThat().content(equalTo(" {\"code\":4501,\"description\":\"unexpected exception occurred : RestPostCommand timed-out and no fallback available. \"}"));
    }

    @Test
    public void should_return_cancellation_with_incorrect_state() {
        given()
                .contentType("application/json")
                .body("{\"orderNumber\":\"111\"}")

                .post(Service.RETURNS.getServicePath())

                .then().assertThat().content(equalTo("{\"code\":4001,\"description\":\"unable to cancel billing for order 111, resulting state was a-horrible-state\"}"));
    }
}
