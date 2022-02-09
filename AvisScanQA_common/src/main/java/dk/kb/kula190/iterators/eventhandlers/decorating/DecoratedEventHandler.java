package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;
import dk.kb.kula190.iterators.common.NodeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.DefaultTreeEventHandler;
import dk.kb.kula190.iterators.eventhandlers.EventRunner;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DecoratedEventHandler extends DefaultTreeEventHandler {
    
    private InheritableThreadLocal<String> batch = new InheritableThreadLocal<>();
    
    
    private final ResultCollector resultCollector;
    
    
    private static final DateTimeFormatter dateFormatter =
            new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4)
                                          .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                                          .appendValue(ChronoField.DAY_OF_MONTH, 2)
                                          .toFormatter();
    
    protected DecoratedEventHandler(ResultCollector resultCollector) {this.resultCollector = resultCollector;}
    
    public final void handleNode(NodeParsingEvent event) {
        String lastName = lastName(event.getName());
        if (batch.get() == null) {
            //modersmaalet_19060701_19061231_RT1
            batch.set(lastName);
            handleBatch(event, lastName);
        } else if (isMETS(event)) {
            this.handleMets(event, batch.get());
        } else if (isMODS(event)) {
            handleMods(event, batch.get());
        } else if (isEdition(event)) {
            handleEdition(event, lastName);
        } else if (isSection(event)) {
            handleSection(event, lastName);
        } else if (isPage(event)) {
            handlePage(event, lastName);
        }
    }
    
    @Override
    public final void handleNodeBegin(NodeBeginsParsingEvent event) {
        handleNode(event);
    }
    
    @Override
    public final void handleNodeEnd(NodeEndParsingEvent event) {
        handleNode(event);
    }
    
    private void handleBatch(NodeParsingEvent event, String lastName) {
        String[] splits = lastName.split("_", 4);
        LocalDate startDate = LocalDate.parse(splits[1], dateFormatter);
        LocalDate endDate = LocalDate.parse(splits[2], dateFormatter);
        String avis = splits[0];
        String roundTrip = splits[3].replaceFirst("^RT", "");
        switch (event.getType()) {
            case NodeBegin -> batchBegins(event, avis, roundTrip, startDate, endDate);
            case NodeEnd -> batchEnds(event, avis, roundTrip, startDate, endDate);
        }
    }
    
    private void handleMets(NodeParsingEvent event, String batchID) {
        String[] splits = batchID.split("_", 4);
        LocalDate startDate = LocalDate.parse(splits[1], dateFormatter);
        LocalDate endDate = LocalDate.parse(splits[2], dateFormatter);
        String avis = splits[0];
        String roundTrip = splits[3].replaceFirst("^RT", "");
        switch (event.getType()) {
            case NodeBegin -> metsBegins((NodeBeginsParsingEvent) event, avis, roundTrip, startDate, endDate);
            case NodeEnd -> metsEnds((NodeEndParsingEvent) event, avis, roundTrip, startDate, endDate);
        }
    }
    
    private void handleMods(NodeParsingEvent event, String batchID) {
        String[] splits = batchID.split("_", 4);
        LocalDate startDate = LocalDate.parse(splits[1], dateFormatter);
        LocalDate endDate = LocalDate.parse(splits[2], dateFormatter);
        String avis = splits[0];
        String roundTrip = splits[3].replaceFirst("^RT", "");
        switch (event.getType()) {
            case NodeBegin -> modsBegins(event, avis, roundTrip, startDate, endDate);
            case NodeEnd -> modsEnds(event, avis, roundTrip, startDate, endDate);
        }
    }
    
    private void handleEdition(NodeParsingEvent event, String edition) {
        //modersmaalet_19060706_udg01
        String[] splits = edition.split("_", 3);
        String avis = splits[0];
        LocalDate editionDate = LocalDate.parse(splits[1], dateFormatter);
        String udgave = splits[2];
        switch (event.getType()) {
            case NodeBegin -> editionBegins(event, avis, editionDate, udgave);
            case NodeEnd -> editionEnds(event, avis, editionDate, udgave);
        }
    }
    
    private void handleSection(NodeParsingEvent event, String section) {
        //modersmaalet_19060706_udg01_1.sektion
        String[] splits = section.split("_", 4);
        String avis = splits[0];
        LocalDate editionDate = LocalDate.parse(splits[1], dateFormatter);
        String udgave = splits[2];
        String sectionName = splits[3];
        switch (event.getType()) {
            case NodeBegin -> sectionBegins(event, avis, editionDate, udgave, sectionName);
            case NodeEnd -> sectionEnds(event, avis, editionDate, udgave, sectionName);
        }
    }
    
    private void handlePage(NodeParsingEvent event, String lastName) {
        //modersmaalet_19060706_udg01_1.sektion_0001
        String[] splits = lastName.split("_", 5);
        String avis = splits[0];
        LocalDate editionDate = LocalDate.parse(splits[1], dateFormatter);
        String udgave = splits[2];
        String sectionName = splits[3];
        Integer pageNumber = Integer.parseInt(splits[4]);
        switch (event.getType()) {
            case NodeBegin -> pageBegins(event, avis, editionDate, udgave, sectionName, pageNumber);
            case NodeEnd -> pageEnds(event, avis, editionDate, udgave, sectionName, pageNumber);
        }
    }
    
    private Integer pageNumber(String name) {
        return Integer.parseInt(name.replaceFirst("^(.+?)_(\\d+)$", "$2"));
    }
    
    
    @Override
    public final void handleAttribute(AttributeParsingEvent event) {
        try {
            //modersmaalet_19060701_19061231_RT1.mets.xml
            if (event.getName().matches(".*\\.((mets)|(mods))(\\.xml)?$")) {
                String name = lastName(event.getName());
                String[] splits = batch.get().split("_", 4);
                LocalDate startDate = LocalDate.parse(splits[1], dateFormatter);
                LocalDate endDate = LocalDate.parse(splits[2], dateFormatter);
                String avis = splits[0];
                String roundTrip = splits[3].replaceFirst("^RT", "");
                if (name.endsWith(".mets") || name.endsWith(".mets.xml")) {
                    metsFile(event, avis, roundTrip, startDate, endDate);
                } else if (name.endsWith(".mods") || name.endsWith(".mods.xml")) {
                    modsFile(event, avis, roundTrip, startDate, endDate);
                }
            } else if (event.getName().matches(".*\\.(((mix|alto)(\\.xml)?)|(pdf)|(tiff?))$")) {
                //modersmaalet_19060701_udg01_MODERSMAALETS Søndagsblad_0001.mix.xml
                
                String name = lastName(event.getName());
                String[] splits = name.split("_", 5);
                String avis = splits[0];
                LocalDate editionDate = LocalDate.parse(splits[1], dateFormatter);
                String udgave = splits[2];
                String sectionName = splits[3];
                Integer pageNumber = Integer.parseInt(splits[4].split("\\.", 2)[0]);
                if (name.endsWith(".alto") || name.contains(".alto.xml")) {
                    altoFile(event, avis, editionDate, udgave, sectionName, pageNumber);
                } else if (name.endsWith(".mix") || name.endsWith(".mix.xml")) {
                    mixFile(event, avis, editionDate, udgave, sectionName, pageNumber);
                } else if (name.endsWith(".tif")) {
                    tiffFile(event, avis, editionDate, udgave, sectionName, pageNumber);
                } else if (name.endsWith(".pdf")) {
                    pdfFile(event, avis, editionDate, udgave, sectionName, pageNumber);
                }
            } else {
                resultCollector.addFailure(event.getName(),
                                           "Unknown Filetype",
                                           this.getClass().getSimpleName(),
                                           "Encounted unexpected file");
            }
            
            
        } catch (IOException e) {
            resultCollector.addFailure(event.getName(),
                                       EventRunner.EXCEPTION,
                                       this.getClass().getSimpleName(),
                                       EventRunner.UNEXPECTED_ERROR + e,
                                       Arrays.stream(e.getStackTrace())
                                             .map(StackTraceElement::toString)
                                             .collect(Collectors.joining("\n")));
        }
    }
    
    
    private boolean isEdition(ParsingEvent event) {
        return getLevel(event) == 2 && !Set.of("METS", "MODS").contains(lastName(event.getName())) && event.getName()
                                                                                                           .matches(
                                                                                                                   ".*\\d{8}_udg\\d{2}$");
    }
    
    private boolean isMETS(ParsingEvent event) {
        return getLevel(event) == 2 && Objects.equals("METS", lastName(event.getName()));
    }
    
    private boolean isMODS(ParsingEvent event) {
        return getLevel(event) == 2 && Objects.equals("MODS", lastName(event.getName()));
    }
    
    private String lastName(String name) {
        return name.replaceFirst("^(.+?)/([^/]+)$", "$2");
    }
    
    private boolean isSection(ParsingEvent event) {
        return getLevel(event) == 3;
    }
    
    private boolean isPage(ParsingEvent event) {
        return getLevel(event) == 4;
    }
    
    
    public final int getLevel(ParsingEvent event) {
        return event.getName().split("/").length;
    }
    
    
    public void batchBegins(ParsingEvent event,
                            String batch,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) {}
    
    
    public void batchEnds(ParsingEvent event,
                          String batch,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) {}
    
    
    public void modsBegins(ParsingEvent event,
                           String avis,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) {}
    
    public void modsFile(AttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    public void modsEnds(ParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) {}
    
    
    public void metsBegins(ParsingEvent event,
                           String avis,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) {}
    
    
    public void metsFile(AttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    public void metsEnds(ParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) {}
    
    
    public void editionBegins(ParsingEvent event,
                              String avis,
                              LocalDate editionDate,
                              String editionName) {}
    
    public void editionEnds(ParsingEvent event,
                            String avis,
                            LocalDate editionDate,
                            String editionName) {}
    
    
    public void sectionBegins(ParsingEvent event,
                              String editionName,
                              LocalDate editionDate,
                              String udgave, String section) {}
    
    public void sectionEnds(ParsingEvent event,
                            String editionName,
                            LocalDate editionDate,
                            String udgave, String section) {}
    
    
    public void pageBegins(ParsingEvent event,
                           String editionName,
                           LocalDate editionDate,
                           String udgave, String sectionName, Integer pageNumber) {}
    
    public void pageEnds(ParsingEvent event,
                         String editionName,
                         LocalDate editionDate,
                         String udgave, String sectionName, Integer pageNumber) {}
    
    
    public void mixFile(AttributeParsingEvent event,
                        String editionName,
                        LocalDate editionDate,
                        String udgave, String sectionName, Integer pageNumber) throws IOException {}
    
    public void tiffFile(AttributeParsingEvent event,
                         String editionName,
                         LocalDate editionDate,
                         String udgave, String sectionName, Integer pageNumber) throws IOException {}
    
    public void altoFile(AttributeParsingEvent event,
                         String editionName,
                         LocalDate editionDate,
                         String udgave, String sectionName, Integer pageNumber) throws IOException {}
    
    public void pdfFile(AttributeParsingEvent event,
                        String editionName,
                        LocalDate editionDate,
                        String udgave, String sectionName, Integer pageNumber) throws IOException {}
    
    
    public ResultCollector getResultCollector() {
        return resultCollector;
    }
}
