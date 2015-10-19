package luminis.whisky.resources.handlers;

import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DyingServiceExceptionHandler implements ExceptionMapper<DyingServiceException>{
    @Override
    public Response toResponse(DyingServiceException e) {
        System.err.println(String.format("unexpected exception occurred : %s ", e.getMessage()));

        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(new ErrorMessage(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), e.getMessage())).build();
    }
}
