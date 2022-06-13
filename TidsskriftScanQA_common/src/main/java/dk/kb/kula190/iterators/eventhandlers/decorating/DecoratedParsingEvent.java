package dk.kb.kula190.iterators.eventhandlers.decorating;

import java.time.LocalDate;

public interface DecoratedParsingEvent {
    
    public String getAvis();
    
    public String getRoundTrip();
    
    public LocalDate getStartDate();
    
    public LocalDate getEndDate();
    
    public LocalDate getEditionDate();
    
    public String getUdgave();
    
    public String getSectionName();
    
    public Integer getPageNumber();

    public Boolean getArticle();
}
