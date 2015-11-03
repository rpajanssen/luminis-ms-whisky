package luminis.whisky.resources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import luminis.whisky.command.RestPostCommand;
import luminis.whisky.command.ServiceResultException;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.domain.OrderReturnRequest;
import luminis.whisky.domain.OrderReturnResponse;
import luminis.whisky.resources.exception.UnableToCancelException;
import luminis.whisky.util.Metrics;
import luminis.whisky.util.Service;
import rx.Observable;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// todo : transaction rollback on failure
@Path("/returns")
@Api(value="Order returns - concurrent, no fan-out, rxjava", description = "Returns the order and cancels shipping" +
        " and billing. It executes the cancellation of the shipping and the billing concurrently using observables." +
        " No fan out.")
public class ReturnsWithObservableResource extends AbstractPingResource {
    private final ConsulServiceUrlFinder consulServiceUrlFinder;
    private final Metrics metrics;

    public ReturnsWithObservableResource(ConsulServiceUrlFinder consulServiceUrlFinder, Metrics metrics) {
        this.consulServiceUrlFinder = consulServiceUrlFinder;
        this.metrics = metrics;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Return order",
            notes = "Cancels an order. Cancels the shipment and billing of the order."
    )
    public Response returnOrder(final OrderReturnRequest orderReturn) throws Throwable {
        final CalculationContext calculationContext = new CalculationContext();

        Observable<Boolean> metricsResponse = Observable.just(metrics.increment(Service.RETURNS.getServiceID()));
        Observable<Response> shippingResponse = callService(Service.SHIPPING, orderReturn);
        Observable<Response> billingResponse = callService(Service.BILLING, orderReturn);

        metricsResponse.subscribe(response -> {
            // don't care what happens
        });
        shippingResponse.subscribe(response -> {
                    ifCancellationFailed(Service.SHIPPING, response);

                    calculationContext.registerShippingSuccess();
                },

                calculationContext::registerException
        );
        billingResponse.subscribe(response -> {
                    ifCancellationFailed(Service.BILLING, response);

                    calculationContext.registerBillingSuccess();
                },

                calculationContext::registerException
        );

        return getResult(orderReturn, calculationContext);
    }

    private Response getResult(OrderReturnRequest orderReturn, CalculationContext calculationContext) throws Throwable {
        // todo : ugly
        while(!calculationContext.completed()) {
            Thread.sleep(1);
        }

        // todo : ugly as hell as well!
        if(calculationContext.withException()) {
            throw calculationContext.exception();
        }

        return Response.status(Response.Status.OK).entity(orderReturn).build();
    }

    <T> Observable<Response> callService(Service service, final T payload) throws DyingServiceException, InterruptedException {
        String baseUrl = consulServiceUrlFinder.findFirstAvailableServiceUrl(service.getServiceID());

        RestPostCommand<T> restPostCommand = new RestPostCommand<>(service, baseUrl, service.getServicePath(), payload);

        return restPostCommand.toObservable();
    }

    void ifCancellationFailed(Service service, Response response) {
        if(Response.Status.OK.getStatusCode()!=response.getStatus()) {
            throw new ServiceResultException(response.getStatus(), response.readEntity(ErrorMessageResponse.class), service);
        }

        OrderReturnResponse orderReturnResponse = response.readEntity(OrderReturnResponse.class);
        if(!"returned".equalsIgnoreCase(orderReturnResponse.getState())) {
            throw new UnableToCancelException(service, orderReturnResponse);
        }
    }

    static class CalculationContext {
        private boolean cancelBillingSuccess = false;
        private boolean cancelShippingSuccess = false;

        private Throwable throwable;

        void registerShippingSuccess() {
            cancelShippingSuccess = true;
        }

        void registerBillingSuccess() {
            cancelBillingSuccess = true;
        }

        void registerException(Throwable e) {
            throwable = e;
        }

        boolean completed() {
            return cancelBillingSuccess && cancelShippingSuccess || throwable!=null;
        }

        public boolean withException() {
            return throwable!=null;
        }

        public Throwable exception() {
            return throwable;
        }
    }
}
