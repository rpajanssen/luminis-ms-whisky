package luminis.whisky.resources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import luminis.whisky.command.RestPostCommand;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.OrderReturnRequest;
import luminis.whisky.domain.OrderReturnResponse;
import luminis.whisky.resources.exception.UnableToCancelException;
import luminis.whisky.util.Metrics;
import luminis.whisky.util.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// todo : transaction rollback on failure
@Path("/imperative-returns")
@Api(value="Order returns - imperative, sequential", description = "Returns the order and cancels shipping and billing." +
        " Comfort zone imperative sequential programming.")
public class ReturnsResource extends AbstractPingResource {
    private final ConsulServiceUrlFinder consulServiceUrlFinder;
    private final Metrics metrics;

    public ReturnsResource(ConsulServiceUrlFinder consulServiceUrlFinder, Metrics metrics) {
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
    public Response returnOrder(final OrderReturnRequest orderReturn) throws DyingServiceException, InterruptedException {
        metrics.increment(Service.RETURNS.getServiceID());

        Response response = callService(Service.SHIPPING, orderReturn);
        ifCancellationFailed(Service.SHIPPING, response);

        response = callService(Service.BILLING, orderReturn);
        ifCancellationFailed(Service.BILLING, response);

        return Response.status(Response.Status.OK).entity(orderReturn).build();
    }

    <T> Response callService(Service service, final T payload) throws DyingServiceException, InterruptedException {
        String baseUrl = consulServiceUrlFinder.findFirstAvailableServiceUrl(service.getServiceID());

        RestPostCommand<T> restPostCommand = new RestPostCommand<>(service, baseUrl, service.getServicePath(), payload);

        return restPostCommand.execute();
    }

    void ifCancellationFailed(Service service, Response response) {
        OrderReturnResponse orderReturnResponse = response.readEntity(OrderReturnResponse.class);
        if(!"returned".equalsIgnoreCase(orderReturnResponse.getState())) {
            throw new UnableToCancelException(service, orderReturnResponse);
        }
    }
}
