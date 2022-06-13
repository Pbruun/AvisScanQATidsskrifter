package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.BasicRunnableComponent;
import dk.kb.kula190.Batch;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.filecheckers.ChecksumChecker;
import dk.kb.kula190.checkers.filecheckers.FileNamingChecker;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

class BasicRunnableComponentTest {
    
    
    private final File
            specificBatch
            = new File(System.getenv("HOME") + "/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19060709_RT1");
    
    @Test
    void run() throws Exception {
        
        Path batchPath = specificBatch.toPath().toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
        
        BasicRunnableComponent component =
                new BasicRunnableComponent(
                        r -> List.of(
                                //Simple Checkers
                                new ChecksumChecker(r),
                                new FileNamingChecker(r)
                                    ),
                        "checksums.txt",
                        List.of("transfer_acknowledged", "transfer_complete", "checksums.txt"));
        
        
        ResultCollector resultCollector = new ResultCollector(getClass().getSimpleName(),
                                                              getClass().getPackage().getImplementationVersion(), null);
        
        component.doWorkOnItem(batch, resultCollector);
        
        System.out.println(resultCollector.toReport());
        
    }
}
