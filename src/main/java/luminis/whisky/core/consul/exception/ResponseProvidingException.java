package luminis.whisky.core.consul.exception;


import javax.ws.rs.core.Response;

public abstract class ResponseProvidingException extends RuntimeException {
    public abstract Response buildResponse();

    public ResponseProvidingException () {}

    public ResponseProvidingException(String message, Throwable cause) {
        super(message, cause);
    }
}
