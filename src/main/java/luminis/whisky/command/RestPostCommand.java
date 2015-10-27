package luminis.whisky.command;

import luminis.whisky.client.RestPostClient;
import luminis.whisky.util.Service;

import javax.ws.rs.core.Response;

/**
 * Hystrix command implementation that posts a rest-call to a service.
 *
 * @param <T> the payload of the post
 */
public class RestPostCommand<T> extends AbstractRestCommand {
    private final RestPostClient<T> restClient;

    public RestPostCommand(Service service, String baseUri, String path, T payload) {
        super(service, CommandType.POST);

        restClient = new RestPostClient<>(baseUri, path, payload);
    }

    @Override
    protected Response run() throws Exception {
        return restClient.post();
    }
}
