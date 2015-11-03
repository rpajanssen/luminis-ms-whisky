package luminis.whisky.resources.handlers;

import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.resources.exception.ErrorCode;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception>{
    @Override
    public Response toResponse(Exception e) {
        System.err.println(String.format(ErrorCode.UEE.getMessage(), e.getMessage()));

        return Response
                .status(ErrorCode.UEE.getResponseStatus())
                .entity(new ErrorMessageResponse(
                                ErrorCode.UEE.getCode(),
                                String.format(ErrorCode.UEE.getMessage(), e.getMessage())
                        )
                ).build();
    }
}
