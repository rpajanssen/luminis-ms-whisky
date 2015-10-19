package luminis.whisky.resources;

import luminis.whisky.domain.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception>{
    @Override
    public Response toResponse(Exception e) {
        return Response.status(500).entity(new ErrorMessage(500, String.format("Unexpected exception occurred: %s", e.getMessage()))).build();
    }
}
