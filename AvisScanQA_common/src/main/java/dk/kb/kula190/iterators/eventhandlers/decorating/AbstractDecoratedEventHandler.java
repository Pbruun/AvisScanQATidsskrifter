package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.InjectedAttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;
import dk.kb.kula190.iterators.common.NodeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.DefaultTreeEventHandler;
import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractDecoratedEventHandler extends DefaultTreeEventHandler {

    protected final InheritableThreadLocal<String> batchName = new InheritableThreadLocal<>();
    protected final InheritableThreadLocal<String> batchLocation = new InheritableThreadLocal<>();

    public AbstractDecoratedEventHandler(ResultCollector resultCollector) {
        super(resultCollector);
    }

    public final void handleNode(NodeParsingEvent event) throws IOException {
        String lastName = EventHandlerUtils.lastName(event.getName());
        if (batchName.get() == null) {
            //modersmaalet_19060701_19061231_RT1
            batchName.set(lastName);
            batchLocation.set(event.getLocation());
            handleBatch(event);
        } else if (isMETS(event)) {
            this.handleMets(event);
        } else if (isMODS(event)) {
            handleMods(event);
        } else if (isEdition(event)) {
            handleEdition(event);
        } else if (isSection(event)) {
            handleSection(event);
        } else if (isPage(event)) {
            handlePage(event);
        } else if (event.getName().equals(batchName.get())) {
            handleBatch(event);
        }
    }


    void handleSection(NodeParsingEvent event) throws IOException {
        DecoratedNodeParsingEvent decoratedEvent = new DecoratedNodeParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> sectionBegins(decoratedEvent,
                                            decoratedEvent.getAvis(),
                                            decoratedEvent.getEditionDate(),
                                            decoratedEvent.getUdgave(),
                                            decoratedEvent.getSectionName());
            case NodeEnd -> sectionEnds(decoratedEvent,
                                        decoratedEvent.getAvis(),
                                        decoratedEvent.getEditionDate(),
                                        decoratedEvent.getUdgave(),
                                        decoratedEvent.getSectionName());
        }
    }


    @Override
    public final void handleFinish() throws IOException {
        //handleBatch(new NodeEndParsingEvent(batchName.get(), batchLocation.get()));
    }

    @Override
    public final void handleNodeBegin(NodeBeginsParsingEvent event) throws IOException {
        handleNode(event);
    }

    @Override
    public final void handleNodeEnd(NodeEndParsingEvent event) throws IOException {
        handleNode(event);
    }

    void handleBatch(NodeParsingEvent event) throws IOException {
        DecoratedNodeParsingEvent decoratedEvent = new DecoratedNodeParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> batchBegins(decoratedEvent,
                                          decoratedEvent.getAvis(),
                                          decoratedEvent.getRoundTrip(),
                                          decoratedEvent.getStartDate(),
                                          decoratedEvent.getEndDate());
            case NodeEnd -> batchEnds(decoratedEvent,
                                      decoratedEvent.getAvis(),
                                      decoratedEvent.getRoundTrip(),
                                      decoratedEvent.getStartDate(),
                                      decoratedEvent.getEndDate());
        }
    }

    void handleMets(NodeParsingEvent event) throws IOException {
        DecoratedNodeParsingEvent decoratedEvent = new DecoratedNodeParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> metsBegins(decoratedEvent,
                                         decoratedEvent.getAvis(),
                                         decoratedEvent.getRoundTrip(),
                                         decoratedEvent.getStartDate(),
                                         decoratedEvent.getEndDate());
            case NodeEnd -> metsEnds(decoratedEvent,
                                     decoratedEvent.getAvis(),
                                     decoratedEvent.getRoundTrip(),
                                     decoratedEvent.getStartDate(),
                                     decoratedEvent.getEndDate());
        }
    }

    void handleMods(NodeParsingEvent event) throws IOException {
        DecoratedNodeParsingEvent decoratedEvent = new DecoratedNodeParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> modsBegins(decoratedEvent,
                                         decoratedEvent.getAvis(),
                                         decoratedEvent.getRoundTrip(),
                                         decoratedEvent.getStartDate(),
                                         decoratedEvent.getEndDate());
            case NodeEnd -> modsEnds(decoratedEvent,
                                     decoratedEvent.getAvis(),
                                     decoratedEvent.getRoundTrip(),
                                     decoratedEvent.getStartDate(),
                                     decoratedEvent.getEndDate());
        }
    }

    void handleEdition(NodeParsingEvent event) throws IOException {
        DecoratedNodeParsingEvent decoratedEvent = new DecoratedNodeParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> editionBegins(decoratedEvent,
                                            decoratedEvent.getAvis(),
                                            decoratedEvent.getEditionDate(),
                                            decoratedEvent.getUdgave());
            case NodeEnd -> editionEnds(decoratedEvent,
                                        decoratedEvent.getAvis(),
                                        decoratedEvent.getEditionDate(),
                                        decoratedEvent.getUdgave());
        }
    }


    @Override
    public final void handleAttribute(AttributeParsingEvent event) {
        try {
            final String name = event.getName();
            String extension = EventHandlerUtils.getExtension(name);

            DecoratedAttributeParsingEvent decoratedEvent = new DecoratedAttributeParsingEvent(event);

            switch (extension) {
                case "mets" -> metsFile(decoratedEvent,
                                        decoratedEvent.getAvis(),
                                        decoratedEvent.getRoundTrip(),
                                        decoratedEvent.getStartDate(),
                                        decoratedEvent.getEndDate());
                case "mods" -> modsFile(decoratedEvent,
                                        decoratedEvent.getAvis(),
                                        decoratedEvent.getRoundTrip(),
                                        decoratedEvent.getStartDate(),
                                        decoratedEvent.getEndDate());
                case "alto" -> altoFile(decoratedEvent,
                                        decoratedEvent.getAvis(),
                                        decoratedEvent.getEditionDate(),
                                        decoratedEvent.getUdgave(),
                                        decoratedEvent.getSectionName(),
                                        decoratedEvent.getPageNumber());
                case "mix" -> mixFile(decoratedEvent,
                                      decoratedEvent.getAvis(),
                                      decoratedEvent.getEditionDate(),
                                      decoratedEvent.getUdgave(),
                                      decoratedEvent.getSectionName(),
                                      decoratedEvent.getPageNumber());
                case "tif","tiff" -> tiffFile(decoratedEvent,
                                       decoratedEvent.getAvis(),
                                       decoratedEvent.getEditionDate(),
                                       decoratedEvent.getUdgave(),
                                       decoratedEvent.getSectionName(),
                                       decoratedEvent.getPageNumber());
                case "pdf" -> pdfFile(decoratedEvent,
                                      decoratedEvent.getAvis(),
                                      decoratedEvent.getEditionDate(),
                                      decoratedEvent.getUdgave(),
                                      decoratedEvent.getSectionName(),
                                      decoratedEvent.getPageNumber());
                case "injected" -> {
                    String injectedType = ((InjectedAttributeParsingEvent) event).getInjectedType();
                    injectedFile(decoratedEvent,
                                 injectedType,
                                 decoratedEvent.getAvis(),
                                 decoratedEvent.getEditionDate(),
                                 decoratedEvent.getUdgave(),
                                 decoratedEvent.getSectionName(),
                                 decoratedEvent.getPageNumber());
                }
                default -> addFailure(event,
                                      FailureType.UNKNOWN_FILETYPE_ERROR,
                                      "Encountered unexpected file");
            }
        } catch (IOException e) {
            addExceptionalFailure(event, e);
        } catch (EncryptedPdfException | ValidationException | ModelParsingException e) {
            throw new RuntimeException(e);
        }
    }

    
    boolean isEdition(ParsingEvent event) {
        return getLevel(event) == 3
               && !Set.of("METS", "MODS").contains(EventHandlerUtils.lastName(event.getName()))
               && event.getName().matches(".*\\d{8}_udg\\d{2}$");
    }

    boolean isSection(ParsingEvent event) {
        return getLevel(event) == 4 && !EventHandlerUtils.lastName(event.getName()).equals(batchName.get());
    }

    boolean isPage(ParsingEvent event) {
        return getLevel(event) == 5;
    }

    boolean isMETS(ParsingEvent event) {
        return getLevel(event) == 1 && Objects.equals("METS", EventHandlerUtils.lastName(event.getName()));
    }

    boolean isMODS(ParsingEvent event) {
        return getLevel(event) == 1 && Objects.equals("MODS", EventHandlerUtils.lastName(event.getName()));
    }

    public final int getLevel(ParsingEvent event) {
        return new File(event.getName()).getName().split("_").length;
    }

    void handlePage(NodeParsingEvent event) throws IOException {
        DecoratedNodeParsingEvent decoratedEvent = new DecoratedNodeParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> pageBegins(decoratedEvent,
                                         decoratedEvent.getAvis(),
                                         decoratedEvent.getEditionDate(),
                                         decoratedEvent.getUdgave(),
                                         decoratedEvent.getSectionName(),
                                         decoratedEvent.getPageNumber());
            case NodeEnd -> pageEnds(decoratedEvent,
                                     decoratedEvent.getAvis(),
                                     decoratedEvent.getEditionDate(),
                                     decoratedEvent.getUdgave(),
                                     decoratedEvent.getSectionName(),
                                     decoratedEvent.getPageNumber());
        }
    }

    public abstract void batchBegins(DecoratedNodeParsingEvent event,
                                     String avis,
                                     String roundTrip,
                                     LocalDate startDate,
                                     LocalDate endDate) throws IOException;


    public abstract void batchEnds(DecoratedNodeParsingEvent event,
                                   String avis,
                                   String roundTrip,
                                   LocalDate startDate,
                                   LocalDate endDate) throws IOException;


    public abstract void modsBegins(DecoratedNodeParsingEvent event,
                                    String avis,
                                    String roundTrip,
                                    LocalDate startDate,
                                    LocalDate endDate) throws IOException;

    public abstract void modsFile(DecoratedAttributeParsingEvent event,
                                  String avis,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate) throws IOException;

    public abstract void modsEnds(DecoratedNodeParsingEvent event,
                                  String avis,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate) throws IOException;


    public abstract void metsBegins(DecoratedNodeParsingEvent event,
                                    String avis,
                                    String roundTrip,
                                    LocalDate startDate,
                                    LocalDate endDate) throws IOException;


    public abstract void metsFile(DecoratedAttributeParsingEvent event,
                                  String avis,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate) throws IOException;

    public abstract void metsEnds(DecoratedNodeParsingEvent event,
                                  String avis,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate) throws IOException;


    public abstract void editionBegins(DecoratedNodeParsingEvent event,
                                       String avis,
                                       LocalDate editionDate,
                                       String editionName) throws IOException;

    public abstract void editionEnds(DecoratedNodeParsingEvent event,
                                     String avis,
                                     LocalDate editionDate,
                                     String editionName) throws IOException;


    public abstract void sectionBegins(DecoratedNodeParsingEvent event,
                                       String avis,
                                       LocalDate editionDate,
                                       String udgave,
                                       String section) throws IOException;

    public abstract void sectionEnds(DecoratedNodeParsingEvent event,
                                     String avis,
                                     LocalDate editionDate,
                                     String udgave,
                                     String section) throws IOException;


    public abstract void pageBegins(DecoratedNodeParsingEvent event,
                                    String avis,
                                    LocalDate editionDate,
                                    String udgave,
                                    String sectionName,
                                    Integer pageNumber) throws IOException;

    public abstract void pageEnds(DecoratedNodeParsingEvent event,
                                  String avis,
                                  LocalDate editionDate,
                                  String udgave,
                                  String sectionName,
                                  Integer pageNumber) throws IOException;


    public abstract void mixFile(DecoratedAttributeParsingEvent event,
                                 String avis,
                                 LocalDate editionDate,
                                 String udgave,
                                 String sectionName,
                                 Integer pageNumber) throws IOException;

    public abstract void tiffFile(DecoratedAttributeParsingEvent event,
                                  String avis,
                                  LocalDate editionDate,
                                  String udgave,
                                  String sectionName,
                                  Integer pageNumber) throws IOException;

    public abstract void altoFile(DecoratedAttributeParsingEvent event,
                                  String avis,
                                  LocalDate editionDate,
                                  String udgave,
                                  String sectionName,
                                  Integer pageNumber) throws IOException;

    public abstract void pdfFile(DecoratedAttributeParsingEvent event,
                                 String avis,
                                 LocalDate editionDate,
                                 String udgave,
                                 String sectionName,
                                 Integer pageNumber)
            throws IOException, ValidationException, EncryptedPdfException, ModelParsingException;

    public abstract void injectedFile(DecoratedAttributeParsingEvent decoratedEvent,
                                      String injectedType,
                                      String avis,
                                      LocalDate editionDate,
                                      String udgave,
                                      String sectionName,
                                      Integer pageNumber) throws IOException;

}
