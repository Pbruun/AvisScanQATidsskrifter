package dk.kb.kula190;

import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.MultiThreadedEventRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

public abstract class MultiThreadedRunnableComponent extends DecoratedRunnableComponent {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public MultiThreadedRunnableComponent(ExecutorService executorService) {
        this(executorService, defaultForkCondition());
    }
    
    
    public MultiThreadedRunnableComponent(ExecutorService executorService,
                                          MultiThreadedEventRunner.EventCondition forkJoinCondition) {
        super((resultCollector, treeEventHandlers, treeIterator) -> new MultiThreadedEventRunner(treeIterator,
                                                                                                 treeEventHandlers,
                                                                                                 resultCollector,
                                                                                                 forkJoinCondition,
                                                                                                 executorService));
    }
    
    
    public static MultiThreadedEventRunner.EventCondition defaultForkCondition() {
        return new MultiThreadedEventRunner.EventCondition() {
            
            @Override
            public boolean shouldFork(ParsingEvent event) {
                int level = event.getName().split("/").length;
                return level == 2; //level 2 is editions
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
