package dk.kb.kula190.iterators.filesystem;


import dk.kb.kula190.iterators.common.AttributeParsingEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents a the Parsing Event of encountering a File
 * In this implementation of ParsingEvent, the location field is associated with the
 * absolute filepath to the file being represented.
 */

public class FileAttributeParsingEvent extends AttributeParsingEvent {
    
    
    private final String checksum;
    private final File file;
    private final File checksumFile;
    
    public FileAttributeParsingEvent(String name, File file) {
        super(name, file.getAbsolutePath());
        this.file         = file;
        this.checksumFile = null;
        checksum          = null;
    }
    
    public FileAttributeParsingEvent(String name, File file, String checksum) {
        super(name, file.getAbsolutePath());
        this.file         = file;
        this.checksumFile = null;
        this.checksum = checksum;
    }
    
    
    public FileAttributeParsingEvent(String name, File file, String checksumRegexp, String checksumPostfix) {
        super(name, file.getAbsolutePath());
        this.file         = file;
        this.checksumFile = new File(file.getAbsolutePath().replaceFirst(checksumRegexp, checksumPostfix));
        checksum          = null;
    }
    
    
    @Override
    public InputStream getData() throws IOException {
        //TODO perhaps caching so not read multiple times?
        return new FileInputStream(file);
    }
    
    public File getFile() {
        return file;
    }
    
    @Override
    public String getChecksum() throws IOException {
        if (checksum != null){
            return checksum;
        }
        //TODO caching so not read multiple times
        if (checksumFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(checksumFile))) {
                String firstLine = reader.readLine();
                if (firstLine == null) {
                    return "";
                }
                return firstWord(firstLine).trim().toLowerCase();
            } catch (FileNotFoundException e) {
                throw new IOException(e);
            }
        }
        return null;
    }
    
    private String firstWord(String firstLine) {
        firstLine = firstLine.trim();
        String[] splits = firstLine.split("\\s", 2);
        if (splits.length == 0) {
            return "";
        }
        return splits[0];
    }
    
    
}
