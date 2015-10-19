package luminis.whisky.client;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class RestClient<T> {
    final String baseUri;
    final String path;
    final T payload;

    public RestClient(String baseUri, String path, T payload) {
        this.baseUri = baseUri;
        this.path = path;
        this.payload = payload;
    }

    public Response post() {
        return getWebTarget(baseUri).path(path).request().accept(MediaType.APPLICATION_JSON).post(Entity.json(payload), Response.class);
    }

    private WebTarget getWebTarget(String baseUri) {
        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.CONNECT_TIMEOUT, 2000);
        config.property(ClientProperties.READ_TIMEOUT, 5000);

        Client client = ClientBuilder.newClient(config);
        return client.target(getBaseURI(baseUri));
    }

    public URI getBaseURI(String baseUri) {
        return UriBuilder.fromUri(baseUri).build();
    }
}
