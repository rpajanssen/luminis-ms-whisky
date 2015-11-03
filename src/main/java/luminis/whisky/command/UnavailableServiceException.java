package luminis.whisky.command;

import luminis.whisky.core.consul.exception.ResponseProvidingException;
import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.resources.exception.ErrorCode;
import luminis.whisky.util.Service;

import javax.ws.rs.core.Response;

public class UnavailableServiceException extends ResponseProvidingException {
    private final Service service;
    private final String message;

    public UnavailableServiceException(Service service, String message) {
        this.service = service;
        this.message = message;
    }

    public Service getService() {
        return service;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ErrorCode getErrorCode() {
        return ErrorCode.US;
    }

    @Override
    public Response buildResponse() {
        return Response
                .status(this.getErrorCode().getResponseStatus())
                .entity(new ErrorMessageResponse(
                                this.getErrorCode().getCode(),
                                String.format(this.getErrorCode().getMessage(), this.getService().getServiceID()))
                ).build();
    }
}
