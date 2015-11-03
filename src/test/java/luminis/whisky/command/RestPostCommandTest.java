package luminis.whisky.command;

import luminis.whisky.domain.OrderReturnRequest;
import luminis.whisky.domain.OrderReturnResponse;
import luminis.whisky.util.Service;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class RestPostCommandTest {
    private static final String BASE_URI = "http://fake.host";
    private static final String PATH = "/fake-path";

    private OrderReturnRequest payload;

    private RestPostCommand<OrderReturnRequest> underTest;

    @Before
    public void setup() {
        payload = new OrderReturnRequest();
        payload.setOrderNumber("42");

        underTest = new RestPostCommand<OrderReturnRequest>(Service.SHIPPING, BASE_URI, PATH, payload) {
            @Override
            protected Response run() throws Exception {
                if("007".equals(payload.getOrderNumber())) {
                    Thread.sleep(5000);
                }

                return Response.status(Response.Status.OK).entity(new OrderReturnResponse(payload).withState(OrderReturnResponse.STATE_RETURNED)).build();
            }
        };
    }

    @Test
    public void should_receive_response() {
        underTest.execute();
    }

    @Test(expected = UnavailableServiceException.class)
    public void should_timeout() {
        payload.setOrderNumber("007");
        underTest.execute();
    }
}
