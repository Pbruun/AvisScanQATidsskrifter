package dk.kb.kula190.iterators.filesystem;


import dk.kb.kula190.iterators.common.AbstractIterator;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.DelegatingTreeIterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Iterator for parsing a tree structure backed by a file system. Each iterator represents a node. A node corresponds
 * to a directory.
 */
public class SimpleIteratorForFilesystems extends AbstractIterator<File> {
    
    
    protected final Map<String, String> checksums;
    protected final String checksumFile;
    protected final List<String> filesToIgnore;
    
    /**
     * Construct an iterator rooted at a given directory
     *
     * @param dir the directory at which to root the iterator.
     */
    public SimpleIteratorForFilesystems(File dir, String checksumFile, List<String> filesToIgnore) throws IOException {
        super(dir);
        this.checksumFile  = checksumFile;
        this.filesToIgnore =filesToIgnore;
        checksums          = Files.readAllLines(dir.toPath().resolve(checksumFile))
                         .stream()
                         .map(line -> line.split("  ", 2))
                         .collect(Collectors.toMap(lineSplits -> new File(dir, lineSplits[1]).toString(),
                                                   lineSplits -> lineSplits[0]));
        
    }
    
    protected SimpleIteratorForFilesystems(File id,
                                           String checksumFile,
                                           Map<String, String> checksums,
                                           List<String> filesToIgnore) {
        super(id);
        this.checksums = checksums;
        this.checksumFile = checksumFile;
        this.filesToIgnore =filesToIgnore;
    }
    
    @Override
    protected Iterator<? extends DelegatingTreeIterator> initializeChildrenIterator() {
        //The id attribute is the id of this node, ie. the File corresponding to the directory
        File[] children = id.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        Arrays.sort(children);
        ArrayList<DelegatingTreeIterator> result = new ArrayList<>(children.length);
        for (File child : children) {
            result.add(new SimpleIteratorForFilesystems(child, checksumFile, checksums, filesToIgnore));
        }
        return result.iterator();
    }
    
    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        List<File> attributes = new ArrayList<>(FileUtils.listFiles(id,
                                                                    new AndFileFilter(FileFileFilter.INSTANCE,
                                                                                      new NotFileFilter(
                                                                                              new NameFileFilter(filesToIgnore))),
                                                                    null));
        Collections.sort(attributes);
        return attributes.iterator();
    }
    
    @Override
    protected AttributeParsingEvent makeAttributeEvent(File nodeID, File attributeID) {
        String checksum = checksums.get(attributeID.toString());
        return new FileAttributeParsingEvent(attributeID.getPath(), attributeID, checksum);
    }
    
    
    /**
     * The name of the directory is used as the Id of the node.
     *
     * @return the name of the directory.
     */
    @Override
    protected String getIdOfNode() {
        return id.getPath();
    }
    
    
}
