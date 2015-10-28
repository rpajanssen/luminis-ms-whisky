package luminis.whisky.command;

import luminis.whisky.util.Service;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class RestGetCommandTest {
    private String baseUri = "http://fake.host";
    private String path = "/fake-path";

    private RestGetCommand underTest;

    @Before
    public void setup() {
        underTest = new RestGetCommand(Service.SHIPPING, baseUri, path) {
            @Override
            protected Response run() throws Exception {
                if("/fake-path-time-out".equals(path)) {
                    Thread.sleep(5000);
                }

                return Response.status(Response.Status.OK).entity("{ \"name\":\"value\"}").build();
            }
        };
    }
    
    @Test
    public void should_receive_response() {
        underTest.execute();
    }

    @Test(expected = UnavailableServiceException.class)
    public void should_timeout() {
        path = "/fake-path-time-out";
        underTest.execute();
    }
}
