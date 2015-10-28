package luminis.whisky.resources.stubs;

import luminis.whisky.domain.OrderReturnRequest;
import luminis.whisky.domain.OrderReturnResponse;
import luminis.whisky.domain.Ping;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/bills")
public class BillingStubResource {

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping() {
        return Response.status(Response.Status.OK).entity(new Ping("pong")).build();
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

        if("111".equals(orderReturn.getOrderNumber())) {
            return Response.status(Response.Status.OK).entity(new OrderReturnResponse(orderReturn).withState(OrderReturnResponse.STATE_CANCELLED)).build();
        }

        return Response.status(Response.Status.OK).entity(new OrderReturnResponse(orderReturn).withState(OrderReturnResponse.STATE_RETURNED)).build();
    }
}
