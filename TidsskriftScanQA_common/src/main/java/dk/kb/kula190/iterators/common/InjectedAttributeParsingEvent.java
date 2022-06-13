package dk.kb.kula190.iterators.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is an implementation of the AttributeParsingEvent. It is meant to be used for injected events from the
 * InjectingTreeEventHandler
 */
public class InjectedAttributeParsingEvent extends AttributeParsingEvent {
    
    
    private final String injectedType;
    private final byte[] data;
    private final String checksum;
    
    /**
     * Constructur
     *  @param name     the event name
     * @param injectedType
     * @param data     the data as a byte array
     * @param checksum the checksum for the data
     */
    public InjectedAttributeParsingEvent(String name, String injectedType, String location, byte[] data, String checksum) {
        super(name, location);
        this.injectedType = injectedType;
        this.data         = data;
        this.checksum     = checksum;
    }
    
    @Override
    public InputStream getData() throws IOException {
        return new ByteArrayInputStream(data);
    }
    
    @Override
    public String getChecksum() throws IOException {
        return checksum;
    }
    
    public String getInjectedType() {
        return injectedType;
    }
}
