package luminis.whisky.resources;

import luminis.whisky.command.RestPostCommand;
import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.OrderReturnRequest;
import luminis.whisky.domain.OrderReturnResponse;
import luminis.whisky.resources.exception.UnableToCancelException;
import luminis.whisky.util.Service;

import javax.ws.rs.core.Response;

/**
 * Captures the boilerplate of fanning out service calls and waiting for a result.
 */
public abstract class AbstractFanOutResource extends AbstractPingResource {

    protected Response waitForResult(OrderReturnRequest orderReturn, CalculationContext calculationContext) throws Throwable {
        while(!calculationContext.hasCompleted()) {
            Thread.sleep(1);
        }

        if(!calculationContext.withSuccess()) {
            throw calculationContext.getException();
        }

        return Response.status(Response.Status.OK).entity(orderReturn).build();
    }

    <T> Response callService(Service service, String baseUrl, final T payload) throws DyingServiceException, InterruptedException {
        RestPostCommand<T> restPostCommand = new RestPostCommand<>(service, baseUrl, service.getServicePath(), payload);

        return restPostCommand.execute();
    }

    void ifCancellationFailedThrowException(Service service, Response response) {
        OrderReturnResponse orderReturnResponse = response.readEntity(OrderReturnResponse.class);
        if(!"returned".equalsIgnoreCase(orderReturnResponse.getState())) {
            throw new UnableToCancelException(service, orderReturnResponse);
        }
    }

    static class CalculationContext {
        private int billingFanOutCount = 0;
        private int shippingFanOutCount =0;

        private boolean cancelBillingSuccess = false;
        private boolean cancelShippingSuccess = false;

        private Throwable billingException;
        private Throwable shippingException;

        void fanOutShipping() {
            shippingFanOutCount++;
        }

        void fanOutBilling() {
            billingFanOutCount++;
        }

        void signOffShippingFanOut() {
            shippingFanOutCount--;
        }

        void signOffBillingFanOut() {
            billingFanOutCount--;
        }

        void registerShippingSuccess() {
            cancelShippingSuccess = true;
        }

        void registerBillingSuccess() {
            cancelBillingSuccess = true;
        }

        void registerShippingException(Throwable e) {
            shippingException = e;
        }

        void registerBillingException(Throwable e) {
            billingException = e;
        }

        boolean hasCompleted() {
            return cancelBillingSuccess && cancelShippingSuccess ||
                    ( (!cancelBillingSuccess && billingException!=null && billingFanOutCount==0) ||
                            (!cancelShippingSuccess && shippingException!=null && shippingFanOutCount==0)
                    );
        }

        boolean withSuccess() {
            return cancelBillingSuccess && cancelShippingSuccess;
        }

        Throwable getException() {
            return billingException != null ? billingException : shippingException;
        }
    }
}
