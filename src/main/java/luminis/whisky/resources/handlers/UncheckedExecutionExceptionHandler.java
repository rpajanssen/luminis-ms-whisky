package luminis.whisky.resources.handlers;

import com.google.common.util.concurrent.UncheckedExecutionException;
import luminis.whisky.core.consul.exception.ResponseProvidingException;
import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.resources.exception.ErrorCode;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UncheckedExecutionExceptionHandler implements ExceptionMapper<UncheckedExecutionException> {
    @Override
    public Response toResponse(UncheckedExecutionException e) {
        if (e.getCause() instanceof ResponseProvidingException) {
            ResponseProvidingException exception = (ResponseProvidingException) e.getCause();
            return exception.buildResponse();
        }

        System.err.println(e.getCause().getMessage());

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorMessageResponse(ErrorCode.UEE.getCode(), e.getCause().getMessage())).build();
    }
}
