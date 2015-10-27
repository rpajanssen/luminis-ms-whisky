package luminis.whisky.command;

import luminis.whisky.client.RestGetClient;
import luminis.whisky.util.Service;

import javax.ws.rs.core.Response;

/**
 * Hystrix command implementation that executes a get rest-call to a service.
 */
public class RestGetCommand extends AbstractRestCommand {
    private final RestGetClient restClient;


    public RestGetCommand(Service service, String baseUri, String path) {
        super(service, CommandType.GET);

        restClient = new RestGetClient(baseUri, path);
    }

    @Override
    protected Response run() throws Exception {
        return restClient.get();
    }
}
