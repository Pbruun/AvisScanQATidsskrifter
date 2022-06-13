package dk.kb.kula190.iterators.eventhandlers;


import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.common.TreeIterator;
import org.slf4j.Logger;

import java.util.List;

public class EventRunner implements Runnable {
    
    public static final String EXCEPTION = "exception";
    public static final String UNEXPECTED_ERROR = "Unexpected error: ";
    private static Logger log = org.slf4j.LoggerFactory.getLogger(EventRunner.class);
    protected final List<TreeEventHandler> eventHandlers;
    protected final ResultCollector resultCollector;
    protected TreeIterator iterator;
    private boolean spawn = false;
    
    
    /**
     * Initialise the EventRunner with a tree iterator.
     *
     * @param iterator        The tree iterator to run events on.
     * @param eventHandlers   eventhandler to hande the events encountered
     * @param resultCollector the result collector
     */
    public EventRunner(TreeIterator iterator, List<TreeEventHandler> eventHandlers, ResultCollector resultCollector) {
        this.iterator        = iterator;
        this.eventHandlers   = eventHandlers;
        this.resultCollector = resultCollector;
    }
    
    /**
     * Initialise the EventRunner. This constructor should only be used by the
     * MultithreadEventRunner
     *
     * @param iterator        The tree iterator to run events on.
     * @param eventHandlers   eventhandler to hande the events encountered
     * @param resultCollector the result collector
     * @param spawn           if true, do not run handleFinish
     * @see MultiThreadedEventRunner
     */
    protected EventRunner(TreeIterator iterator,
                          List<TreeEventHandler> eventHandlers,
                          ResultCollector resultCollector,
                          boolean spawn) {
        this(iterator, eventHandlers, resultCollector);
        this.spawn = spawn;
    }
    
    
    /**
     * Trigger all the given event handlers on all events of the iterator.
     */
    public void run() {
        
        try {
            ParsingEvent current = null;
            while (iterator.hasNext()) {
                current = popInjectedEvent();
                if (current == null) {
                    current = iterator.next();
                }
                switch (current.getType()) {
                    case NodeBegin -> {
                        if (current instanceof NodeBeginsParsingEvent) {
                            handleNodeBegins((NodeBeginsParsingEvent) current);
                        }
                    }
                    case NodeEnd -> {
                        if (current instanceof NodeEndParsingEvent) {
                            handleNodeEnd((NodeEndParsingEvent) current);
                        }
                    }
                    case Attribute -> {
                        if (current instanceof AttributeParsingEvent) {
                            handleAttribute((AttributeParsingEvent) current);
                        }
                    }
                }
            }
            if (!spawn) {
                handleFinish();
            }
        } catch (RuntimeException e){
            resultCollector.addExceptionalFailure(e);
        }
    }
    
    /**
     * pop an injected event from the first injecting event handler that has
     * an injected event
     *
     * @return an injected event or null
     */
    public AttributeParsingEvent popInjectedEvent() {
        for (TreeEventHandler eventHandler : eventHandlers) {
            if (eventHandler instanceof InjectingTreeEventHandler) {
                AttributeParsingEvent event = ((InjectingTreeEventHandler) eventHandler).popEvent();
                if (event != null) {
                    return event;
                }
            }
        }
        return null;
    }
    
    public void handleFinish() {
        for (TreeEventHandler handler : eventHandlers) {
            try {
                handler.handleFinish();
            } catch (Exception e) {
                log.error("Caught Exception", e);
                //TODO why can we not get an event for this?
                resultCollector.addFailure(new NodeEndParsingEvent("Finished"),
                                           FailureType.EXCEPTION,
                                           handler.getClass().getSimpleName(),
                                           FailureType.UNEXPECTED_ERROR.name() +"\n"+ e.toString());
            }
        }
    }
    
    public void handleAttribute(AttributeParsingEvent current) {
        for (TreeEventHandler handler : eventHandlers) {
            try {
                handler.handleAttribute(current);
            } catch (Exception e) {
                log.error("Caught Exception", e);
                resultCollector.addFailure(current,
                                           FailureType.EXCEPTION,
                                           handler.getClass().getSimpleName(),
                                           FailureType.UNEXPECTED_ERROR.name() +"\n"+ e.toString());
            }
        }
    }
    
    public void handleNodeEnd(NodeEndParsingEvent current) {
        for (TreeEventHandler handler : eventHandlers) {
            try {
                handler.handleNodeEnd(current);
            } catch (Exception e) {
                log.error("Caught Exception", e);
                resultCollector.addFailure(current,
                                           FailureType.EXCEPTION,
                                           handler.getClass().getSimpleName(),
                                           FailureType.UNEXPECTED_ERROR.name() +"\n"+ e.toString());
            }
        }
    }
    
    public void handleNodeBegins(NodeBeginsParsingEvent current) {
        for (TreeEventHandler handler : eventHandlers) {
            try {
                handler.handleNodeBegin(current);
            } catch (Exception e) {
                log.error("Caught Exception", e);
                resultCollector.addFailure(current,
                                           FailureType.EXCEPTION,
                                           handler.getClass().getSimpleName(),
                                           FailureType.UNEXPECTED_ERROR.name()+"\n"+ e.toString());
            }
        }
    }
}
