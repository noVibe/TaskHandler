package exceptions;

public class PastCallException extends RuntimeException{
    public PastCallException() {
        super("Can't create a task in the past!");
    }
}
