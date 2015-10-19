package luminis.whisky.resources;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import luminis.whisky.command.RestPostCommand;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.ErrorMessage;
import luminis.whisky.domain.OrderReturn;
import luminis.whisky.util.Metrics;
import luminis.whisky.util.Services;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// todo : swagger
// todo : hystrix
// todo : execute calls to billing / shipping concurrently
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
    public Response ping() {
        return Response.status(Response.Status.OK).entity("pong").build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response returnOrder(final OrderReturn orderReturn) throws DyingServiceException, InterruptedException {
        System.out.println("Incoming return order call: " + orderReturn.getOrderNumber());

        metrics.increment(Services.RETURNS.getServiceID());

        Response response = notify(Services.SHIPPING, orderReturn);
        if(Response.Status.OK.getStatusCode()!=response.getStatus()) {
            return Response.status(response.getStatus()).entity(response.getEntity()).build();
        }

        // todo : what if state is not 'returned'?

        response = notify(Services.BILLING, orderReturn);
        if(Response.Status.OK.getStatusCode()!=response.getStatus()) {
            return Response.status(response.getStatus()).entity(response.getEntity()).build();
        }

        // todo : what if state is not 'returned'?

        return Response.status(Response.Status.OK).entity(orderReturn).build();
    }

    private Response notify(Services service, final OrderReturn orderReturn) throws DyingServiceException, InterruptedException {
        String url = consulServiceUrlFinder.findServiceUrl(service.getServiceID());

//        RestClient<OrderReturn> restClient =
//                new RestClient<>(url, service.getServicePath(), orderReturn);
//
//        return restClient.post();

        try {
            RestPostCommand<OrderReturn> restPostCommand = new RestPostCommand<>(service, url, service.getServicePath(), orderReturn);

            return restPostCommand.execute();
        } catch (HystrixRuntimeException e) {
            if (e.getCause() instanceof InterruptedException) {
                throw (InterruptedException) e.getCause();
            }

            System.err.println(String.format("problem with service %s : %s ", service.getServiceID(), e.getMessage()));

            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(new ErrorMessage(
                                    Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                                    String.format("service %s unavailbale", service.getServiceID()))
                    ).build();
        }

    }
}
