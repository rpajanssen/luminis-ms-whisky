package luminis.whisky.resources.stubs;

import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.domain.OrderReturnRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/shipments")
public class ShippingStubResource {

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
        if("666".equalsIgnoreCase(orderReturn.getOrderNumber())) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessageResponse(
                                    Response.Status.NOT_FOUND.getStatusCode(),
                                    String.format("No shipments for order %s found.", orderReturn.getOrderNumber()))
                    ).build();
        }

        return Response.status(Response.Status.OK).entity(orderReturn).build();
    }
}
