package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.iterators.common.NodeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;

import java.time.LocalDate;

public class DecoratedNodeParsingEvent extends NodeParsingEvent implements DecoratedParsingEvent{
    
    private String avis;
    private String roundTrip;
    private LocalDate startDate;
    private LocalDate endDate;
    
    private LocalDate editionDate;
    private String udgave;
    private String sectionName;
    private Integer pageNumber;

    private Boolean article;
    
    
    public DecoratedNodeParsingEvent(NodeParsingEvent delegate) {
        super(delegate.getName(), delegate.getType(), delegate.getLocation());
    
    
        final String lastName = EventHandlerUtils.lastName(delegate.getName());

        String avis = null;
        LocalDate editionDate = null;
        String udgave = null;
        String sectionName = null;
        Integer pageNumber = null;
        Boolean article = false;

        String batch;

        String[] splits = lastName.split("_", 5);
        editionDate = LocalDate.parse(splits[1], EventHandlerUtils.dateFormatter);
        LocalDate startDate = editionDate;
        LocalDate endDate = editionDate;
        avis = null;
        String roundTrip = splits[2].replaceFirst("^rt","");

        this.avis        = avis;
        this.roundTrip   = roundTrip;
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.editionDate = editionDate;
        this.udgave      = udgave;
        this.sectionName = sectionName;
        this.pageNumber  = pageNumber;
        this.article = article;
    }
    
    @Override
    public String getAvis() {
        return avis;
    }
    
    @Override
    public String getRoundTrip() {
        return roundTrip;
    }
    
    @Override
    public LocalDate getStartDate() {
        return startDate;
    }
    
    @Override
    public LocalDate getEndDate() {
        return endDate;
    }
    
    @Override
    public LocalDate getEditionDate() {
        return editionDate;
    }
    
    @Override
    public String getUdgave() {
        return udgave;
    }
    
    @Override
    public String getSectionName() {
        return sectionName;
    }
    
    @Override
    public Integer getPageNumber() {
        return pageNumber;
    }

    @Override
    public Boolean getArticle(){return article;}
}
