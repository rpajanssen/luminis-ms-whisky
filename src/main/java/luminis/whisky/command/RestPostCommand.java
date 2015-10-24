package luminis.whisky.command;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import luminis.whisky.client.RestClient;
import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.util.Service;

import javax.ws.rs.core.Response;

public class RestPostCommand<T> extends HystrixCommand<Response> {
    private final RestClient<T> restClient;
    private final Service service;

    public RestPostCommand(Service service, String baseUri, String path, T payload) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("post-" + service.getServiceID())));

        this.service = service;
        restClient = new RestClient<>(baseUri, path, payload);
    }

    @Override
    protected Response run() throws Exception {
        return restClient.post();
    }

    @Override
    public Response execute() {
        try {
            return super.execute();
        } catch (HystrixRuntimeException e) {
            if (e.getCause() instanceof InterruptedException) {
                throw new ThreadInterruptedException(e.getCause().getMessage(), e.getCause());
            }

            System.err.println(String.format("problem with service %s : %s ", service.getServiceID(), e.getMessage()));

            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(new ErrorMessageResponse(
                                    Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                                    String.format("service %s unavailbale", service.getServiceID()))
                    ).build();
        }
    }
}
