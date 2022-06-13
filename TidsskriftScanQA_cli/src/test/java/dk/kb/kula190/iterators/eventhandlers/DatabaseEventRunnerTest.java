package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.Batch;
import dk.kb.kula190.DatabaseRegister;
import dk.kb.kula190.DecoratedRunnableComponent;
import dk.kb.kula190.MultiThreadedRunnableComponent;
import dk.kb.kula190.ResultCollector;

import dk.kb.kula190.checkers.filecheckers.XmlSchemaChecker;

import dk.kb.kula190.checkers.pagecheckers.PageStructureChecker;

import dk.kb.kula190.generated.Failure;
import dk.kb.util.yaml.YAML;
import org.junit.jupiter.api.Test;
import org.postgresql.Driver;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

class DatabaseEventRunnerTest {
    
    private final File
            specificBatch
            = new File(System.getenv("HOME") + "/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19060709_RT1");
    
    
    @Test
    void run() throws Exception {
        
        Path batchPath = specificBatch.toPath().toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
        
        ResultCollector resultCollector = new ResultCollector(getClass().getSimpleName(),
                                                              getClass().getPackage().getImplementationVersion(), null);
        
        
        DecoratedRunnableComponent component = new MultiThreadedRunnableComponent(
                Executors.newFixedThreadPool(4),
                r -> {
                    return List.of(
                            //Per file- checkers
                            new XmlSchemaChecker(r),
                            new PageStructureChecker(r)
                    
                    
                                  );
                },
                "checksums.txt",
                List.of("transfer_acknowledged", "transfer_complete"));
        
        
        component.doWorkOnItem(batch, resultCollector);
        
        System.out.println(resultCollector.toReport());
        
        final List<Failure> failures = resultCollector.getFailures();
        File configFolder = new File(Thread.currentThread()
                                           .getContextClassLoader()
                                           .getResource("AvisScanQA_cli-behaviour.yaml")
                                           .toURI()).getParentFile();
        YAML dbConfig = YAML.resolveLayeredConfigs(configFolder.getAbsolutePath() + "/AvisScanQA_cli-*.yaml");
        
        
        DecoratedRunnableComponent databaseComponent = new DecoratedRunnableComponent(
                r -> List.of(new DatabaseRegister(
                                     r,
                                     new Driver(),
                                     dbConfig.getString("jdbc.jdbc-connection-string"),
                                     dbConfig.getString("jdbc.jdbc-user"),
                                     dbConfig.getString("jdbc.jdbc-password"),
                                     dbConfig.getString("states.initial-batch-state"),
                                     dbConfig.getString("states.finished-batch-state"),
                                     "transfer_acknowledged",
                                     failures)
                            ),
                "checksums.txt",
                List.of("transfer_acknowledged", "transfer_complete", "checksums.txt"));
        
        
        databaseComponent.doWorkOnItem(batch, resultCollector);
        
        System.out.println(resultCollector.toReport());
        
        
    }
}
