package luminis.whisky.resources.exception;

import luminis.whisky.domain.OrderReturnResponse;
import luminis.whisky.util.Service;

public class UnableToCancelException extends RuntimeException {
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
}
