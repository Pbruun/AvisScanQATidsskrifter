package dk.kb.kula190.checkers.singlecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.util.xml.XML;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

public class XpathMixChecker extends DecoratedEventHandler {
    public XpathMixChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    
    
    
    
    @Override
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        Document document;
        try (InputStream in = event.getData()) {
            document = XML.fromXML(in, true);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }
        XPathSelector xpath = XpathUtils.createXPathSelector("mix", "http://www.loc.gov/mix/v20");
        
        
        
        Integer width = xpath.selectInteger(document,
                                            "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth");
        Integer height = xpath.selectInteger(document,
                                            "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeight");
        //All pages stand up, so height > width.
        checkAtLeast(event,
                "INVALID_MIX",
                height.doubleValue(),
                width.doubleValue(),
                "MIX image height: {required} was less than width: {actual}"); //`${height}` not supported in this java version?

        String colorSpace = xpath.selectString(document,"/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:PhotometricInterpretation/mix:colorSpace");
        checkEquals(event,
                "INVALID_MIX",
                "MIX colorspace should have been {expected} but was {actual}", colorSpace,
                "RGB"
        );
        
    }

}
