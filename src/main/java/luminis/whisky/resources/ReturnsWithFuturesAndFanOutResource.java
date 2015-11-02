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
public class ReturnsWithFuturesAndFanOutResource {
    private final ConsulServiceUrlFinder consulServiceUrlFinder;
    private final Metrics metrics;

    public ReturnsWithFuturesAndFanOutResource(ConsulServiceUrlFinder consulServiceUrlFinder, Metrics metrics) {
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

        return getResult(orderReturn, calculationContext);
    }

    private void fanOutShippingCancellation(OrderReturnRequest orderReturn, CalculationContext calculationContext, List<Callable> tasks) throws DyingServiceException {
        List<String> shippingServiceUrls = consulServiceUrlFinder.findServiceUrl(Service.SHIPPING.getServiceID());
        for(String url : shippingServiceUrls) {
            tasks.add(() -> {
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
                    ifCancellationFailed(Service.BILLING, response);

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
