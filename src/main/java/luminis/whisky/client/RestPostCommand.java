package luminis.whisky.client;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class RestPostCommand<T> extends HystrixCommand {
    String baseUri;
    String path;
    T payload;

    public RestPostCommand(String baseUri, String path, T payload) {
        // todo : maybe we need the service id here as well
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("rest-post")));

        this.baseUri = baseUri;
        this.path = path;
        this.payload = payload;
    }

    protected Response post() {
        return getWebTarget(baseUri).path(path).request().accept(MediaType.APPLICATION_JSON).post(Entity.json(payload), Response.class);
    }

    private WebTarget getWebTarget(String baseUri) {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        return client.target(getBaseURI(baseUri));
    }

    public URI getBaseURI(String baseUri) {
        return UriBuilder.fromUri(baseUri).build();
    }

    @Override
    protected Object run() throws Exception {
        return post();
    }
}
