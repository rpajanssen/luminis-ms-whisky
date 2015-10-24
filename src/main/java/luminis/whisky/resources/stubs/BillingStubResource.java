package luminis.whisky.resources.stubs;

import luminis.whisky.domain.OrderReturnRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/bills")
public class BillingStubResource {

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response ping() {
        return Response.status(Response.Status.OK).entity("pong").build();
    }

    @POST
    @Path("/returns")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response returnOrder(final OrderReturnRequest orderReturn) {
        if("007".equals(orderReturn.getOrderNumber())) {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                // silently fail
            }
        }

        return Response.status(Response.Status.OK).entity(orderReturn).build();
    }
}
