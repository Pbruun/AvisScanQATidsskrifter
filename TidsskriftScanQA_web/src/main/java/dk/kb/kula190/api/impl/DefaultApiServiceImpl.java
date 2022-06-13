package dk.kb.kula190.api.impl;

import dk.kb.avischk.qa.web.ContentLocationResolver;
import dk.kb.kula190.JsonYamlUtils;
import dk.kb.kula190.api.DefaultApi;
import dk.kb.kula190.dao.DAOFailureException;
import dk.kb.kula190.dao.DaoUtils;
import dk.kb.kula190.dao.NewspaperQADao;
import dk.kb.kula190.model.*;
import dk.kb.kula190.webservice.ServiceExceptionMapper;
import dk.kb.kula190.webservice.exception.InternalServiceException;
import dk.kb.kula190.webservice.exception.InvalidArgumentServiceException;
import dk.kb.kula190.webservice.exception.NotFoundServiceException;
import dk.kb.kula190.webservice.exception.ServiceException;
import dk.kb.util.yaml.YAML;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.security.Principal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * AvisScanQA_web
 *
 * <p>This pom can be inherited by projects wishing to integrate to the SBForge development platform.
 */
public class DefaultApiServiceImpl implements DefaultApi {
    
    private Logger log = LoggerFactory.getLogger(this.toString());
    
    
    
    /* How to access the various web contexts. See https://cxf.apache.org/docs/jax-rs-basics.html#JAX-RSBasics-Contextannotations */
    
    @Context
    private transient UriInfo uriInfo;
    
    @Context
    private transient SecurityContext securityContext;
    
    @Context
    private transient HttpHeaders httpHeaders;
    
    @Context
    private transient Providers providers;
    
    @Context
    private transient Request request;
    
    @Context
    private transient ContextResolver<?> contextResolver;
    
    @Context
    private transient HttpServletRequest httpServletRequest;
    
    @Context
    private transient HttpServletResponse httpServletResponse;
    
    @Context
    private transient ServletContext servletContext;
    
    @Context
    private transient ServletConfig servletConfig;
    
    @Context
    private transient MessageContext messageContext;
    
    @Context
    private transient Application application;
    
    @Context
    private transient Configuration configuration;
    
    private dk.kb.kula190.webservice.Application getApplication() {
        return (dk.kb.kula190.webservice.Application) application;
    }
    
    private String getBatchesFolder() {
        return getApplication().getBatchesFolder();
    }
    
    private NewspaperQADao getDAO() {
        return getApplication().getDao();
    }
    
    @Override
    public List<NewspaperID> getNewspaperIDs() {
        try {
            return getDAO().getNewspaperIDs();

        } catch (DAOFailureException e) {
            log.error("Could not get newspaper IDs from backend");
            throw handleException(e);
        }
    }

    @Override public List<Note> getNewspaperNotes(String avisID) {
        try {
            return getDAO().getNewspaperNotes(avisID);
        } catch (DAOFailureException e) {
            log.error("Could not retrieve notes for '{}'",
                      avisID, e);
            throw handleException(e);
        }
    }

    @Override public Integer getNoteCount(String newspaperID, String batchID, String date) {
        try {
            return getDAO().getNoteCount(newspaperID, batchID, date);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<String> getYearsForNewspaper(String newspaperID) {
        try {
            List<String> years = getDAO().getYearsForNewspaperID(newspaperID);
            return years;
        } catch (DAOFailureException e) {
            log.error("Could not get dates for newspaper ID {}", newspaperID);
            throw handleException(e);
        }
    }

    @Override
    public void removeNote(String batchID, Integer id) {
        try {
            getDAO().removeNotes(id);
        }catch (DAOFailureException e) {
            log.error("Could not delete notes for '{}' '{}'",
                      batchID,id, e);
            throw handleException(e);
        }
    }

    @Override public void removeNotes(String avisID, Integer id) {
        try {
            getDAO().removeNotes(id);
        }catch (DAOFailureException e) {
            log.error("Could not delete notes for '{}' '{}'",
                      avisID,id, e);
            throw handleException(e);
        }

    }

    @Override
    public void setNewspaperNotes(String avis,
                                  String date,
                                  String body,
                                  String batchID,
                                  String edition,
                                  String section,
                                  String page) {
        //        Note that null values might have the value "null". Regard this as null
        log.info("{}/{}/{}/{}/{}/{}, {}", batchID, avis, date, edition, section, page, body);
        Principal loggedInUser = securityContext.getUserPrincipal();
        String username = loggedInUser.getName();
        try {
            getDAO().setNotes(DaoUtils.nullable(batchID),
                              DaoUtils.nullableDate(date),
                              DaoUtils.nullable(body),
                              avis,
                              DaoUtils.nullable(edition),
                              DaoUtils.nullable(section),
                              DaoUtils.nullableInteger(page),
                              username);
        } catch (DAOFailureException e) {
            log.error("Could not store notes for {}/{}/{}/{}/{}/{}, '{}'",
                      batchID,
                      avis,
                      date,
                      edition,
                      section,
                      page,
                      body);
            throw handleException(e);
        }
    }

    @Override
    public List<Note> getNotes(String batchID) {
        try {
            return getDAO().getNotes(batchID);
        } catch (DAOFailureException e) {
            log.error("Could not retrieve notes for '{}'",
                      batchID, e);
            throw handleException(e);
        }
        
    }
    
    @Override
    public Object getConfig() {
        try {
            YAML subConfigMap = getApplication().getConfig()
                                                .getSubMap(
                                                        "avischk-web-qa.webserviceConfig",
                                                        false);
            return JsonYamlUtils.yaml2Json(JsonYamlUtils.yamlToString(subConfigMap));
        } catch (IOException e){
            log.error("Failed to handle webapp json config",e);
            handleException(e);
        }
        return null;
    }
    @Override
    public StreamingOutput getTiffFile(String relPath,String batchID) {
        return output -> {
            Path batchesFolder = new File(getBatchesFolder()).toPath().toAbsolutePath().normalize();
            Path file = batchesFolder.resolve(batchID+"/"+relPath).toAbsolutePath();
            if (file.startsWith(batchesFolder)) {
                httpServletResponse.setHeader("Content-disposition",
                                              "inline; filename=\"" + file.getFileName().toString() + "\"");
        
                try (InputStream buffer = IOUtils.buffer(new FileInputStream(file.toFile()))) {
                    IOUtils.copy(buffer, output);
                } catch (FileNotFoundException e) {
                    throw new NotFoundServiceException("File '" + file + "' not found on system", e);
                }
            } else {
                throw new InvalidArgumentServiceException("File '" + file + "' does not work with " + batchesFolder);
            }
        };
    }
    
    
    @Override
    public void setNotes(String batchID,
                         String date,
                         String body,
                         String avis,
                         String edition,
                         String section,
                         String page) {
        //        Note that null values might have the value "null". Regard this as null
        log.info("{}/{}/{}/{}/{}/{}, {}", batchID, avis, date, edition, section, page, body);
        Principal loggedInUser = securityContext.getUserPrincipal();
        String username = loggedInUser.getName();
        try {
            getDAO().setNotes(batchID,
                              DaoUtils.nullableDate(date),
                              DaoUtils.nullable(body),
                              DaoUtils.nullable(avis),
                              DaoUtils.nullable(edition),
                              DaoUtils.nullable(section),
                              DaoUtils.nullableInteger(page),
                              username);
        } catch (DAOFailureException e) {
            log.error("Could not store notes for {}/{}/{}/{}/{}/{}, '{}'",
                      batchID,
                      avis,
                      date,
                      edition,
                      section,
                      page,
                      body);
            throw handleException(e);
        }
    }
    
    @Override
    public void setState(String batchID, String state) {
        //        Note that null values might have the value "null". Regard this as null
        log.info("{}/{}", batchID, state);
        String username = securityContext.getUserPrincipal().getName();
        try {
            getDAO().setState(batchID,
                              DaoUtils.nullable(state),
                              username);
        } catch (DAOFailureException e) {
            log.error("Could not store notes for {}/{}",
                      batchID,
                      state
                     );
            throw handleException(e);
        }
    }
    
    @Override
    public List<SlimBatch> getBatches() {
        try {
            List<SlimBatch> IDs = getDAO().getBatchIDs();
            return IDs;
        } catch (DAOFailureException e) {
            log.error("Could not get newspaper IDs from backend");
            throw handleException(e);
        }
    }
    
    @Override
    public Batch getBatch(String batchID) {
        try {
            return getDAO().getBatch(batchID);
        } catch (DAOFailureException e) {
            throw handleException(e);
        }
    }
    
    @Override
    public List<NewspaperDate> getBatchDatesForNewspaper(String batchID, String year) {
        try {
            List<NewspaperDate> dates = getDAO().getDatesForBatchID(batchID, year);
            return dates;
        } catch (DAOFailureException e) {
            log.error("Could not get dates for batch ID {}", batchID);
            throw handleException(e);
        }
        
    }
    
    
    @Override
    public NewspaperDay getNewspaperDay(String batchID, String newspaperID, String date) {
        try {
            NewspaperDay entities = getDAO().getNewspaperEditions(batchID,
                                                                  newspaperID,
                                                                  LocalDate.parse(date),
                                                                  getBatchesFolder());
            return entities;
        } catch (DAOFailureException e) {
            log.error("Could not get entities for date {} for newspaper ID {}", date, newspaperID);
            throw handleException(e);
        }
        
    }
    
    @Override
    public List<NewspaperDate> getDatesForNewspaperYear(String newspaperID, String year) {
        try {
            List<NewspaperDate> dates = getDAO().getDatesForNewspaperID(newspaperID, year);
            return dates;
        } catch (DAOFailureException e) {
            log.error("Could not get dates for newspaper ID {}", newspaperID);
            throw handleException(e);
        }
    }
    
    //This is used for the content display, which we do not need any more
    @Override
    public List<CharacterizationInfo> getEntityCharacterization(Long handle) {
        try {
            List<CharacterizationInfo> characterisations = getDAO().getCharacterizationForEntity(handle);
            return characterisations;
        } catch (DAOFailureException e) {
            log.error("Could not get characterisation for newspaper with handle {}", handle);
            throw handleException(e);
        }
    }
 
    //This is used for the content display, which we do not need any more
    @Override
    public URI getEntityURL(Long handle, String type) {
        String relPath;
        try {
            relPath = getDAO().getOrigRelPath(handle);
            
        } catch (DAOFailureException e) {
            log.error("Could not get relative path for handle {}", handle);
            throw handleException(e);
        }
        try {
            return URI.create(ContentLocationResolver.getContent(relPath, type));
        } catch (FileNotFoundException e) {
            log.error("Could not find file {} in filesystem", relPath);
            throw handleException(e);
        }
    }
    
    
    /**
     * This method simply converts any Exception into a Service exception
     *
     * @param e: Any kind of exception
     * @return A ServiceException
     * @see ServiceExceptionMapper
     */
    private ServiceException handleException(Exception e) {
        if (e instanceof ServiceException) {
            return (ServiceException) e; // Do nothing - this is a declared ServiceException from within module.
        } else {// Unforseen exception (should not happen). Wrap in internal service exception
            log.error("ServiceException(HTTP 500):", e); //You probably want to log this.
            return new InternalServiceException(e.getMessage());
        }
    }
    
}
