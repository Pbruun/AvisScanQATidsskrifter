package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.xml.XML;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Set;

import static org.apache.commons.io.FilenameUtils.getName;

public class EventHandlerUtils {
    public static final  DateTimeFormatter dateFormatter =
            new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4)
                                          .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                                          .appendValue(ChronoField.DAY_OF_MONTH, 2)
                                          .toFormatter();

    public static String lastName(String name) {
        return getName(name);
    }

    public static String firstName(String name) {
        return name.replaceFirst("^([^/]+)/.*$", "$1");
    }

    public static String removeExtension(String filename) {
        if (Set.of("xml","injected").contains(FilenameUtils.getExtension(filename))) {
            return removeExtension(FilenameUtils.removeExtension(filename));
        } else {
            return FilenameUtils.removeExtension(filename);
        }
    }
    
    public static String getExtension(String filename) {
        if (Set.of("xml").contains(FilenameUtils.getExtension(filename))) {
            return getExtension(FilenameUtils.removeExtension(filename));
        } else {
            return FilenameUtils.getExtension(filename);
        }
    }

    public static Document handleDocument(DecoratedAttributeParsingEvent event) throws IOException {
        Document document;
        try (InputStream in = event.getData()) {
            document = XML.fromXML(in, true);
            return document;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException | IOException e) {
            throw new IOException(e);
        }
    }
}
