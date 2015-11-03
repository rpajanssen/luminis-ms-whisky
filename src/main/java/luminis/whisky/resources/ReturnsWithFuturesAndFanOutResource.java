package luminis.whisky.resources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.OrderReturnRequest;
import luminis.whisky.util.Metrics;
import luminis.whisky.util.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// todo : transaction rollback on failure
@Path("/concurrent-futures-fanout-returns")
@Api(value="Order returns - concurrent fan-out", description = "Returns the order and cancels shipping and billing." +
        " It executes the cancellation of the billing and shipping concurrently using and executor services" +
        " and callables. It fans out to all available shipping and billing services and reacts to the" +
        " first incoming response.")
public class ReturnsWithFuturesAndFanOutResource extends AbstractFanOutResource{
    private final ConsulServiceUrlFinder consulServiceUrlFinder;
    private final Metrics metrics;

    public ReturnsWithFuturesAndFanOutResource(ConsulServiceUrlFinder consulServiceUrlFinder, Metrics metrics) {
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

        List<Callable> tasks = new ArrayList<>();

        // define metrics task
        tasks.add(() -> metrics.increment(Service.RETURNS.getServiceID()));
        // define cancel shipping tasks
        fanOutShippingCancellation(orderReturn, calculationContext, tasks);
        // define cancel billing tasks
        fanOutBillingCancellation(orderReturn, calculationContext, tasks);

        // execute all tasks
        ExecutorService threadPool = Executors.newCachedThreadPool();
        for(Callable task : tasks) {
            threadPool.submit(task);
        }

        return waitForResult(orderReturn, calculationContext);
    }

    private void fanOutShippingCancellation(OrderReturnRequest orderReturn, CalculationContext calculationContext, List<Callable> tasks) throws DyingServiceException {
        List<String> shippingServiceUrls = consulServiceUrlFinder.findServiceUrl(Service.SHIPPING.getServiceID());
        for(String url : shippingServiceUrls) {
            tasks.add(() -> {
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

                return null;
            });
        }
    }

    private void fanOutBillingCancellation(OrderReturnRequest orderReturn, CalculationContext calculationContext, List<Callable> tasks) throws DyingServiceException {
        List<String> billingServiceUrls = consulServiceUrlFinder.findServiceUrl(Service.BILLING.getServiceID());
        for(String url : billingServiceUrls) {
            tasks.add(() -> {
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

                return null;
            });
        }
    }
}
