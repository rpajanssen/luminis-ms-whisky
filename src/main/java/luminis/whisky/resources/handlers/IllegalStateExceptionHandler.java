package luminis.whisky.resources.handlers;

import luminis.whisky.domain.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class IllegalStateExceptionHandler implements ExceptionMapper<IllegalStateException>{
    @Override
    public Response toResponse(IllegalStateException e) {
        System.err.println(String.format("unexpected exception occurred : %s ", e.getMessage()));

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage())).build();
    }
}
