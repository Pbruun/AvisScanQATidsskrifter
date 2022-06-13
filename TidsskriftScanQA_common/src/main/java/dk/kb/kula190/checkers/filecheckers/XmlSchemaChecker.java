package dk.kb.kula190.checkers.filecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

public class XmlSchemaChecker extends DecoratedEventHandler {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(XmlSchemaChecker.class);
    private Validator mixValidator;
    private Validator altoValidator;
    
    private ThreadLocal<ParsingEvent> parsingEvent = new ThreadLocal<>();
    
    public XmlSchemaChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    @Override
    public void batchBegins(DecoratedNodeParsingEvent event,
                            String newspaper,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) {
        try (InputStream schemaStream = Thread.currentThread()
                                              .getContextClassLoader()
                                              .getResourceAsStream("MetadataSchema/AltoSchema.xsd")) {
            altoValidator = setErrorHandler("Alto", schemaStream);
        } catch (IOException | SAXException e) {
            addExceptionalFailure(event, e);
        }
        
        try (InputStream schemaStream = Thread.currentThread()
                                              .getContextClassLoader()
                                              .getResourceAsStream("MetadataSchema/MixSchema.xsd")) {
            mixValidator = setErrorHandler("Alto", schemaStream);
        } catch (IOException | SAXException e) {
            addExceptionalFailure(event, e);
        }
        
    }
    
    @Override
    public synchronized void mixFile(DecoratedAttributeParsingEvent decoratedEvent,
                                     String newspaper,
                                     LocalDate editionDate,
                                     String edition,
                                     String section,
                                     Integer pageNumber) throws IOException {
        parsingEvent.set(decoratedEvent);
        try (InputStream stream = decoratedEvent.getData()) {
            
            mixValidator.validate(new StreamSource(stream));
        } catch (SAXException e) {
            addExceptionalFailure(parsingEvent.get(), e);
        }
    }
    
    @Override
    public synchronized void altoFile(DecoratedAttributeParsingEvent event,
                                      String newspaper,
                                      LocalDate editionDate,
                                      String edition,
                                      String section,
                                      Integer pageNumber) throws IOException {
        parsingEvent.set(event);
        try (InputStream stream = event.getData()) {
            altoValidator.validate(new StreamSource(stream));
        } catch (SAXException e) {
            addExceptionalFailure(parsingEvent.get(), e);
        }
    }
    
    public Validator setErrorHandler(String name, InputStream stream) throws SAXException {
        
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(stream));
        
        Validator validator = schema.newValidator();
        validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                log.warn(name + " warning", exception);
            }
            
            @Override
            public void error(SAXParseException exception) throws SAXException {
                //TODO Give the right Appendic name here
                addFailure(parsingEvent.get(),
                           FailureType.SCHEMA_ERROR,
                           name + " error encountered at line {0}, column {1}: {2}",
                           exception.getLineNumber(),
                           exception.getColumnNumber(),
                           exception.getMessage());
            }
            
            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
              addExceptionalFailure(parsingEvent.get(), exception);
            }
        });
        return validator;
    }
    
    
}
