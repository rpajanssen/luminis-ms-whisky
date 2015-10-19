package luminis.whisky.resources;

import luminis.whisky.domain.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class IllegalStateExceptionHandler implements ExceptionMapper<IllegalStateException>{
    @Override
    public Response toResponse(IllegalStateException e) {
        return Response.status(500).entity(new ErrorMessage(500, e.getMessage())).build();
    }
}
