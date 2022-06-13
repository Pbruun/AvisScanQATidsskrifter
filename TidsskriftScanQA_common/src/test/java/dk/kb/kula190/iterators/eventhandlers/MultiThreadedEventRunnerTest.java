package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.Batch;
import dk.kb.kula190.DecoratedRunnableComponent;
import dk.kb.kula190.MultiThreadedRunnableComponent;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.filecheckers.XmlSchemaChecker;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

class MultiThreadedEventRunnerTest {
    
    
    private final File
            specificBatch
            = new File(System.getenv("HOME") + "/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19061231_RT1");
    
    @Test
    void run() throws Exception {
        
        Path batchPath = specificBatch.toPath().toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
        
        
        DecoratedRunnableComponent component = new MultiThreadedRunnableComponent(
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()),
                resultCollector -> List.of(
                        
                        // new TiffAnalyzerExiv2(resultCollector),
                        // new TiffCheckerExiv2(resultCollector),
                        
                        // new TiffAnalyzerImageMagick(resultCollector),
                        // new TiffCheckerImageMagick(resultCollector)
                        //
                        
                        //Per file- checkers
                        new XmlSchemaChecker(resultCollector)
                        
                        
                        //CrossCheckers
                        // new XpathPageChecker(resultCollector),
                        // new NoMissingMiddlePagesChecker(resultCollector),
                        //  new PageStructureChecker(resultCollector)
                
                
                                          ),
                "checksums.txt",
                List.of("transfer_acknowledged",
                        "transfer_complete",
                        "checksums.txt"));
        
        ResultCollector resultCollector = new ResultCollector(getClass().getSimpleName(),
                                                              getClass().getPackage().getImplementationVersion(), null);
        
        component.doWorkOnItem(batch, resultCollector);
        
        System.out.println(resultCollector.toReport());
        
        
    }
}
