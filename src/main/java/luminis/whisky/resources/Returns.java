package luminis.whisky.resources;


import com.google.common.base.Optional;
import luminis.whisky.domain.OrderReturn;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/returns")
@Produces(MediaType.APPLICATION_JSON)
public class Returns {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response returnOrder(final OrderReturn orderReturn) {

        // call shipping

        // call billing


        orderReturn.setOrderNumber("hahaha");

        return Response.status(201).entity(orderReturn).build();
    }


    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response ping() {
        return Response.status(200).entity("pong").build();
    }
}
