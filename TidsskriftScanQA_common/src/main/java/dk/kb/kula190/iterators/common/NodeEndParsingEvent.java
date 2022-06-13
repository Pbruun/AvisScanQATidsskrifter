package dk.kb.kula190.iterators.common;

/**
 * This event represents the iterator leaving a node. It is given when the iterator is finished processing all
 * attributes
 * and subtrees from the current node, just before leaving it.
 */
public class NodeEndParsingEvent extends NodeParsingEvent {
    
    
    public NodeEndParsingEvent(String name) {
        super(name, ParsingEventType.NodeEnd, null);
    }
    
    public NodeEndParsingEvent(String name, String location) {
        super(name, ParsingEventType.NodeEnd, location);
    }
    
    
}
