package luminis.whisky.command;

import luminis.whisky.core.consul.exception.ResponseProvidingException;
import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.util.Service;

import javax.ws.rs.core.Response;

public class ServiceResultException extends ResponseProvidingException {
    private final int status;
    private final ErrorMessageResponse errorMessageResponse;
    private final Service service;

    public ServiceResultException(int status, ErrorMessageResponse errorMessageResponse, Service service) {
        this.status = status;
        this.errorMessageResponse = errorMessageResponse;
        this.service = service;
    }

    public int getStatus() {
        return status;
    }

    public ErrorMessageResponse getErrorMessageResponse() {
        return errorMessageResponse;
    }

    public Service getService() {
        return service;
    }

    @Override
    public Response buildResponse() {
        return Response.status(this.getStatus()).entity(this.getErrorMessageResponse()).build();
    }
}
