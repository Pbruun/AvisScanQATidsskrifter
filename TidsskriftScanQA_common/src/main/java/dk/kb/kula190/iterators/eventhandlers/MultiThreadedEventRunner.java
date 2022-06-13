package dk.kb.kula190.iterators.eventhandlers;


import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.common.TreeIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

//TODO injecting in multithreaded. Current limitations explained:
// Injection works INSIDE one EventRunner
// MultiThreadedEventRunner gets an EventCondition object, which determines when to fork off a new eventrunner and when to join
// The condition we use, forks on each page and joins everything afterwards
// So an InjectingTreeEventHandler's injected event will only be used by other checkers IN THE SAME EventRunner
// Meaning either outside any pages, or inside one specific page.
public class MultiThreadedEventRunner extends EventRunner {
    
    
    public static EventCondition singleThreaded = new EventCondition() {
        @Override
        public boolean shouldFork(ParsingEvent nodeBeginsParsingEvent) {
            return false;
        }
        
        @Override
        public boolean shouldJoin(ParsingEvent nodeEndParsingEvent) {
            return false;
        }
    };
    private final EventCondition forker;
    private final ExecutorService executor;
    private List<Future<?>> childTasks = new ArrayList<>();
    
    public MultiThreadedEventRunner(TreeIterator iterator, List<TreeEventHandler> eventHandlers,
                                    ResultCollector resultCollector, EventCondition forker, ExecutorService executor) {
        super(iterator, eventHandlers, resultCollector);
        this.forker   = forker;
        this.executor = executor;
    }
    
    @Override
    public void handleFinish() {
        super.handleFinish();
    }
    
    @Override
    public void handleNodeBegins(NodeBeginsParsingEvent current) {
        if (forker.shouldFork(current)) {
            //any further will spawn sub iterators
            //Skip to next sibling will branch of the iterator that began with this node begins
            //It will then return than iterator.
            //And the iterator where this was called will skip to the next node begins that was not this tree
            TreeIterator childIterator = iterator.skipToNextSibling();
            EventRunner childRunner = new EventRunner(childIterator, eventHandlers, resultCollector, true);
            Future<?> future = executor.submit(childRunner);
            childTasks.add(future);
        } else {
            super.handleNodeBegins(current);
        }
        
    }
    
    @Override
    public void handleNodeEnd(NodeEndParsingEvent current) {
        
        if (forker.shouldJoin(current)) {
            for (Future<?> childTask : childTasks) {
                try {
                    childTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    resultCollector.addFailure(current,
                                               FailureType.EXCEPTION,
                                               this.getClass().getSimpleName(),
                                               FailureType.UNEXPECTED_ERROR.name() +"\n"+ e.toString(),
                                               Arrays.stream(e.getStackTrace())
                                                     .map(st -> st.toString())
                                                     .collect(Collectors.joining("\n")));
                }
            }
        }
        super.handleNodeEnd(current);
    }
    
    public interface EventCondition {
        public boolean shouldFork(ParsingEvent nodeBeginsParsingEvent);
        
        public boolean shouldJoin(ParsingEvent nodeEndParsingEvent);
    }
    
}
