package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

public class DecoratedAttributeParsingEvent extends AttributeParsingEvent implements DecoratedParsingEvent {
    
    private final AttributeParsingEvent delegate;
    private final String avis;
    private final String roundTrip;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDate editionDate;
    private final String udgave;
    private final String sectionName;
    private final Integer pageNumber;

    private final Boolean article;
    
    public DecoratedAttributeParsingEvent(AttributeParsingEvent delegate) {
        super(delegate.getName(), delegate.getLocation());
    
        final String lastName = EventHandlerUtils.removeExtension(EventHandlerUtils.lastName(delegate.getName()));
    
        String avis = null;
        LocalDate editionDate = null;
        String udgave = null;
        String sectionName = null;
        Integer pageNumber = null;
        Boolean article = false;
    
        String batch;
        String[] splittedName = delegate.getName().split("/");
        if (splittedName[2].equals("articles")) {
            article = true;
        } else { //page/section/edition-like
            batch = splittedName[0];
            String[] fileNameSplitted = splittedName[3].split("_");
            sectionName = fileNameSplitted[2];
            String[] page = fileNameSplitted[3].split("page");
            pageNumber = Integer.parseInt(page[1]);
        }

        avis        = splittedName[1];
        editionDate = LocalDate.parse(splittedName[0].split("_")[1], EventHandlerUtils.dateFormatter);

        LocalDate startDate = editionDate;
        LocalDate endDate = editionDate;
        String roundTrip = splittedName[0].split("_")[2].replaceFirst("^rt", "");
    
        this.delegate    = delegate;
        this.avis        = avis;
        this.roundTrip   = roundTrip;
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.editionDate = editionDate;
        this.udgave      = null;
        this.sectionName = sectionName;
        this.pageNumber  = pageNumber;
        this.article = article;
    }
    
    @Override
    public InputStream getData() throws IOException {
        return delegate.getData();
    }
    
    @Override
    public String getChecksum() throws IOException {
        return delegate.getChecksum();
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
    public Boolean getArticle(){
        return article;
    }
    @Override
    public String toString() {
        return "DecoratedAttributeParsingEvent{" +
               "type=" + type +
               ", name='" + name + '\'' +
               ", location='" + location + '\'' +
               ", avis='" + avis + '\'' +
               ", roundTrip='" + roundTrip + '\'' +
               ", startDate=" + startDate +
               ", endDate=" + endDate +
               ", editionDate=" + editionDate +
               ", udgave='" + udgave + '\'' +
               ", sectionName='" + sectionName + '\'' +
               ", pageNumber=" + pageNumber + '\'' +
               ", article=" + article +
               '}';
    }
}
