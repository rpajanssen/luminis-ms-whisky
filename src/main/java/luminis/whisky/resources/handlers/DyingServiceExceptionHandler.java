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
        return Response.status(503).entity(new ErrorMessage(503, e.getMessage())).build();
    }
}
