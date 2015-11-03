package luminis.whisky.resources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.domain.OrderReturnRequest;
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
public class ReturnsWithObservableAndFanOutResource extends AbstractFanOutResource {
    private final ConsulServiceUrlFinder consulServiceUrlFinder;
    private final Metrics metrics;

    public ReturnsWithObservableAndFanOutResource(ConsulServiceUrlFinder consulServiceUrlFinder, Metrics metrics) {
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
                        ifCancellationFailedThrowException(Service.SHIPPING, response);

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
                        ifCancellationFailedThrowException(Service.BILLING, response);

                        calculationContext.registerBillingSuccess();
                    } catch (Throwable e) {
                        calculationContext.registerBillingException(e);
                    } finally {
                        calculationContext.signOffBillingFanOut();
                    }
                }
        );

        return waitForResult(orderReturn, calculationContext);
    }
}
