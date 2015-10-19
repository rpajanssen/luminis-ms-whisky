package luminis.whisky.resources;

import luminis.whisky.client.RestClient;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.OrderReturn;
import luminis.whisky.util.Metrics;
import luminis.whisky.util.Services;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// todo : swagger
// todo : hystrix
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
        return Response.status(200).entity("pong").build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response returnOrder(final OrderReturn orderReturn) throws DyingServiceException {
        System.out.println("Incoming return order call: " + orderReturn.getOrderNumber());

        metrics.increment(Services.RETURNS.getServiceID());

        Response response = notify(Services.SHIPPING, orderReturn);
        if(response.getStatus()!= 200) {
            return Response.status(response.getStatus()).entity(response.getEntity()).build();
        }

        // todo : what if state is not 'returned'?

        response = notify(Services.BILLING, orderReturn);
        if(response.getStatus() != 200) {
            return Response.status(response.getStatus()).entity(response.getEntity()).build();
        }

        // todo : what if state is not 'returned'?

        return Response.status(200).entity(orderReturn).build();
    }

    private Response notify(Services service, final OrderReturn orderReturn) throws DyingServiceException {
        String url = consulServiceUrlFinder.findServiceUrl(service.getServiceID());

        RestClient<OrderReturn> restClient =
                new RestClient<>(url, service.getServicePath(), orderReturn);

        return restClient.post();
    }
}
