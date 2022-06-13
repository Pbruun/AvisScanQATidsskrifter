package dk.kb.kula190.iterators.eventhandlers;


import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;

import java.io.IOException;

/**
 * Interface for tree event handlers
 */
public interface TreeEventHandler {
    /**
     * Signifies that a node with nested elements will be processed.
     *
     * @param event Contains information on the node.
     */
    public void handleNodeBegin(NodeBeginsParsingEvent event) throws IOException;
    
    /**
     * Signifies that a node end has been reached.
     *
     * @param event Contains information on the node.
     */
    public void handleNodeEnd(NodeEndParsingEvent event) throws IOException;
    
    /**
     * Signifies that a leaf has been reached.
     *
     * @param event Contains information on leaf.
     */
    public void handleAttribute(AttributeParsingEvent event) throws IOException;
    
    /**
     * Signifies that the parsing of the batch has been is finished, and any crosscutting batch
     * analysis should done.
     */
    public void handleFinish() throws IOException;
}
