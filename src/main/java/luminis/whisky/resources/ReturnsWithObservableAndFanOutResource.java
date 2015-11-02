package luminis.whisky.resources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import luminis.whisky.command.RestPostCommand;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.OrderReturnRequest;
import luminis.whisky.domain.OrderReturnResponse;
import luminis.whisky.domain.Ping;
import luminis.whisky.resources.exception.UnableToCancelException;
import luminis.whisky.util.Metrics;
import luminis.whisky.util.Service;
import rx.Observable;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// todo : transaction rollback on failure
@Path("/concurrent-fanout-returns")
@Api(value="Order returns - concurrent fan-out, rxjava", description = "Returns the order and cancels shipping" +
        " and billing. It executes the cancellation of the shipping and billing concurrently using observables." +
        " It fans out (concurrently) to all registered services and reacts on the first incoming result.")
public class ReturnsWithObservableAndFanOutResource {
    private final ConsulServiceUrlFinder consulServiceUrlFinder;
    private final Metrics metrics;

    public ReturnsWithObservableAndFanOutResource(ConsulServiceUrlFinder consulServiceUrlFinder, Metrics metrics) {
        this.consulServiceUrlFinder = consulServiceUrlFinder;
        this.metrics = metrics;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Ping",
            notes = "Simply returns pong."
    )
    public Response ping() {
        return Response.status(Response.Status.OK).entity(new Ping("pong")).build();
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
        Observable<String> shippingUrls = Observable.from(consulServiceUrlFinder.findServiceUrl(Service.SHIPPING.getServiceID()));
        Observable<String> billingUrls = Observable.from(consulServiceUrlFinder.findServiceUrl(Service.BILLING.getServiceID()));

        metricsResponse.subscribe(response -> {
            // don't care what happens
        });

        shippingUrls.subscribe(
                url -> {
                    try {
                        calculationContext.fanOutShipping();

                        Response response = callService(Service.SHIPPING, url, orderReturn);
                        ifCancellationFailed(Service.SHIPPING, response);

                        calculationContext.registerShippingSuccess();
                    } catch (Throwable e) {
                        calculationContext.registerShippingException(e);
                    } finally {
                        calculationContext.signOffShippingFanOut();
                    }
                }
        );

        billingUrls.subscribe(
                url -> {
                    try {
                        calculationContext.fanOutBilling();

                        Response response = callService(Service.BILLING, url, orderReturn);
                        ifCancellationFailed(Service.BILLING, response);

                        calculationContext.registerBillingSuccess();
                    } catch (Throwable e) {
                        calculationContext.registerBillingException(e);
                    } finally {
                        calculationContext.signOffBillingFanOut();
                    }
                }
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

    <T> Response callService(Service service, String baseUrl, final T payload) throws DyingServiceException, InterruptedException {
        RestPostCommand<T> restPostCommand = new RestPostCommand<>(service, baseUrl, service.getServicePath(), payload);

        return restPostCommand.execute();
    }

    void ifCancellationFailed(Service service, Response response) {
        OrderReturnResponse orderReturnResponse = response.readEntity(OrderReturnResponse.class);
        if(!"returned".equalsIgnoreCase(orderReturnResponse.getState())) {
            throw new UnableToCancelException(service, orderReturnResponse);
        }
    }

    // todo : unit test
    class CalculationContext {
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

        boolean completed() {
            return cancelBillingSuccess && cancelShippingSuccess ||
                    ( (billingException!=null && billingFanOutCount==0) ||
                            (shippingException!=null && shippingFanOutCount==0)
                    );
        }

        boolean withException() {
            return billingException!=null || shippingException!=null;
        }

        Throwable exception() {
            return billingException != null ? billingException : shippingException;
        }
    }
}
