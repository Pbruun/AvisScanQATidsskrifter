package dk.kb.kula190;

import dk.kb.kula190.generated.Details;
import dk.kb.kula190.generated.Failure;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.generated.Failures;
import dk.kb.kula190.generated.ObjectFactory;
import dk.kb.kula190.generated.Reference;
import dk.kb.kula190.generated.Result;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedParsingEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

public class ResultCollector {
    
    
    private static Logger log = org.slf4j.LoggerFactory.getLogger(ResultCollector.class);
    private Result resultStructure;
    
    private boolean preservable = true;
    
    private Integer maxResults;
    private int resultCount;
    private Failure lastFailure;
    
    /**
     * @param tool
     * @param version
     * @param maxResults The maximum number of results to collect. If null then unlimited results may be collected.
     */
    public ResultCollector(String tool, String version, Integer maxResults) {
        resultStructure = new ObjectFactory().createResult();
        setSuccess(true);
        resultStructure.setFailures(new Failures());
        resultStructure.setTool(tool);
        resultStructure.setVersion(version);
        setTimestamp(new Date());
        this.maxResults = maxResults;
        resultCount     = 0;
    }
    
    /**
     * This method is marked as deprecated. Use the alternative constructor where the maximum number of allowable
     * results is explicitly specified.
     *
     * @param tool
     * @param version
     */
    @Deprecated
    public ResultCollector(String tool, String version) {
        this(tool, version, null);
    }
    
    
    /**
     * Merge the failures from this ResultCollector into the given result collector. The maxResults specified in the
     * "that" argument is respected.
     *
     * @param that the result collector to merge into
     * @return that
     */
    public ResultCollector mergeInto(ResultCollector that) {
        for (Failure failure : getFailures()) {
            that.addFailure(failure);
        }
        if (that.getTimestamp().before(this.getTimestamp())) {
            that.setTimestamp(this.getTimestamp());
        }
        return that;
    }
    
    
    /**
     * This flag controls whether or not the result collecter contains results that should be preserved. Default
     * true. If set to false, the result will not be preserved and thus the event will never have happened in
     * the event framework
     */
    public boolean isPreservable() {
        return preservable;
    }
    
    /**
     * This flag controls whether or not the result collecter contains results that should be preserved. Default
     * true. If set to false, the result will not be preserved and thus the event will never have happened in
     * the event framework
     */
    public void setPreservable(boolean preservable) {
        this.preservable = preservable;
    }
    
    /**
     * Get the success value of the execution
     *
     * @return the success
     */
    public boolean isSuccess() {
        return "Success".equals(resultStructure.getOutcome());
    }
    
    /**
     * Set the success Value of the execution
     *
     * @param success the sucesss
     */
    public void setSuccess(boolean success) {
        resultStructure.setOutcome(success ? "Success" : "Failure");
    }
    
    
    protected void addFailure(Failure failure) {
        resultStructure.getFailures().getFailure().add(failure);
        setSuccess(false);
    }
    
    public void addExceptionalFailure(Exception e){
        log.warn("Exception failure", e);
        Failure failure = new Failure();
        failure.setType(FailureType.EXCEPTION);
        failure.setReference(null);
        failure.setFilereference(null);
        //TODO set the other details here
        failure.setDescription(ExceptionUtils.getMessage(e));
        Details details = new Details();
        details.getContent().addAll(Arrays.asList(ExceptionUtils.getStackFrames(e)));
        failure.setDetails(details);
        addFailure(failure);
        
    }
    
    /**
     * Add a specific failure to the result collector. All these parameters must be non-null and non-empty
     *
     * @param reference   the reference to the file/object that caused the failure
     * @param type        the type of failure
     * @param component   the component that failed
     * @param description Description of the failure.
     */
    public void addFailure(ParsingEvent reference, FailureType type, String component, String description) {
        addFailure(reference, type, component, description, new String[]{});
        setSuccess(false);
    }
    
    /**
     * Add a specific failure to the result collector. All these parameters, except the last, must be non-null and
     * non-empty
     *
     * @param fileReference the reference to the file/object that caused the failure
     * @param type          the type of failure
     * @param component     the component that failed
     * @param description   Description of the failure.
     * @param details       additional details, can be null
     */
    public synchronized void addFailure(ParsingEvent fileReference,
                                        FailureType type,
                                        String component,
                                        String description,
                                        String... details) {
        resultCount++; //The count of the current failure, starting at 1.
        
        Failure failure = new Failure();
        
        failure.setFilereference(fileReference.getName());
        
        if (fileReference instanceof DecoratedParsingEvent decoratedParsingEvent) {
            Reference reference = createSpecificReference(decoratedParsingEvent);
            failure.setReference(reference);
        }
        
        failure.setType(type);
        failure.setComponent(component);
        failure.setDescription(description);
        
        if (details.length > 0) {
            Details xmlDetails = new Details();
            xmlDetails.getContent().add(String.join("\n", details));
            failure.setDetails(xmlDetails);
        }
        if (maxResults == null || resultCount < maxResults) {
            resultStructure.getFailures().getFailure().add(failure);
        } else {
            Details currentDetails = failure.getDetails();
            if (currentDetails == null) {
                currentDetails = new Details();
            }
            currentDetails.getContent().add(0, description);
            failure.setDetails(currentDetails);
            failure.setDescription("The number of results ("
                                   + resultCount
                                   + ") exceeded the maximum number that can be"
                                   +
                                   " collected ("
                                   + maxResults
                                   + ").");
            lastFailure = failure;
        }
        setSuccess(false);
    }
    
    private Reference createSpecificReference(DecoratedParsingEvent decoratedParsingEvent) {
        Reference reference = new Reference();
        reference.setAvis(decoratedParsingEvent.getAvis());
        reference.setEditionDate(Optional.ofNullable(decoratedParsingEvent.getEditionDate())
                                         .map(LocalDate::toString)
                                         .orElse(null));
        reference.setUdgave(decoratedParsingEvent.getUdgave());
        reference.setSectionName(decoratedParsingEvent.getSectionName());
        reference.setPageNumber(decoratedParsingEvent.getPageNumber());
        
        return reference;
    }
    
    
    /**
     * Get the list of failures. This method is only meant to be used for merging purposes
     *
     * @return the failures
     */
    public List<Failure> getFailures() {
        return Collections.unmodifiableList(
                resultStructure.getFailures().getFailure());
    }
    
    /**
     * Return the report as xml
     */
    public String toReport() {
        if (lastFailure != null) {
            resultStructure.getFailures().getFailure().add(lastFailure);
        }
        try {
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter writer = new StringWriter();
            marshaller.marshal(resultStructure, writer);
            return writer.toString();
        } catch (JAXBException e) {
            return null;
        }
    }
    
    /**
     * The timestamp of the event
     *
     * @return
     */
    public Date getTimestamp() {
        return resultStructure.getDate().toGregorianCalendar().getTime();
    }
    
    /**
     * Timestamp the event that this is the result of
     *
     * @param timestamp
     */
    public void setTimestamp(Date timestamp) {
        resultStructure.setDate(format(timestamp));
    }
    
    public void setDuration(long durationInMS) {
        try {
            resultStructure.setDuration(DatatypeFactory.newInstance().newDuration(durationInMS));
        } catch (DatatypeConfigurationException e) {
            throw new Error(e);
        }
    }
    
    
    private XMLGregorianCalendar format(Date date) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            throw new Error(e);
        }
        return date2;
    }
}
