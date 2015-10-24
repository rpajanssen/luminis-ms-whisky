package luminis.whisky.resources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import luminis.whisky.command.RestPostCommand;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.domain.OrderReturnRequest;
import luminis.whisky.domain.OrderReturnResponse;
import luminis.whisky.domain.Ping;
import luminis.whisky.util.Metrics;
import luminis.whisky.util.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// todo : execute calls to billing / shipping concurrently
// todo : fan out
// todo : transaction rollback on failure
@Path("/returns")
@Api(value="Order returns", description = "Returns the order and cancels shipping and billing.")
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
        System.out.println("Incoming return order call: " + orderReturn.getOrderNumber());

        metrics.increment(Service.RETURNS.getServiceID());

        Response response = callService(Service.SHIPPING, orderReturn);
        if(Response.Status.OK.getStatusCode()!=response.getStatus()) {
            return response;
        }

        // todo : throw exception or some other cleanup
        if("returned".equalsIgnoreCase(((OrderReturnResponse)response.getEntity()).getState())) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(
                            new ErrorMessageResponse(
                                    Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                                    String.format("unable to cancel shipping")
                            )
                    ).build();
        }

        response = callService(Service.BILLING, orderReturn);
        if(Response.Status.OK.getStatusCode()!=response.getStatus()) {
            return response;
        }

        // todo : throw exception or some other cleanup
        if("returned".equalsIgnoreCase(((OrderReturnResponse)response.getEntity()).getState())) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(
                            new ErrorMessageResponse(
                                    Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                                    String.format("unable to cancel billing")
                            )
                    ).build();
        }


        return Response.status(Response.Status.OK).entity(orderReturn).build();
    }

    private <T> Response callService(Service service, final T payload) throws DyingServiceException, InterruptedException {
        String baseUrl = consulServiceUrlFinder.findServiceUrl(service.getServiceID());

        RestPostCommand<T> restPostCommand = new RestPostCommand<>(service, baseUrl, service.getServicePath(), payload);

        return restPostCommand.execute();
    }
}
