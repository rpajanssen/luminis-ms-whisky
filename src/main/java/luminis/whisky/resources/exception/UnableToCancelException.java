package luminis.whisky.resources.exception;

import luminis.whisky.core.consul.exception.ResponseProvidingException;
import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.domain.OrderReturnResponse;
import luminis.whisky.util.Service;

import javax.ws.rs.core.Response;

public class UnableToCancelException extends ResponseProvidingException {
    private final Service service;
    private final OrderReturnResponse returnResponse;

    public UnableToCancelException(Service service, OrderReturnResponse returnResponse) {
        this.service = service;
        this.returnResponse = returnResponse;
    }

    public Service getService() {
        return service;
    }

    public OrderReturnResponse getReturnResponse() {
        return returnResponse;
    }

    @Override
    public Response buildResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(
                        new ErrorMessageResponse(
                                ErrorCode.UTC.getCode(),
                                String.format(ErrorCode.UTC.getMessage(), this.getService().getServiceID(), this.getReturnResponse().getOrderNumber(), this.getReturnResponse().getState())
                        )
                ).build();
    }
}
