package luminis.whisky.command;

import luminis.whisky.resources.exception.ErrorCode;

public class ThreadInterruptedException extends RuntimeException {
    public ThreadInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorCode getErrorCode() {
        return ErrorCode.TIE;
    }
}
