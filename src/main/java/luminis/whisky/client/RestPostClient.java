package luminis.whisky.client;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestPostClient<T>  extends RestGetClient {
    private final T payload;

    public RestPostClient(String baseUri, String path, T payload) {
        super(baseUri, path);

        this.payload = payload;
    }

    public Response post() {
        return getWebTarget(getBaseUri()).path(getPath()).request().accept(MediaType.APPLICATION_JSON).post(Entity.json(payload), Response.class);
    }
}
