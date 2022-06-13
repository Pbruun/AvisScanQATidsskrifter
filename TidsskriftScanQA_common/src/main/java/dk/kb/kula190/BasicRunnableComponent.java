package dk.kb.kula190;

import dk.kb.kula190.iterators.common.TreeIterator;
import dk.kb.kula190.iterators.eventhandlers.EventRunner;
import dk.kb.kula190.iterators.eventhandlers.TreeEventHandler;
import dk.kb.kula190.iterators.filesystem.SimpleIteratorForFilesystems;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class BasicRunnableComponent {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(BasicRunnableComponent.class);
    
    private final TriFunction<ResultCollector, List<TreeEventHandler>, TreeIterator, EventRunner> eventRunnerFactory;
    
    private final Function<ResultCollector, List<TreeEventHandler>> eventHandlerFactory;
    protected final String checksumFile;
    protected final List<String> filesToIgnore;
    
    public BasicRunnableComponent(Function<ResultCollector, List<TreeEventHandler>> eventHandlerFactory,
                                  String checksumFile,
                                  List<String> filesToIgnore) {
        this(eventHandlerFactory, (resultCollector, treeEventHandlers, treeIterator) -> new EventRunner(
                treeIterator,
                treeEventHandlers,
                resultCollector),
             checksumFile,
             filesToIgnore);
        
    }
    
    public BasicRunnableComponent(Function<ResultCollector, List<TreeEventHandler>> eventHandlerFactory,
                                  TriFunction<ResultCollector, List<TreeEventHandler>, TreeIterator, EventRunner> eventRunnerFactory,
                                  String checksumFile,
                                  List<String> filesToIgnore) {
        this.eventHandlerFactory = eventHandlerFactory;
        this.eventRunnerFactory  = eventRunnerFactory;
        this.checksumFile = checksumFile;
        this.filesToIgnore = filesToIgnore;
    }
    
    
    
    public void doWorkOnItem(Batch batch, ResultCollector resultCollector) throws IOException {
        log.info("Starting validation of '{}'", batch.getFullID());
        
        List<TreeEventHandler> eventHandlers = eventHandlerFactory.apply(resultCollector);
        
        TreeIterator iterator = getIterator(batch.getLocation());
        
        EventRunner runner = eventRunnerFactory.apply(resultCollector, eventHandlers, iterator);
        runner.run();
        
    }
    
    
    protected TreeIterator getIterator(Path pathname) throws IOException {
        File specificBatch = pathname.toFile();
        TreeIterator iterator = new SimpleIteratorForFilesystems(specificBatch,
                                                                 //How to adapt the filename for the checksum extension below
                                                                 checksumFile,
                                                                 filesToIgnore);
        
        return iterator;
        
    }
    
    
}
