package luminis.whisky.command;

import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.util.Service;


public class ServiceResultException extends RuntimeException {
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
}
