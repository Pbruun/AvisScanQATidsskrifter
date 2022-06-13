package dk.kb.kula190.iterators.filesystem.transparent;

import dk.kb.kula190.iterators.common.DelegatingTreeIterator;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TransparintingFileIterator extends TransparintingFileSystemIterator {
    private final List<File> group;
    private final File batchFolder;
    private final List<String> transparentDirNames;
    protected String prefix;
    
    
    public TransparintingFileIterator(File id,
                                      String prefix,
                                      List<File> group,
                                      File batchFolder,
                                      List<String> transparentDirNames,
                                      List<String> virtualLevelsRegexp,
                                      String checksumFile,
                                      Map<String, String> checksums,
                                      List<String> filesToIgnore) {
        super(id, batchFolder, transparentDirNames, virtualLevelsRegexp, checksumFile, checksums, filesToIgnore);
        this.prefix              = prefix;
        this.group               = group;
        this.batchFolder         = batchFolder;
        this.transparentDirNames = transparentDirNames;
    }
    
    
    @Override
    protected Iterator<? extends DelegatingTreeIterator> initializeChildrenIterator() {
        if (virtualLevelsRegexp.isEmpty()) {
            return Collections.emptyIterator();
        } else {
            Map<String, List<File>> groups = getGroups(group);
            
            //if (groups.size() == 1) { //if only a single group, we do not group
            //    return Collections.emptyIterator();
            //} else {
            List<String> virtualLevelsRegexp = this.virtualLevelsRegexp.subList(1,
                                                                                this.virtualLevelsRegexp.size());
            return groups.entrySet()
                         .stream()
                         .sorted(Map.Entry.comparingByKey())
                         .filter(group -> group.getValue().size() > 1)
                         .map(group -> new TransparintingFileIterator(new File(id.getParentFile(), group.getKey()),
                                                                      prefix + "/" + group.getKey(),
                                                                      group.getValue(),
                                                                      batchFolder,
                                                                      transparentDirNames,
                                                                      virtualLevelsRegexp,
                                                                      checksumFile,
                                                                      checksums,
                                                                      filesToIgnore))
                         .iterator();
            //}
        }
        
    }
    
    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        if (virtualLevelsRegexp.isEmpty()) {
            return group.iterator();
        } else {
            Map<String, List<File>> grouped = getGroups(group);
            
            //if (grouped.size() == 1) { //if all the files are in the same group, do not make this virtual group
            //    return grouped.values().stream().flatMap(Collection::stream).toList().iterator();
            //}
            
            return grouped.values().stream().filter(group -> group.size() == 1).flatMap(Collection::stream).iterator();
        }
    }
    
    
    private Map<String, List<File>> getGroups(List<File> files) {
        return files.stream()
                    .sorted()
                    .collect(Collectors.groupingBy(this::getPrefix));
    }
    
    
    public String toPathID(File file) {
        if (file.isFile()) {
            File folder = file.getParentFile().getAbsoluteFile();
            String pathId = folder.getPath()
                                  .replaceFirst(Pattern.quote(batchFolder.getAbsolutePath() + "/"), "");
            // for (String transparentDirName : transparentDirNames) {
            //     pathId = pathId.replaceAll(Pattern.quote("/" + transparentDirName) + "(/|\\z)", "/");
            // }
            pathId = new File(pathId, file.getName()).toString();
            return pathId;
        } else {
            return super.toPathID(file);
        }
        //return pathId;
    }
    //
    //
    //protected String getPrefix(File file) {
    //    String prefix = file.getName().split(virtualLevelsRegexp.get(0))[0];
    //    return prefix;
    //}
    
    
    @Override
    public String toString() {
        return "TransparintingFileIterator{" +
               "prefix='" + getIdOfNode() + '\'' +
               '}';
    }
}
