package luminis.whisky.command;

import luminis.whisky.core.consul.exception.ResponseProvidingException;
import luminis.whisky.domain.ErrorMessageResponse;
import luminis.whisky.resources.exception.ErrorCode;

import javax.ws.rs.core.Response;

public class ThreadInterruptedException extends ResponseProvidingException {
    public ThreadInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorCode getErrorCode() {
        return ErrorCode.TIE;
    }

    @Override
    public Response buildResponse() {
        return Response
                .status(this.getErrorCode().getResponseStatus())
                .entity(new ErrorMessageResponse(
                                this.getErrorCode().getCode(),
                                String.format(this.getErrorCode().getMessage(), this.getMessage())
                        )
                ).build();
    }
}
