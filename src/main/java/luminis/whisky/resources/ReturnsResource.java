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

// todo : execute calls to metrics / billing / shipping concurrently
// todo : fan out
// todo : transaction rollback on failure
@Path("/returns")
@Api(value="Order returns - sequential", description = "Returns the order and cancels shipping and billing. Comfort zone imperative" +
        " sequential programming.")
public class ReturnsResource {
    private final ConsulServiceUrlFinder consulServiceUrlFinder;
    private final Metrics metrics;

    public ReturnsResource(ConsulServiceUrlFinder consulServiceUrlFinder, Metrics metrics) {
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
    public Response returnOrder(final OrderReturnRequest orderReturn) throws DyingServiceException, InterruptedException {
        System.out.println(String.format("incoming return order call for order %s", orderReturn.getOrderNumber()));

        metrics.increment(Service.RETURNS.getServiceID());

        Response response = callService(Service.SHIPPING, orderReturn);
        ifOrderStateNotReturnedThrowException(Service.SHIPPING, response);

        response = callService(Service.BILLING, orderReturn);
        ifOrderStateNotReturnedThrowException(Service.BILLING, response);

        return Response.status(Response.Status.OK).entity(orderReturn).build();
    }

    <T> Response callService(Service service, final T payload) throws DyingServiceException, InterruptedException {
        String baseUrl = consulServiceUrlFinder.findFirstAvailableServiceUrl(service.getServiceID());

        RestPostCommand<T> restPostCommand = new RestPostCommand<>(service, baseUrl, service.getServicePath(), payload);

        return restPostCommand.execute();
    }

    void ifOrderStateNotReturnedThrowException(Service service, Response response) {
        OrderReturnResponse orderReturnResponse = response.readEntity(OrderReturnResponse.class);
        if(!"returned".equalsIgnoreCase(orderReturnResponse.getState())) {
            throw new UnableToCancelException(service, orderReturnResponse);
        }
    }
}
