package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.ResultCollector;
import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;

import java.io.IOException;
import java.time.LocalDate;

public class DecoratedEventHandler extends AbstractDecoratedEventHandler {
    
    public DecoratedEventHandler(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    @Override
    public void batchBegins(DecoratedNodeParsingEvent event,
                            String newspaper,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) throws IOException {}
    
    @Override
    public void batchEnds(DecoratedNodeParsingEvent event,
                          String newspaper,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) throws IOException {}
    
    @Override
    public void modsBegins(DecoratedNodeParsingEvent event,
                           String newspaper,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) throws IOException {}
    @Override
    public void modsFile(DecoratedAttributeParsingEvent event,
                         String newspaper,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    @Override
    public void modsEnds(DecoratedNodeParsingEvent event,
                         String newspaper,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    @Override
    public void metsBegins(DecoratedNodeParsingEvent event,
                           String newspaper,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) throws IOException {}
    
    @Override
    public void metsFile(DecoratedAttributeParsingEvent event,
                         String newspaper,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    @Override
    public void metsEnds(DecoratedNodeParsingEvent event,
                         String newspaper,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    @Override
    public void editionBegins(DecoratedNodeParsingEvent event,
                              String newspaper,
                              LocalDate editionDate,
                              String edition) throws IOException {}
    @Override
    public void editionEnds(DecoratedNodeParsingEvent event,
                            String newspaper,
                            LocalDate editionDate,
                            String edition) throws IOException {}
    
    
    
    @Override
    public void sectionBegins(DecoratedNodeParsingEvent event,
                              String newspaper,
                              LocalDate editionDate,
                              String edition,
                              String section) throws IOException {
        
    }
    
    @Override
    public void sectionEnds(DecoratedNodeParsingEvent event,
                            String newspaper,
                            LocalDate editionDate,
                            String edition,
                            String section) throws IOException {
        
    }
    
    @Override
    public void pageBegins(DecoratedNodeParsingEvent event,
                           String newspaper,
                           LocalDate editionDate,
                           String udgave,
                           String section,
                           Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
                         String newspaper,
                         LocalDate editionDate,
                         String edition,
                         String section,
                         Integer pageNumber) throws IOException {
        
    }
    
   
    @Override
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String newspaper,
                        LocalDate editionDate,
                        String edition,
                        String section,
                        Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String newspaper,
                         LocalDate editionDate,
                         String edition,
                         String section,
                         Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void altoFile(DecoratedAttributeParsingEvent event,
                         String newspaper,
                         LocalDate editionDate,
                         String edition,
                         String section,
                         Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void pdfFile(DecoratedAttributeParsingEvent event,
                        String newspaper,
                        LocalDate editionDate,
                        String edition,
                        String section,
                        Integer pageNumber)
            throws IOException, ValidationException, EncryptedPdfException, ModelParsingException {
        
    }
    
    @Override
    public void injectedFile(DecoratedAttributeParsingEvent decoratedEvent,
                             String injectedType,
                             String newspaper,
                             LocalDate editionDate,
                             String edition,
                             String section,
                             Integer pageNumber) throws IOException {
        
    }
}
