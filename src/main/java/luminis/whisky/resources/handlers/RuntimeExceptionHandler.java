package luminis.whisky.resources.handlers;

import luminis.whisky.domain.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RuntimeExceptionHandler implements ExceptionMapper<RuntimeException>{
    @Override
    public Response toResponse(RuntimeException e) {
        System.err.println(String.format("unexpected exception occurred : %s ", e.getMessage()));

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorMessage(
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                        String.format("Unexpected exception occurred: %s", e.getMessage()
                        )
                )
        ).build();
    }
}
