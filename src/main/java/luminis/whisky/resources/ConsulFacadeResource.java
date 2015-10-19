package luminis.whisky.resources;

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
public class ConsulFacadeResource {

    private ConsulClient consulClient;

    public ConsulFacadeResource() {
        consulClient = new ConsulClient();

        System.out.println("instantiated consul resource");
    }

    @GET
    public Response ping() {
        return Response.status(200).entity("pong").build();
    }

    @POST
    @Path("/ip/{address}")
    public Response ip(@PathParam("address") final String address) {
        ConsulConfiguration.getInstance().setIp(address);

        return Response.status(200).entity("Host set to " + address).build();
    }

    @POST
    @Path("/port/{port}")
    public Response port(@PathParam("port") final String port) {
        ConsulConfiguration.getInstance().setPort(port);

        return Response.status(200).entity("Port set to " + port).build();
    }


    @GET
    @Path("/services")
    public Response services() {
        return Response.status(200).entity(consulClient.services()).build();
    }

    @GET
    @Path("/checks")
    public Response checks() {
        return Response.status(200).entity(consulClient.checks()).build();
    }

    @GET
    @Path("/members")
    public Response members() {
        return Response.status(200).entity(consulClient.members()).build();
    }

    @GET
    @Path("/self")
    public Response self() {
        return Response.status(200).entity(consulClient.self()).build();
    }

    @GET
    @Path("/join/{address}")
    public Response join(@PathParam("address") final String address) {
        return Response.status(200).entity(consulClient.join(address)).build();
    }

    @GET
    @Path("/force-leave/{node}")
    public Response forceLeave(@PathParam("node") final String node) {
        return Response.status(200).entity(consulClient.forceLeave(node)).build();
    }

    @GET
    @Path("/catalog/services")
    public Response catalogServices() {
        return Response.status(200).entity(consulClient.catalogServices()).build();
    }

    @GET
    @Path("/catalog/service/{service}")
    public Response catalogService(@PathParam("service") final String service) {
        return Response.status(200).entity(consulClient.catalogService(service)).build();
    }

    @GET
    @Path("/health/service/{service}")
    public Response healthService(@PathParam("service") final String service) {
        return Response.status(200).entity(consulClient.healthService(service)).build();
    }
}
