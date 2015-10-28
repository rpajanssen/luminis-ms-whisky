package luminis.whisky.resources.stubs;

import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.OrderReturnRequest;
import luminis.whisky.domain.OrderReturnResponse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BillingStubResourceTest {
    BillingStubResource underTest;

    @Before
    public void setup() {
        underTest = new BillingStubResource();
    }

    @Test
    public void should_cancel_billing() throws InterruptedException, DyingServiceException {
        OrderReturnRequest request = new OrderReturnRequest();
        request.setOrderNumber("42");

        OrderReturnResponse response = (OrderReturnResponse)underTest.returnOrder(request).getEntity();
        assertEquals("42", response.getOrderNumber());
        assertEquals(OrderReturnResponse.STATE_RETURNED, response.getState());
    }

    @Test
    public void should_fail_to_cancel_billing_of_order() throws InterruptedException, DyingServiceException {
        OrderReturnRequest request = new OrderReturnRequest();
        request.setOrderNumber("111");

        OrderReturnResponse response = (OrderReturnResponse)underTest.returnOrder(request).getEntity();
        assertEquals("111", response.getOrderNumber());
        assertEquals(OrderReturnResponse.STATE_CANCELLED, response.getState());
    }
}
