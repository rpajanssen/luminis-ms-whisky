package luminis.whisky.client;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class RestGetClient {
    private final String baseUri;
    private final String path;

    private final Client client;

    public RestGetClient(String baseUri, String path) {
        this.baseUri = baseUri;
        this.path = path;

        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.CONNECT_TIMEOUT, 2000);
        config.property(ClientProperties.READ_TIMEOUT, 4000);
        client = ClientBuilder.newClient(config);
    }

    public Response get() {
        return getWebTarget(baseUri).path(path).request().accept(MediaType.APPLICATION_JSON).get();
    }

    protected WebTarget getWebTarget(String baseUri) {
        return client.target(getBaseURI(baseUri));
    }

    protected URI getBaseURI(String baseUri) {
        return UriBuilder.fromUri(baseUri).build();
    }

    protected String getBaseUri() {
        return baseUri;
    }

    protected String getPath() {
        return path;
    }

    protected Client getClient() {
        return client;
    }
}
