package luminis.whisky.core.consul;

import luminis.whisky.core.consul.exception.ResponseProvidingException;
import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.resources.exception.ErrorCode;

import javax.ws.rs.core.Response;

public class DyingServiceException extends ResponseProvidingException {
    private final String serviceId;

    public DyingServiceException(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public ErrorCode getErrorCode() {
        return ErrorCode.SNH;
    }

    @Override
    public Response buildResponse() {
        return Response.status(this.getErrorCode().getResponseStatus())
                .entity(
                        new ErrorMessageResponse(
                                this.getErrorCode().getCode(),
                                String.format(this.getErrorCode().getMessage(), this.getServiceId())
                        )
                ).build();
    }
}
