package nci.advanced_programming;

/**
 * Custom exception thrown when there is an inconsistency in client data or operations.
 * This exception is used to handle situations where client requests or data 
 * violate expected constraints or business rules in the Event Board application.
 */

 
//Author: André Pont De Anda
//Student ID: x23164034
public class ClientInconsistencyException extends Exception {
    
    public ClientInconsistencyException() {
        super();
    }
    
    public ClientInconsistencyException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ClientInconsistencyException with the specified detail message and cause.
     * 
     * @param message the detail message explaining the inconsistency
     * @param cause the cause of the exception
     */
    public ClientInconsistencyException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new ClientInconsistencyException with the specified cause.
     * 
     * @param cause the cause of the exception
     */
    public ClientInconsistencyException(Throwable cause) {
        super(cause);
    }
}
