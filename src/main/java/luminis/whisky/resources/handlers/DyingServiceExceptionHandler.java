package luminis.whisky.resources.handlers;

import luminis.whisky.core.consul.DyingServiceException;
import luminis.whisky.domain.ErrorMessageResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DyingServiceExceptionHandler implements ExceptionMapper<DyingServiceException>{
    @Override
    public Response toResponse(DyingServiceException e) {
        System.err.println(String.format(e.getErrorCode().getMessage(), e.getServiceId()));

        return Response.status(e.getErrorCode().getResponseStatus())
                .entity(
                        new ErrorMessageResponse(
                                e.getErrorCode().getCode(),
                                String.format(e.getErrorCode().getMessage(), e.getServiceId())
                        )
                ).build();
    }
}
