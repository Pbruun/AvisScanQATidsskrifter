package dk.kb.kula190.webservice.exception;

import javax.ws.rs.core.Response;

/*
 * Custom web-exception class (400)
 */
public class InvalidArgumentServiceException extends ServiceException {
    
    //Constant fields for the OpenApi
    public static final String description = "InvalidArgumentServiceException";
    public static final String responseCode = "400";
    
    private static final long serialVersionUID = 27182823L;
    private static final Response.Status responseStatus = Response.Status.BAD_REQUEST; // 400
    
    public InvalidArgumentServiceException() {
        super(responseStatus);
    }
    
    public InvalidArgumentServiceException(String message) {
        super(message, responseStatus);
    }
    
    public InvalidArgumentServiceException(String message, Throwable cause) {
        super(message, cause, responseStatus);
    }
    
    public InvalidArgumentServiceException(Throwable cause) {
        super(cause, responseStatus);
    }
    
    
}
