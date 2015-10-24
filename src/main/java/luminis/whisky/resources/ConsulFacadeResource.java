package luminis.whisky.resources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import luminis.whisky.client.ConsulClient;
import luminis.whisky.core.consul.ConsulConfiguration;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A facade around a running Consul instance.
 */
@Path("/consul")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value="Consul facade", description = "Exposes Consul API to enable Consul operations.")
public class ConsulFacadeResource {

    private ConsulClient consulClient;

    public ConsulFacadeResource() {
        consulClient = new ConsulClient();

        System.out.println("instantiated consul resource");
    }

    @GET
    public Response ping() {
        return Response.status(Response.Status.OK).entity("pong").build();
    }

    @POST
    @Path("/ip/{address}")
    @ApiOperation(
            value = "Set IP address of local consul agent.",
            notes = ""
    )
    public Response ip(@PathParam("address") final String address) {
        ConsulConfiguration.getInstance().setIp(address);

        return Response.status(Response.Status.OK).entity("Host set to " + address).build();
    }

    @POST
    @Path("/port/{port}")
    @ApiOperation(
            value = "Set port of local consul agent.",
            notes = ""
    )
    public Response port(@PathParam("port") final String port) {
        ConsulConfiguration.getInstance().setPort(port);

        return Response.status(Response.Status.OK).entity("Port set to " + port).build();
    }


    @GET
    @Path("/services")
    @ApiOperation(
            value = "Returns the services the agent is managing.",
            notes = ""
    )
    public Response services() {
        return Response.status(Response.Status.OK).entity(consulClient.services()).build();
    }

    @GET
    @Path("/checks")
    @ApiOperation(
            value = "Returns the checks the local agent is managing.",
            notes = ""
    )
    public Response checks() {
        return Response.status(Response.Status.OK).entity(consulClient.checks()).build();
    }

    @GET
    @Path("/members")
    @ApiOperation(
            value = "Returns the members as seen by the local serf agent.",
            notes = ""
    )
    public Response members() {
        return Response.status(Response.Status.OK).entity(consulClient.members()).build();
    }

    @GET
    @Path("/self")
    @ApiOperation(
            value = "Returns the local node configuration.",
            notes = ""
    )
    public Response self() {
        return Response.status(Response.Status.OK).entity(consulClient.self()).build();
    }

    @GET
    @Path("/join/{address}")
    @ApiOperation(
            value = "Triggers the local agent to join a node.",
            notes = ""
    )
    public Response join(@PathParam("address") final String address) {
        return Response.status(Response.Status.OK).entity(consulClient.join(address)).build();
    }

    @GET
    @Path("/force-leave/{node}")
    @ApiOperation(
            value = "Forces removal of a node.",
            notes = ""
    )
    public Response forceLeave(@PathParam("node") final String node) {
        return Response.status(Response.Status.OK).entity(consulClient.forceLeave(node)).build();
    }

    @GET
    @Path("/catalog/services")
    @ApiOperation(
            value = "Lists services in a given DC.",
            notes = ""
    )
    public Response catalogServices() {
        return Response.status(Response.Status.OK).entity(consulClient.catalogServices()).build();
    }

    @GET
    @Path("/catalog/service/{service}")
    @ApiOperation(
            value = "Lists the nodes in a given service.",
            notes = ""
    )
    public Response catalogService(@PathParam("service") final String service) {
        return Response.status(Response.Status.OK).entity(consulClient.catalogService(service)).build();
    }

    @GET
    @Path("/health/service/{service}")
    @ApiOperation(
            value = "Returns the nodes and health info of a service.",
            notes = ""
    )
    public Response healthService(@PathParam("service") final String service) {
        return Response.status(Response.Status.OK).entity(consulClient.healthService(service)).build();
    }
}
