package dk.kb.kula190.checkers.batchcheckers;

import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Node;

import java.io.IOException;
import java.time.LocalDate;

public class ArticleXML {

    private LocalDate editionDate;
    private String newspaper;
    private DecoratedAttributeParsingEvent event;

    private String pageFile;

    private String sectionName;

    private Integer sectionNumber;

    private Integer pageNumber;

    private String fileName;

    private String source;

    private Integer publishDate;

    public ArticleXML(DecoratedAttributeParsingEvent event,
                      String newspaper,
                      LocalDate editionDate) throws IOException {
        this.event = event;
        this.newspaper = newspaper;
        this.editionDate = editionDate;
        createData();
    }

    private void createData() throws IOException {
        Node metadata = EventHandlerUtils.handleDocument(event);
        XPathSelector xpath = XpathUtils.createXPathSelector();

        fileName = xpath.selectString(metadata, "/article/administrativedata/filename");
        pageNumber = xpath.selectInteger(metadata, "/article/metadata/pages/page/pagenumber");
        sectionNumber = xpath.selectInteger(metadata,"/article/metadata/pages/page/sectionnumber");
        sectionName = xpath.selectString(metadata,"/article/metadata/pages/page/sectionname");
        pageFile = xpath.selectString(metadata,"/article/metadata/pages/page/pagefile");
        source = xpath.selectString(metadata,"/article/metadata/source");
        publishDate = xpath.selectInteger(metadata,"/article/metadata/publishdate");

    }
    public void setEditionDate(LocalDate editionDate) {
        this.editionDate = editionDate;
    }

    public void setNewspaper(String newspaper) {
        this.newspaper = newspaper;
    }

    public void setEvent(DecoratedAttributeParsingEvent event) {
        this.event = event;
    }

    public String getPageFile() {
        return pageFile;
    }

    public void setPageFile(String pageFile) {
        this.pageFile = pageFile;
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


    public LocalDate getEditionDate() {
        return editionDate;
    }

    public String getNewspaper() {
        return newspaper;
    }

    public DecoratedAttributeParsingEvent getEvent() {
        return event;
    }

    public String getSource() {
        return source;
    }

    public Integer getPublishDate() {
        return publishDate;
    }
}
