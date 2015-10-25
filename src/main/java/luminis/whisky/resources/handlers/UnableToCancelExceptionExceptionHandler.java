package luminis.whisky.resources.handlers;

import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.resources.exception.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UnableToCancelExceptionExceptionHandler implements ExceptionMapper<UnableToCancelException> {
    @Override
    public Response toResponse(UnableToCancelException e) {
        System.err.println(
                String.format(ErrorCode.UTC.getMessage(), e.getService().getServiceID(), e.getReturnResponse().getOrderNumber(), e.getReturnResponse().getState())
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(
                        new ErrorMessageResponse(
                                ErrorCode.UTC.getCode(),
                                String.format(ErrorCode.UTC.getMessage(), e.getService().getServiceID(), e.getReturnResponse().getOrderNumber(), e.getReturnResponse().getState())
                        )
                ).build();
    }
}
