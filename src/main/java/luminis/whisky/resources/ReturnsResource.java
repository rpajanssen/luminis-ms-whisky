package luminis.whisky.resources;

import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.domain.OrderReturn;
import luminis.whisky.util.ApplicationConstants;
import luminis.whisky.util.Metrics;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

// todo : error handling
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
    public Response returnOrder(final OrderReturn orderReturn) {
        System.out.println("Incoming return order call");
        metrics.increment(ApplicationConstants.RETURNS_SERVICE_ID);

        Response response = notifyShipping(orderReturn);
        if(response.getStatus()!= 200) {
            return Response.status(response.getStatus()).entity(response.getEntity()).build();
        }

        // todo : what if state is not 'returned'?

        response = notifyBilling(orderReturn);
        if(response.getStatus() != 200) {
            return Response.status(response.getStatus()).entity(response.getEntity()).build();
        }

        // todo : what if state is not 'returned'?

        return Response.status(200).entity(orderReturn).build();
    }

    private Response notifyShipping(final OrderReturn orderReturn) {
        String shippingUrl = consulServiceUrlFinder.findServiceUrl(ApplicationConstants.SHIPPING_SERVICE_ID);
        System.out.println("Found shipping url: " + shippingUrl);
        return post(shippingUrl, ApplicationConstants.SHIPPING_SERVICE_PATH, orderReturn);
    }

    private Response notifyBilling(final OrderReturn orderReturn) {
        String billingUrl = consulServiceUrlFinder.findServiceUrl(ApplicationConstants.BILLING_SERVICE_ID);
        System.out.println("Found billingUrl url: " + billingUrl);
        return post(billingUrl, ApplicationConstants.BILLING_SERVICE_PATH, orderReturn);
    }

    private Response post(String baseUri, String path, final OrderReturn orderReturn) {
        return getWebTarget(baseUri).path(path).request().accept(MediaType.APPLICATION_JSON).post(Entity.json(orderReturn), Response.class);
    }

    private WebTarget getWebTarget(String baseUri) {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        return client.target(getBaseURI(baseUri));
    }

    public URI getBaseURI(String baseUri) {
        return UriBuilder.fromUri(baseUri).build();
    }
}
