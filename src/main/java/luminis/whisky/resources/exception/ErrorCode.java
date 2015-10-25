package luminis.whisky.resources.exception;

import javax.ws.rs.core.Response;

public enum ErrorCode {
    UTC(Response.Status.INTERNAL_SERVER_ERROR, 4001, "unable to cancel %s for order %s, resulting state was %s"),

    TIE(Response.Status.INTERNAL_SERVER_ERROR, 4010, "thread got interrupted : %s"),

    SNH(Response.Status.SERVICE_UNAVAILABLE, 4101, "service %s unhealthy and not available"),
    US(Response.Status.SERVICE_UNAVAILABLE, 4102, "service %s unavailable"),

    UEE(Response.Status.INTERNAL_SERVER_ERROR, 4501, "unexpected exception occurred : %s ")
    ;

    private final Response.Status responseStatus;
    private final int code;
    private final String message;


    ErrorCode(Response.Status responseStatus, int code, String message) {
        this.responseStatus = responseStatus;
        this.code = code;
        this.message = message;
    }

    public Response.Status getResponseStatus() {
        return responseStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
