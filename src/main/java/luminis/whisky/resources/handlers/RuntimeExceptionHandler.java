package luminis.whisky.resources.handlers;

import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.resources.exception.ErrorCode;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RuntimeExceptionHandler implements ExceptionMapper<RuntimeException>{
    @Override
    public Response toResponse(RuntimeException e) {
        System.err.println(String.format(ErrorCode.UEE.getMessage(), e.getMessage()));

        e.printStackTrace();

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorMessageResponse(
                                ErrorCode.UEE.getCode(),
                                String.format(ErrorCode.UEE.getMessage(), e.getMessage())
                        )
                ).build();
    }
}
