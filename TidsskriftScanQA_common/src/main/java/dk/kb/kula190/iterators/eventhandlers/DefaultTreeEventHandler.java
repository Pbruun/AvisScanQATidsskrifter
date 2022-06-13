package dk.kb.kula190.iterators.eventhandlers;


import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import org.apache.commons.text.ExtendedMessageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Abstract tree event handler, with no-op methods
 */
public abstract class DefaultTreeEventHandler implements TreeEventHandler {
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private ResultCollector resultCollector;
    
    public DefaultTreeEventHandler(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
    }
    
    public final ResultCollector getResultCollector() {
        return resultCollector;
    }
    
    
    public void handleFinish() throws IOException {
    }
    
    public void handleNodeBegin(NodeBeginsParsingEvent event) throws IOException {
    }
    
    public void handleNodeEnd(NodeEndParsingEvent event) throws IOException {
    }
    
    public void handleAttribute(AttributeParsingEvent event) throws IOException {
    }
    
    
    private String asString(Object object) {
        if (object == null) {
            return "null";
        } else {
            return object.toString();
        }
    }
    
    protected void checkEquals(ParsingEvent event,
                               FailureType type,
                               String description,
                               Object actual,
                               Object expected,
                               Object... extraValues) {
        String actualString = asString(actual);
        String expectedString = asString(expected);
        if (!Objects.equals(actual, expected) && !actualString.equalsIgnoreCase(expectedString)) {
            addFailure(event,
                       type,
                       description.replace("{expected}", expectedString).replace("{actual}", actualString),
                       extraValues);
        }
    }
    
    protected Matcher checkRegExp(ParsingEvent event,
                                  FailureType type,
                                  String description,
                                  String actual,
                                  Pattern expected,
                                  Object... extraValues) {
        String actualString = asString(actual);
        String expectedString = asString(expected);
        Matcher matcher = null;
        if (expected != null) {
            matcher = expected.matcher(actual);
            if (!expected.matcher(actual).matches()) {
                addFailure(event,
                           type,
                           description.replace("{expected}", expectedString).replace("{actual}", actualString),
                           extraValues);
            }
        }
        return matcher;
    }
    
    
    protected <K> void checkAllInSet(ParsingEvent event,
                              FailureType type,
                              String description,
                              Set<K> actual,
                              Set<K> set,
                              Object... extraValues) {
        String actualString = asString(actual);
        String expectedString = asString(set);
        if (set == null || !set.containsAll(actual)) {
            addFailure(event,
                       type,
                       description.replace("{expected}", expectedString).replace("{actual}", actualString),
                       extraValues);
        }
        
    }
    protected void checkInSet(ParsingEvent event,
                              FailureType type,
                              String description,
                              Object actual,
                              Set<?> set,
                              Object... extraValues) {
        String actualString = asString(actual);
        String expectedString = asString(set);
        if (set == null || !set.contains(actual)) {
            addFailure(event,
                       type,
                       description.replace("{expected}", expectedString).replace("{actual}", actualString),
                       extraValues);
        }
        
    }
    
    protected void checkAllEquals(ParsingEvent event,
                                  FailureType type,
                                  String description,
                                  Object... equals
                                 ) {
        if (Arrays.stream(equals).distinct().count() != 1) {
            addFailure(event, type, description, equals);
        }
        
    }
    
    
    protected void checkTrue(ParsingEvent event,
                             FailureType type,
                             String description,
                             boolean bool,
                             Object... extraValues) {
        if (!bool) {
            addFailure(event, type, description, extraValues);
        }
    }
    
    protected void checkAtLeast(ParsingEvent event,
                                FailureType type,
                                Double actual,
                                Double required,
                                String description,
                                Object... extraValues) {
        if (actual < required) {
            addFailure(event,
                       type,
                       description.replace("{required}", asString(required)).replace("{actual}", asString(actual)),
                       extraValues);
        }
    }
    
    protected void checkBetween(ParsingEvent event,
                                FailureType type,
                                LocalDate actual,
                                LocalDate requiredMin,
                                LocalDate requiredMax,
                                String description,
                                Object... extraValues) {
        
        if (actual.isBefore(requiredMin) || actual.isAfter(requiredMax)) {
            addFailure(event,
                       type,
                       description.replace("{requiredMin}", asString(requiredMin))
                                  .replace("{actual}", asString(actual))
                                  .replace("{requiredMax}", asString(requiredMax)),
                       extraValues);
        }
    }
    
    
    protected void checkWithinRange(ParsingEvent event,
                                    FailureType type,
                                    Double actual,
                                    Double requiredMin,
                                    Double requiredMax,
                                    String description,
                                    Object... extraValues) {
        
        if (actual < requiredMin || actual > requiredMax) {
            addFailure(event,
                       type,
                       description.replace("{requiredMin}", asString(requiredMin))
                                  .replace("{actual}", asString(actual))
                                  .replace("{requiredMax}", asString(requiredMax)),
                       extraValues);
        }
    }
    
    public void addFailure(ParsingEvent fileReference,
                           FailureType type,
                           String description,
                           Object... values) {
        log.warn(
                "Adding failure for " +
                "resource '{}' " +
                "of type '{}' " +
                "from component '{}' " +
                "with description '{}' ",
                fileReference,
                type,
                this.getClass().getSimpleName(),
                ExtendedMessageFormat.format(description, values));
        
        getResultCollector().addFailure(fileReference, type, this.getClass().getSimpleName(),
                                        ExtendedMessageFormat.format(description, values));
    }
    
    
    public void addExceptionalFailure(ParsingEvent event, Exception e) {
        getResultCollector().addFailure(event,
                                        FailureType.EXCEPTION,
                                        this.getClass().getSimpleName(),
                                        FailureType.UNEXPECTED_ERROR.name() + "\n" + e,
                                        Arrays.stream(e.getStackTrace())
                                              .map(StackTraceElement::toString)
                                              .collect(Collectors.joining("\n")));
    }
    
}
