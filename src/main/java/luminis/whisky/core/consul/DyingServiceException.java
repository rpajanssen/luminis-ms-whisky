package luminis.whisky.core.consul;

import luminis.whisky.resources.exception.ErrorCode;

public class DyingServiceException extends Exception{
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
}
