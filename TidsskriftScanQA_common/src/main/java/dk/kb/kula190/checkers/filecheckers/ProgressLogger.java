package dk.kb.kula190.checkers.filecheckers;


import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Prints the tree to the console. Used for testing purposes.
 */
public class ProgressLogger extends DecoratedEventHandler {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    
    public ProgressLogger(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    
    @Override
    public void batchBegins(DecoratedNodeParsingEvent event,
                            String newspaper,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) throws IOException {
        log.debug("batch:begins newpaper '{}', roundtrip '{}', startdate '{}', endDate '{}'",
                     newspaper,
                     roundTrip,
                     startDate,
                     endDate);
    }
    
    
    @Override
    public void batchEnds(DecoratedNodeParsingEvent event,
                          String newspaper,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) throws IOException {
        log.debug("batch:ends   newpaper '{}', roundtrip '{}', startdate '{}', endDate '{}'",
                     newspaper,
                     roundTrip,
                     startDate,
                     endDate);
        
    }
    
    @Override
    public void modsBegins(DecoratedNodeParsingEvent event,
                           String newspaper,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) throws IOException {
        log.trace(" mods:begins newpaper '{}', roundtrip '{}', startdate '{}', endDate '{}'",
                     newspaper,
                     roundTrip,
                     startDate,
                     endDate);
        
    }
    
    
    @Override
    public void modsEnds(DecoratedNodeParsingEvent event,
                         String newspaper,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {
        log.trace(" mods:ends   newpaper '{}', roundtrip '{}', startdate '{}', endDate '{}'",
                     newspaper,
                     roundTrip,
                     startDate,
                     endDate);
        
    }
    
    @Override
    public void metsBegins(DecoratedNodeParsingEvent event,
                           String newspaper,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) throws IOException {
        log.debug(" mets:begins newpaper '{}', roundtrip '{}', startdate '{}', endDate '{}'",
                     newspaper,
                     roundTrip,
                     startDate,
                     endDate);
    }
    
   
    
    @Override
    public void metsEnds(DecoratedNodeParsingEvent event,
                         String newspaper,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {
        log.debug(" mets:ends   newpaper '{}', roundtrip '{}', startdate '{}', endDate '{}'",
                     newspaper,
                     roundTrip,
                     startDate,
                     endDate);
    }
    
    @Override
    public void editionBegins(DecoratedNodeParsingEvent event, String newspaper, LocalDate editionDate, String edition)
            throws IOException {
        log.debug(" edition:begins newpaper '{}', day '{}', edition '{}'", newspaper, editionDate, edition);
    }
    
    
    @Override
    public void editionEnds(DecoratedNodeParsingEvent event, String newspaper, LocalDate editionDate, String edition)
            throws IOException {
        log.debug(" edition:ends   newpaper '{}', day '{}', edition '{}'", newspaper, editionDate, edition);

    }
    
    @Override
    public void sectionBegins(DecoratedNodeParsingEvent event,
                              String newspaper,
                              LocalDate editionDate,
                              String edition,
                              String section) throws IOException {
        log.trace("  section:begins newpaper '{}', day '{}', edition '{}', section '{}'",
                     newspaper, editionDate, edition, section);
    }
    
    @Override
    public void sectionEnds(DecoratedNodeParsingEvent event,
                            String newspaper,
                            LocalDate editionDate,
                            String edition,
                            String section) throws IOException {
        log.trace("  section:ends   newpaper '{}', day '{}', edition '{}', section '{}'",
                     newspaper, editionDate, edition, section);
    
    }
    
    @Override
    public void pageBegins(DecoratedNodeParsingEvent event,
                           String newspaper,
                           LocalDate editionDate,
                           String udgave,
                           String section,
                           Integer pageNumber) {
        log.trace("   page:begins newpaper '{}', day '{}', edition '{}', section '{}', page '{}'",
                     newspaper,
                     editionDate,
                     udgave,
                     section,
                     pageNumber);
    }
    
    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
                         String newspaper,
                         LocalDate editionDate,
                         String edition,
                         String section,
                         Integer pageNumber) {
        log.trace("   page:ends   newpaper '{}', day '{}', edition '{}', section '{}', page '{}'",
                     newspaper,
                     editionDate,
                     edition,
                     section,
                     pageNumber);
        
    }
}
