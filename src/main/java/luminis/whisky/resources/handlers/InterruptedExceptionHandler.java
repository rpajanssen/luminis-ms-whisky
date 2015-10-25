package luminis.whisky.resources.handlers;

import luminis.whisky.command.ThreadInterruptedException;
import luminis.whisky.domain.ErrorMessageResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InterruptedExceptionHandler implements ExceptionMapper<ThreadInterruptedException>{
    @Override
    public Response toResponse(luminis.whisky.command.ThreadInterruptedException e) {
        System.err.println(String.format(e.getErrorCode().getMessage(), e.getMessage()));

        return Response
                .status(e.getErrorCode().getResponseStatus())
                .entity(new ErrorMessageResponse(
                                e.getErrorCode().getCode(),
                                String.format(e.getErrorCode().getMessage(), e.getMessage())
                        )
                ).build();
    }
}
