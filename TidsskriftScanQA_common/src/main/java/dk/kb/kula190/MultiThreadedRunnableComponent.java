package dk.kb.kula190;

import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.MultiThreadedEventRunner;
import dk.kb.kula190.iterators.eventhandlers.TreeEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

public class MultiThreadedRunnableComponent extends DecoratedRunnableComponent {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public MultiThreadedRunnableComponent(ExecutorService executorService, Function<ResultCollector, List<TreeEventHandler>> eventHandlerFactory, String checksumFile, List<String> filesToIgnore) {
        this(executorService, defaultForkCondition(), eventHandlerFactory, checksumFile, filesToIgnore);
    }
    
    
    public MultiThreadedRunnableComponent(ExecutorService executorService,
                                          MultiThreadedEventRunner.EventCondition forkJoinCondition,
                                          Function<ResultCollector, List<TreeEventHandler>> eventHandlerFactory,
                                          String checksumFile, List<String> filesToIgnore
                                         ) {
        super(eventHandlerFactory,
              (resultCollector, treeEventHandlers, treeIterator) -> new MultiThreadedEventRunner(treeIterator,
                                                                                                 treeEventHandlers,
                                                                                                 resultCollector,
                                                                                                 forkJoinCondition,
                                                                                                 executorService),
              checksumFile,
              filesToIgnore
              );
    }
    
    
    public static MultiThreadedEventRunner.EventCondition defaultForkCondition() {
        return new MultiThreadedEventRunner.EventCondition() {
            
            @Override
            public boolean shouldFork(ParsingEvent event) {
                int level1 = event.getName().split("/").length;
                int level2 = new File(event.getName()).getName().split("_").length;
                return level1 == 2 && level2 == 3; //level 2 is editions
            }
            
            @Override
            public boolean shouldJoin(ParsingEvent event) {
                int level = event.getName().split("/").length;
                return level == 1; //level 1 is batch
            }
            //What this means is that we each edition is handled in a new thread,
            // and when we get to the end of the batch, we join back the threads
        };
    }
    
}
