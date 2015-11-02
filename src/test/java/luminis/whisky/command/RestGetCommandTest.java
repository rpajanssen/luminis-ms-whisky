package luminis.whisky.command;

import luminis.whisky.util.Service;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class RestGetCommandTest {
    private static final String REST_RESPONSE = "{ \"name\":\"value\"}";
    private static final String BASE_URI = "http://fake.host";
    private static final String FAULTY_PATH = "/fake-path-time-out";

    private String path = "/fake-path";

    private RestGetCommand underTest;

    @Before
    public void setup() {
        underTest = new RestGetCommand(Service.SHIPPING, BASE_URI, path) {

            @Override
            protected Response run() throws Exception {
                if(FAULTY_PATH.equals(path)) {
                    Thread.sleep(5000);
                }

                return Response.status(Response.Status.OK).entity(REST_RESPONSE).build();
            }
        };
    }
    
    @Test
    public void should_receive_response() {
        underTest.execute();
    }

    @Test(expected = UnavailableServiceException.class)
    public void should_timeout() {
        path = FAULTY_PATH;
        underTest.execute();
    }
}
