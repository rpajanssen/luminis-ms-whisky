package luminis.whisky.resources;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import luminis.whisky.command.RestPostCommand;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.domain.OrderReturnRequest;
import luminis.whisky.util.Metrics;
import luminis.whisky.util.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// todo : execute calls to billing / shipping concurrently
// todo : fan out
@Path("/returns")
public class ReturnsResource {
    private ConsulServiceUrlFinder consulServiceUrlFinder;
    private Metrics metrics;

    public ReturnsResource() {
        consulServiceUrlFinder = new ConsulServiceUrlFinder();
        metrics = new Metrics(consulServiceUrlFinder);
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping() {
        return Response.status(Response.Status.OK).entity("pong").build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response returnOrder(final OrderReturnRequest orderReturn) throws DyingServiceException, InterruptedException {
        System.out.println("Incoming return order call: " + orderReturn.getOrderNumber());

        metrics.increment(Service.RETURNS.getServiceID());

        Response response = notify(Service.SHIPPING, orderReturn);
        if(Response.Status.OK.getStatusCode()!=response.getStatus()) {
            return Response.status(response.getStatus()).entity(response.getEntity()).build();
        }

        // todo : what if state is not 'returned'?

        response = notify(Service.BILLING, orderReturn);
        if(Response.Status.OK.getStatusCode()!=response.getStatus()) {
            return Response.status(response.getStatus()).entity(response.getEntity()).build();
        }

        // todo : what if state is not 'returned'?

        return Response.status(Response.Status.OK).entity(orderReturn).build();
    }

    // todo : cleanup
    private Response notify(Service service, final OrderReturnRequest orderReturn) throws DyingServiceException, InterruptedException {
        String url = consulServiceUrlFinder.findServiceUrl(service.getServiceID());

        try {
            RestPostCommand<OrderReturnRequest> restPostCommand = new RestPostCommand<>(service, url, service.getServicePath(), orderReturn);

            return restPostCommand.execute();
        } catch (HystrixRuntimeException e) {
            if (e.getCause() instanceof InterruptedException) {
                throw (InterruptedException) e.getCause();
            }

            System.err.println(String.format("problem with service %s : %s ", service.getServiceID(), e.getMessage()));

            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(new ErrorMessageResponse(
                                    Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                                    String.format("service %s unavailbale", service.getServiceID()))
                    ).build();
        }

    }
}
