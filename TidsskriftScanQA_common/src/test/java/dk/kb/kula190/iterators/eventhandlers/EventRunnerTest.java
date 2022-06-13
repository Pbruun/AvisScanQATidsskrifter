package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.Batch;
import dk.kb.kula190.DecoratedRunnableComponent;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.batchcheckers.PDFAChecker;
import dk.kb.kula190.checkers.batchcheckers.PageArticleChecker;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

class EventRunnerTest {
    
    private final File
            specificBatch
            = new File(System.getenv("HOME") + "/Projects/AvisScanQATidsskrifter/AvisScanQATidsskrifter/data/dl_20220301_rt1_short");
    
    
    @Test
    void run() throws Exception {
        
        Path batchPath = specificBatch.toPath().toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
        
        
        DecoratedRunnableComponent component = new DecoratedRunnableComponent(
                resultCollector -> List.of(
                        // new TiffAnalyzerExiv2(resultCollector),
                        // new TiffCheckerExiv2(resultCollector),
                        // new MetsChecker(resultCollector),
                        new PDFAChecker(resultCollector),
                        new PageArticleChecker(resultCollector)
                        //Per file- checkers
//                        new XmlSchemaChecker(resultCollector),
                        
                        // CrossCheckers

//                        new PageStructureChecker(resultCollector)
                
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
