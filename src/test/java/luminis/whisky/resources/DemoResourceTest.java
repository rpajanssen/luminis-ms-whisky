package luminis.whisky.resources;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;
import static org.hamcrest.CoreMatchers.equalTo;

public class DemoResourceTest {

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
        get("?name=IntegrationTest").then().assertThat().content(equalTo("{\"hello\":\"Hi, IntegrationTest!\"}"));
    }

    @Test
    public void should_return_pretty_error_message() {
        get("?name=xess").then().assertThat().content(equalTo("{\"code\":4501,\"description\":\"unexpected exception occurred : Unbelievable name has been rejected! \"}"));
    }
}
