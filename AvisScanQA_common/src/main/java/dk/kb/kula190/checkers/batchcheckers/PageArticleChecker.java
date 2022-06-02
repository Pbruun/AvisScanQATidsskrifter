package dk.kb.kula190.checkers.batchcheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class PageArticleChecker extends DecoratedEventHandler {

    private ThreadLocal<ArticleXML> articles = new ThreadLocal<>();
    private ThreadLocal<PageXML> pages = new ThreadLocal<>();

    private final Map<String, ArticleXML> articlesMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, PageXML> pagesMap = Collections.synchronizedMap(new HashMap<>());

    public PageArticleChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }

    @Override
    public void xmlFile(DecoratedAttributeParsingEvent event,
                        String newspaper,
                        LocalDate editionDate,
                        String edition,
                        String section,
                        Integer pageNumber,
                        Boolean article)
            throws IOException {
        if (article) {
//            articles.set(new ArticleXML(event,newspaper,editionDate));
            articlesMap.put(event.getName().split("/")[3], new ArticleXML(event, newspaper, editionDate));
        } else {
//            pages.set(new PageXML(event,newspaper,editionDate,edition,section,pageNumber));
            pagesMap.put(event.getName().split("/")[3], new PageXML(event, newspaper, editionDate, edition, section, pageNumber));
        }

    }

    @Override
    public void batchEnds(DecoratedNodeParsingEvent event,
                          String newspaper,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) {
        for (Map.Entry<String, PageXML> page : pagesMap.entrySet()) {
            if(page.getValue().getArticles() != null){
                List<String> arts = page.getValue().getArticles().stream().filter(Objects::nonNull).toList();
                for (String a : arts) {
                    ArticleXML article = articlesMap.get(a);
                    Integer aSectionNr = article.getSectionNumber();
                    Integer pSectionNr = page.getValue().getSectionNumber();
                    String aSectionName = article.getSectionName();
                    String pSectionName = page.getValue().getSectionName();
                    String aSource = article.getSource();
                    String pSource = page.getValue().getSource();
                    Integer aPublish = article.getPublishDate();
                    Integer pPublish = page.getValue().getPublishDate();
                    checkEquals(event,
                                FailureType.EXCEPTION, "{actual} should have been {expected}", aSectionNr, pSectionNr);
                    checkEquals(event,FailureType.EXCEPTION,"{actual} should have been {expected}",aSectionName,pSectionName);
                    checkEquals(event,FailureType.EXCEPTION,"{actual} should have been {expected}",aSource,pSource);
                    checkEquals(event,FailureType.EXCEPTION,"{actual} should have been {expected}",aPublish,pPublish);
                }
            }
        }
    }

    public ThreadLocal<ArticleXML> getArticles() {
        return articles;
    }

    public ThreadLocal<PageXML> getPages() {
        return pages;
    }

    public Map<String, ArticleXML> getArticlesMap() {
        return articlesMap;
    }

    public Map<String, PageXML> getPagesMap() {
        return pagesMap;
    }
}
