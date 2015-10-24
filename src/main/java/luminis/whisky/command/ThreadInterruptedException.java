package luminis.whisky.command;

public class ThreadInterruptedException extends RuntimeException {
    public ThreadInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
