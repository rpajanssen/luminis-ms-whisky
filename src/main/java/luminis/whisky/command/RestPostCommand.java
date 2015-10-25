package luminis.whisky.command;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import luminis.whisky.client.RestClient;
import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.util.Service;

import javax.ws.rs.core.Response;

/**
 * Hystrix command implementation that posts a rest-call to a service.
 *
 * @param <T> the payload of the post
 */
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

    /**
     * Returns the response from the called service.
     *
     * Throws ThreadInterruptedException when this service was interrupted (Hystrix).
     * Throws ServiceResultException when the response status is not OK.
     * Throws ServiceNotAvailableException when the service to call is not available.
     *
     * @return Response The response of the called service
     */
    @Override
    public Response execute() {
        try {
            Response response =  super.execute();

            ifResponseStatusNotOKThenThrowException(response);

            return response;
        } catch (HystrixRuntimeException e) {
            ifCausedByInterruptThenThrowInterruptedException(e);

            // service is not available
            System.err.println(String.format("problem with service %s : %s ", service.getServiceID(), e.getMessage()));
            throw new UnavailableServiceException(service, e.getMessage());
        }
    }

    private void ifResponseStatusNotOKThenThrowException(Response response) {
        if(Response.Status.OK.getStatusCode()!=response.getStatus()) {
            throw new ServiceResultException(response.getStatus(), response.readEntity(ErrorMessageResponse.class), service);
        }
    }

    private void ifCausedByInterruptThenThrowInterruptedException(HystrixRuntimeException e) {
        if (e.getCause() instanceof InterruptedException) {
            throw new ThreadInterruptedException(e.getCause().getMessage(), e.getCause());
        }
    }
}
