package luminis.whisky.resources.stubs;

import luminis.whisky.domain.OrderReturn;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// todo : on occasion return a 500 response
@Path("/bills")
public class BillingStubResource {

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response ping() {
        return Response.status(200).entity("pong").build();
    }

    @POST
    @Path("/returns")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response returnOrder(final OrderReturn orderReturn) {
        return Response.status(200).entity(orderReturn).build();
    }
}