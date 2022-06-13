package dk.kb.kula190.webservice;

import dk.kb.kula190.webservice.exception.ServiceException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Locale;

/*
 * Catches {@link ServiceException}s and adjusts the response accordingly.
 */
@Provider
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {
    
    @Override
    public Response toResponse(ServiceException exception) {
        
        Response.Status responseStatus = exception.getResponseStatus();
        Object entity = exception.getEntity();
        
        if (entity != null) {
            return Response.status(responseStatus)
                           .entity(entity)
                           .language(Locale.getDefault())
                           .type(exception.getMimeType())
                           .build();
        } else {
            return Response.status(responseStatus)
                           .language(Locale.getDefault())
                           .type(exception.getMimeType())
                           .build();
        }
        
    }
}
