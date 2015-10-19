package luminis.whisky.command;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import luminis.whisky.client.RestClient;
import luminis.whisky.util.Services;

import javax.ws.rs.core.Response;

public class RestPostCommand<T> extends HystrixCommand<Response> {
    private final RestClient<T> restClient;

    public RestPostCommand(Services service, String baseUri, String path, T payload) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(service.getServiceID())));

        restClient = new RestClient<>(baseUri, path, payload);
    }

    @Override
    protected Response run() throws Exception {
        return restClient.post();
    }
}
