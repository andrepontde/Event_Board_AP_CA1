package nci.advanced_programming;

/**
 * Custom exception thrown when there is an invalid command or inconsistency in client data or operations.
 * This exception is used to handle situations where client requests or data 
 * violate expected constraints or business rules in the Event Board application.
 */

 
//Author: André Pont De Anda
//Student ID: x23164034
public class InvalidCommandException extends Exception {
    
    public InvalidCommandException() {
        super();
    }
    
    public InvalidCommandException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new InvalidCommandException with the specified detail message and cause.
     * 
     * @param message the detail message explaining the inconsistency
     * @param cause the cause of the exception
     */
    public InvalidCommandException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new InvalidCommandException with the specified cause.
     * 
     * @param cause the cause of the exception
     */
    public InvalidCommandException(Throwable cause) {
        super(cause);
    }
}