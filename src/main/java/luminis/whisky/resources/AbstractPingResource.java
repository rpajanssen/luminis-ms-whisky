package luminis.whisky.resources;

import com.wordnik.swagger.annotations.ApiOperation;
import luminis.whisky.domain.Ping;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Just implements ping!
 */
public abstract class AbstractPingResource {
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
}
