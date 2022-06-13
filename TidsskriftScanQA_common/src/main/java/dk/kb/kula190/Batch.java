package dk.kb.kula190;

import java.nio.file.Path;

public class Batch {
    
    private String fullID;
    private Path location;
    
    public Batch(String fullID, Path location) {
        this.fullID   = fullID;
        this.location = location;
    }
    
    public String getFullID() {
        return fullID;
    }
    
    public Path getLocation() {
        return location;
    }
    
}
