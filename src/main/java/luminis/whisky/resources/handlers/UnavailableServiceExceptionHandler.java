package luminis.whisky.resources.handlers;

import luminis.whisky.command.UnavailableServiceException;
import luminis.whisky.domain.ErrorMessageResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UnavailableServiceExceptionHandler implements ExceptionMapper<UnavailableServiceException> {
    @Override
    public Response toResponse(UnavailableServiceException e) {
        System.err.println(
                String.format(e.getErrorCode().getMessage(), e.getService().getServiceID())
        );

        return Response
                .status(e.getErrorCode().getResponseStatus())
                .entity(new ErrorMessageResponse(
                                e.getErrorCode().getCode(),
                                String.format(e.getErrorCode().getMessage(), e.getService().getServiceID()))
                ).build();
    }
}
