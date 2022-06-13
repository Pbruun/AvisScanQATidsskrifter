package dk.kb.avischk.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    
    
    // file.delete does not work for a directory unless it is empty. hence this method
    protected static void doDelete(File path) {
        if (path.isDirectory()) {
            for (File child : path.listFiles()) {
                doDelete(child);
            }
        }
        if (!path.delete()) {
            log.info("Could not delete " + path);
        }
    }
    
    /**
     * Multi protocol resource loader. Primary attempt is direct file, secondary is classpath resolved to File.
     *
     * @param resource a generic resource.
     * @return a File pointing to the resource.
     */
    protected static File getFile(String resource) throws IOException {
        File directFile = new File(resource);
        if (directFile.exists()) {
            return directFile;
        }
        URL classLoader = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (classLoader == null) {
            throw new FileNotFoundException("Unable to locate '" + resource + "' as direct File or on classpath");
        }
        String fromURL = classLoader.getFile();
        if (fromURL == null || fromURL.isEmpty()) {
            throw new FileNotFoundException("Unable to convert URL '" + fromURL + "' to File");
        }
        return new File(fromURL);
    }
}
