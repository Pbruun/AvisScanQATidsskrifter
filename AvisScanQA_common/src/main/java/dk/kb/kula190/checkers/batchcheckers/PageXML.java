package dk.kb.kula190.checkers.batchcheckers;

import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Node;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PageXML {
    private DecoratedAttributeParsingEvent event;
    private String newspaper;
    private LocalDate editionDate;
    private String edition;
    private String section;
    private Integer pageNumber;
    private String fileName;
    private Set<String> articles;
    private String sectionName;
    private Integer sectionNumber;

    public PageXML(DecoratedAttributeParsingEvent event,
                   String newspaper,
                   LocalDate editionDate,
                   String edition,
                   String section,
                   Integer pageNumber) throws IOException {
        this.event = event;
        this.newspaper = newspaper;
        this.editionDate = editionDate;
        this.edition = edition;
        this.section = section;
        this.pageNumber = pageNumber;
        createData();
    }

    private void createData() throws IOException {
        Node metadata = EventHandlerUtils.handleDocument(event);
        XPathSelector xpath = XpathUtils.createXPathSelector();

        fileName = xpath.selectString(metadata, "/pdfinfo/filename");
        List<String> temp = xpath.selectStringList(metadata, "/pdfinfo/articles/article/filename/text()");
        if(!temp.isEmpty()){
            articles = new HashSet<String>(temp);
        }
        sectionNumber = xpath.selectInteger(metadata,"/pdfinfo/positional/sectionnumber");
        sectionName = xpath.selectString(metadata,"/pdfinfo/positional/sectionname");
    }

    public DecoratedAttributeParsingEvent getEvent() {
        return event;
    }

    public void setEvent(DecoratedAttributeParsingEvent event) {
        this.event = event;
    }

    public String getNewspaper() {
        return newspaper;
    }

    public void setNewspaper(String newspaper) {
        this.newspaper = newspaper;
    }

    public LocalDate getEditionDate() {
        return editionDate;
    }

    public void setEditionDate(LocalDate editionDate) {
        this.editionDate = editionDate;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Set<String> getArticles() {
        return articles;
    }

    public void setArticles(Set<String> articles) {
        this.articles = articles;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public Integer getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(Integer sectionNumber) {
        this.sectionNumber = sectionNumber;
    }
}
