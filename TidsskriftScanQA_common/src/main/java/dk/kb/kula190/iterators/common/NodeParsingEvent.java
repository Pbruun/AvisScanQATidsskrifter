package dk.kb.kula190.iterators.common;

public class NodeParsingEvent extends ParsingEvent {
    /**
     * Constructor for this class.
     *
     * @param name     The name of this event in the parse tree.
     * @param type     The type of the event.
     * @param location A String specifying location information associated with this event (e.g. a
     *                 a fedora pid or filepath). The interpretation of this parameter is implementation
     */
    public NodeParsingEvent(String name, ParsingEventType type, String location) {
        super(name, type, location);
    }
}
