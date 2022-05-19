package dk.kb.kula190.checkers.pagecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class PageStructureChecker extends DecoratedEventHandler {
    private ThreadLocal<Set<String>> types = new ThreadLocal<>();
    
    public PageStructureChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    
    @Override
    public void pageBegins(DecoratedNodeParsingEvent event,
                           String newspaper,
                           LocalDate editionDate,
                           String udgave,
                           String section,
                           Integer pageNumber) {
        //Checkes that each page consists of MIX ALTO TIFF
        types.set(new HashSet<>());
    }
    
    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
                         String newspaper,
                         LocalDate editionDate,
                         String edition,
                         String section,
                         Integer pageNumber) {
        checkEquals(event, FailureType.MISSING_FILE_ERROR,"Appendix H – File structure: Page does not contains all expected file {expected}. Files were {actual}", types.get(), Set.of("MIX", "ALTO", "TIFF", "MIX/METS") );
    }
    
    
    @Override
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String newspaper,
                        LocalDate editionDate,
                        String edition,
                        String section,
                        Integer pageNumber) throws IOException {
        types.get().add("MIX");
    }
    
    @Override
    public void altoFile(DecoratedAttributeParsingEvent event,
                         String newspaper,
                         LocalDate editionDate,
                         String edition,
                         String section,
                         Integer pageNumber) throws IOException {
        types.get().add("ALTO");
    }
    
    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String newspaper,
                         LocalDate editionDate,
                         String edition,
                         String section,
                         Integer pageNumber) throws IOException {
        types.get().add("TIFF");
    }
}
