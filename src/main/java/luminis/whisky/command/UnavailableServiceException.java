package luminis.whisky.command;

import luminis.whisky.resources.exception.ErrorCode;
import luminis.whisky.util.Service;

public class UnavailableServiceException extends RuntimeException {
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
}
