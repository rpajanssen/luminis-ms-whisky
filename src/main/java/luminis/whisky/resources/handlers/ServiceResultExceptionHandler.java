package luminis.whisky.resources.handlers;

import luminis.whisky.command.ServiceResultException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * This handler simply passes through the response given by the called service.
 */
@Provider
public class ServiceResultExceptionHandler implements ExceptionMapper<ServiceResultException>{
    @Override
    public Response toResponse(ServiceResultException e) {
        System.err.println(
                String.format("service %s returned not-ok response [%d] : %s ",
                        e.getService(), e.getErrorMessageResponse().getCode(),
                        e.getErrorMessageResponse().getDescription()
                )
        );

        return Response.status(e.getStatus()).entity(e.getErrorMessageResponse()).build();
    }
}
