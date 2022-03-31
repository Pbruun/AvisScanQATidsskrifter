package dk.kb.kula190.cli;

import dk.kb.kula190.BasicRunnableComponent;
import dk.kb.kula190.Batch;
import dk.kb.kula190.DatabaseRegister;
import dk.kb.kula190.DecoratedRunnableComponent;
import dk.kb.kula190.MultiThreadedRunnableComponent;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.batchcheckers.MetsChecker;
import dk.kb.kula190.checkers.batchcheckers.MetsSplitter;
import dk.kb.kula190.checkers.editioncheckers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.checkers.filecheckers.ChecksumChecker;
import dk.kb.kula190.checkers.filecheckers.FileNamingChecker;
import dk.kb.kula190.checkers.filecheckers.XmlSchemaChecker;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffAnalyzerExiv2;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffAnalyzerImageMagick;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffCheckerExiv2;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffCheckerImageMagick;
import dk.kb.kula190.checkers.pagecheckers.PageStructureChecker;
import dk.kb.kula190.checkers.pagecheckers.XpathPageChecker;
import dk.kb.kula190.generated.Failure;
import dk.kb.kula190.iterators.eventhandlers.TreeEventHandler;
import dk.kb.util.yaml.YAML;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class AvisScanQATool {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final boolean persistReport;
    
    public AvisScanQATool(boolean persistReport) {
        this.persistReport = persistReport;
    }
    
    public ResultCollector check(Path batchPath) throws IOException, URISyntaxException {
        
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
        
        ResultCollector resultCollector = new ResultCollector(getClass().getSimpleName(),
                                                              getClass().getPackage().getImplementationVersion(),
                                                              null);
        
        try {
            try {
                
                //Filename checker will cause all other checks to fail...
                
                //Checksum checker might not cause other failures. Checksum errors cause automatic return
                //But we might want to report OTHER errors even when checksums fails...
                
                //Perform basic checks
                final BasicRunnableComponent
                        basicRunnableComponent
                        = new BasicRunnableComponent(r -> List.of(new ChecksumChecker(r), new FileNamingChecker(r)));
                
                basicRunnableComponent.doWorkOnItem(batch, resultCollector);
                
            } catch (Exception e) {
                resultCollector.addExceptionalFailure(e);
            }
            
            //If the basic checks worked, proceed
            if (resultCollector.isSuccess()) {
                
                try {
                    //TODO configurable number of threads
                    DecoratedRunnableComponent
                            component
                            = new MultiThreadedRunnableComponent(Executors.newFixedThreadPool(4), checkerFactory());
                    
                    component.doWorkOnItem(batch, resultCollector);
                    
                } catch (Exception e) {
                    resultCollector.addExceptionalFailure(e);
                }
            } else {
                //TODO what to do if we fail in the first checks??
                log.error("Failed basic checks: \n{}", resultCollector.toReport());
            }
            if (persistReport) {
    
                registerResultInDB(batch, resultCollector);
            }
        } finally {
            System.out.println(resultCollector.toReport());
        }
        return resultCollector;
    }
    
    
    private void registerResultInDB(Batch batch, ResultCollector resultCollector)
            throws IOException, URISyntaxException {
        final List<Failure> failures = resultCollector.getFailures();
        File configFolder = new File(Thread.currentThread()
                                           .getContextClassLoader()
                                           .getResource("dbconfig-behaviour.yaml")
                                           .toURI()).getParentFile();
        YAML dbConfig = YAML.resolveLayeredConfigs(configFolder.getAbsolutePath() + "/dbconfig-*.yaml");
        
        
        DecoratedRunnableComponent databaseComponent = new DecoratedRunnableComponent(r -> List.of(new DatabaseRegister(
                r,
                new Driver(),
                dbConfig.getString("jdbc.jdbc-connection-string"),
                dbConfig.getString("jdbc.jdbc-user"),
                dbConfig.getString("jdbc.jdbc-password"),
                dbConfig.getString("jdbc.jdbc-initial-batch-state"),
                dbConfig.getString("jdbc.jdbc-finished-batch-state"),
                failures)));
        databaseComponent.doWorkOnItem(batch, resultCollector);
        
    }
    
    private Function<ResultCollector, List<TreeEventHandler>> checkerFactory() {
        //TODO ensure this is the current and correct list of checkers
        return r -> List.of(new TiffAnalyzerExiv2(r), new TiffCheckerExiv2(r),
        
                            new TiffAnalyzerImageMagick(r), new TiffCheckerImageMagick(r),
        
                            new MetsSplitter(r), new MetsChecker(r),
        
                            //Per file- checkers
                            new XmlSchemaChecker(r),
        
                            //CrossCheckers
                            new XpathPageChecker(r), new NoMissingMiddlePagesChecker(r), new PageStructureChecker(r));
    }
    
}
