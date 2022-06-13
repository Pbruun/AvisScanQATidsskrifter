package dk.kb.kula190.iterators.filesystem.transparent;

import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.DelegatingTreeIterator;
import dk.kb.kula190.iterators.filesystem.FileAttributeParsingEvent;
import dk.kb.kula190.iterators.filesystem.SimpleIteratorForFilesystems;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TransparintingFileSystemIterator extends SimpleIteratorForFilesystems {
    
    protected final List<String> virtualLevelsRegexp;
    private final File batchFolder;
    private final List<String> transparentDirNames;
    
    
    /**
     * Construct an iterator rooted at a given directory
     *
     * @param specificBatch    the directory at which to root the iterator.
     * @param folderForBatches The directory containing the batch folders
     */
    public TransparintingFileSystemIterator(File specificBatch,
                                            File folderForBatches,
                                            List<String> transparentDirNames,
                                            List<String> virtualLevelsRegexp,
                                            String checksumFile,
                                            List<String> filesToIgnore) throws IOException {
        super(specificBatch, checksumFile, filesToIgnore);
        this.batchFolder         = folderForBatches;
        this.transparentDirNames = transparentDirNames;
        this.virtualLevelsRegexp = virtualLevelsRegexp;
        
    }
    
    protected TransparintingFileSystemIterator(File specificBatch,
                                            File folderForBatches,
                                            List<String> transparentDirNames,
                                            List<String> virtualLevelsRegexp,
                                            String checksumFile,
                                            Map<String, String> checksums,
                                               List<String> filesToIgnore
                                            ) {
        super(specificBatch, checksumFile, checksums, filesToIgnore);
        this.batchFolder         = folderForBatches;
        this.transparentDirNames = transparentDirNames;
        this.virtualLevelsRegexp = virtualLevelsRegexp;
        
    }
    
    
    @Override
    protected Iterator<? extends DelegatingTreeIterator> initializeChildrenIterator() {
        
        ArrayList<DelegatingTreeIterator> result = new ArrayList<>();
        
        Arrays.stream(Objects.requireNonNull(id.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)))
              .sorted()
              .toList()
              .stream()
              .filter(subdir -> !transparentDirNames.contains(subdir.getName()))
              .map(subdir -> new TransparintingFileSystemIterator(subdir,
                                                                  batchFolder,
                                                                  transparentDirNames,
                                                                  virtualLevelsRegexp,
                                                                  checksumFile,
                                                                  checksums,
                                                                  filesToIgnore
                                                                  ))
              .forEach(result::add);
        
        
        Map<String, List<File>>
                groups
                = getGroups(Arrays.stream(Objects.requireNonNull(id.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)))
                                  .sorted()
                                  .toList()
                                  .stream()
                                  .filter(subdir -> transparentDirNames.contains(subdir.getName()))
                                  .collect(Collectors.toList()));
        groups.keySet()
              .stream()
              .sorted()
              .map(key -> new TransparintingFileIterator(new File(id, key),
                                                         key,
                                                         groups.get(key),
                                                         batchFolder,
                                                         transparentDirNames,
                                                         virtualLevelsRegexp.subList(1,
                                                                                     virtualLevelsRegexp.size()),
                                                         checksumFile,
                                                         checksums,
                                                         filesToIgnore
              ))
              .forEach(result::add);
        return result.iterator();
        
    }
    
    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        Map<String, List<File>> groups = getGroups(id);
        return groups.values()
                     .stream()
                     .filter((List<File> coll) -> coll.size() == 1)
                     .flatMap(Collection::stream)
                     .iterator();
        
    }
    
    private Map<String, List<File>> getGroups(File... dirs) {
        return getGroups(Arrays.stream(dirs).collect(Collectors.toList()));
    }
    
    private Map<String, List<File>> getGroups(List<File> dirs) {
        return dirs.stream()
                   .flatMap(dir -> FileUtils.listFiles(dir,
                                                       new AndFileFilter(FileFileFilter.INSTANCE,
                                                                         new NotFileFilter(new NameFileFilter(filesToIgnore))),
                                                       new NameFileFilter(transparentDirNames))
                                            .stream())
                   .sorted()
                   .collect(Collectors.groupingBy(this::getPrefix));
    }
    
    protected String getPrefix(File file) {
        if (virtualLevelsRegexp.isEmpty()) {
            return file.getName();
        }
        String regex = virtualLevelsRegexp.get(0);
        String[] split = file.getName().split(regex);
        return split[0];
    }
    
    
    public String toPathID(File id) {
        String pathId = id.getAbsolutePath()
                          .replaceFirst(Pattern.quote(batchFolder.getAbsolutePath() + File.separator), "");
        for (String transparentDirName : transparentDirNames) {
            pathId = pathId.replaceAll(File.separator + transparentDirName + File.separator, File.separator);
        }
        return pathId;
    }
    
    @Override
    protected AttributeParsingEvent makeAttributeEvent(File nodeID, File attributeID) {
        String name = toPathID(attributeID);
        String checksum = checksums.get(attributeID.toString());
        return new FileAttributeParsingEvent(name, attributeID, checksum);
    }
    
    @Override
    protected String getIdOfNode() {
        return toPathID(id);
    }
    
    @Override
    public String toString() {
        return "TransparintingFileSystemIterator{" +
               "id=" + getIdOfNode() +
               '}';
    }
}
