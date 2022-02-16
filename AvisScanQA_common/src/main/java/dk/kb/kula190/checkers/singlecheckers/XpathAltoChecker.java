package dk.kb.kula190.checkers.singlecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandlerWithSections;
import dk.kb.util.xml.XML;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

public class XpathAltoChecker extends DecoratedEventHandlerWithSections {
    public XpathAltoChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    
    @Override
    public void altoFile(AttributeParsingEvent event,
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
        XPathSelector xpath = XpathUtils.createXPathSelector("alto", "http://www.loc.gov/standards/alto/ns-v2#");
        
        
        //        <Page ID="P1" HEIGHT="13835" WIDTH="11005" PHYSICAL_IMG_NR="1" QUALITY="OK" POSITION="Single" PROCESSING="OCR1"
        //              ACCURACY="41.00" PC="0.410">
        
        Node pageNode = xpath.selectNode(document, "/alto:alto/alto:Layout/alto:Page");
        
        //TODO should this check actually be there?
        checkAtLeast(event,
                     "INVALID_ALTO",
                     Double.parseDouble(pageNode.getAttributes().getNamedItem("ACCURACY").getNodeValue()),
                     10.0,
                     "ALTO OCR accurary {actual} is lower than {required}");
        
        checkEquals(event,
                    "INVALID_ALTO",
                    pageNode.getAttributes().getNamedItem("QUALITY").getNodeValue(),
                    "OK",
                    "ALTO quality should have been {expected} but was {actual}");
        //TODO compare against acceptable levels
        //Checks page Height is within range. what was meant with acceptable levels?
        checkWithinRange(event,
                "INVALID_ALTO",
                Double.parseDouble(pageNode.getAttributes().getNamedItem("HEIGHT").getNodeValue()),
                10000,
                50000,
                "ALTO page height is not within range: {requiredMin}-{requiredMax} actual height is: {actual}");
        //Checks page Width is within range
        checkWithinRange(event,
                "INVALID_ALTO",
                Double.parseDouble(pageNode.getAttributes().getNamedItem("WIDTH").getNodeValue()),
                10000,
                50000,
                "ALTO page width is not within range: {requiredMin}-{requiredMax} actual width is: {actual}");

    }
    
}
