package luminis.whisky.resources.stubs;

import luminis.whisky.domain.ErrorMessage;
import luminis.whisky.domain.OrderReturn;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/shipments")
public class ShippingStubResource {

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
        if("666".equalsIgnoreCase(orderReturn.getOrderNumber())) {
            return Response.status(404).entity(new ErrorMessage(404, String.format("No shipments for order %s found.", orderReturn.getOrderNumber()))).build();
        }

        return Response.status(200).entity(orderReturn).build();
    }
}
