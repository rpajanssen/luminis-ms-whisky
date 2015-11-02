package luminis.whisky.resources;


import luminis.whisky.command.ServiceResultException;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.domain.OrderReturnRequest;
import luminis.whisky.domain.OrderReturnResponse;
import luminis.whisky.resources.exception.UnableToCancelException;
import luminis.whisky.util.Metrics;
import luminis.whisky.util.Service;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.Response;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ReturnsWithFuturesAndFanOutResourceTest {

    private ConsulServiceUrlFinder consulServiceUrlFinder = mock(ConsulServiceUrlFinder.class);
    private Metrics metrics = mock(Metrics.class);

    private ReturnsWithFuturesAndFanOutResource underTest;

    @Before
    public void setup() throws DyingServiceException {

        when(consulServiceUrlFinder.findServiceUrl(Service.SHIPPING.getServiceID())).then(invocationOnMock -> Arrays.asList("shipping-url"));

        when(consulServiceUrlFinder.findServiceUrl(Service.BILLING.getServiceID())).then(invocationOnMock -> Arrays.asList("billing-url"));

        underTest = new ReturnsWithFuturesAndFanOutResource(consulServiceUrlFinder, metrics) {
            @Override
            <T> Response callService(Service service, String baseUrl, final T payload) throws DyingServiceException, InterruptedException {
                OrderReturnRequest request = (OrderReturnRequest)payload;

                if(Service.SHIPPING.equals(service) && "112".equals(request.getOrderNumber())) {
                    OrderReturnResponse response =  new OrderReturnResponse();
                    response.setOrderNumber("112");
                    response.setState(OrderReturnResponse.STATE_CANCELLED);
                    return Response.status(Response.Status.OK).entity(response).build();
                }

                if(Service.BILLING.equals(service) && "111".equals(request.getOrderNumber())) {
                    OrderReturnResponse response =  new OrderReturnResponse();
                    response.setOrderNumber("111");
                    response.setState(OrderReturnResponse.STATE_CANCELLED);
                    return Response.status(Response.Status.OK).entity(response).build();
                }

                OrderReturnResponse response =  new OrderReturnResponse();
                response.setOrderNumber("42");
                response.setState(OrderReturnResponse.STATE_RETURNED);
                return Response.status(Response.Status.OK).entity(response).build();
            }

            @Override
            void ifCancellationFailed(Service service, Response response) {
                if(Response.Status.OK.getStatusCode()!=response.getStatus()) {
                    throw new ServiceResultException(response.getStatus(), response.readEntity(ErrorMessageResponse.class), service);
                }

                OrderReturnResponse orderReturnResponse = (OrderReturnResponse)response.getEntity();
                if(!"returned".equalsIgnoreCase(orderReturnResponse.getState())) {
                    throw new UnableToCancelException(service, orderReturnResponse);
                }
            }
        };
    }

    @Test
    public void should_cancel_order() throws Throwable {
        OrderReturnRequest request = new OrderReturnRequest();
        request.setOrderNumber("42");

        OrderReturnRequest response = (OrderReturnRequest)underTest.returnOrder(request).getEntity();
        assertEquals("42", response.getOrderNumber());
    }

    @Test(expected = UnableToCancelException.class)
    public void should_fail_to_cancel_shipping_of_order() throws Throwable {
        OrderReturnRequest request = new OrderReturnRequest();
        request.setOrderNumber("111");

        underTest.returnOrder(request).getEntity();
    }

    @Test(expected = UnableToCancelException.class)
    public void should_fail_to_cancel_billing_of_order() throws Throwable {
        OrderReturnRequest request = new OrderReturnRequest();
        request.setOrderNumber("112");

        underTest.returnOrder(request).getEntity();
    }
}
