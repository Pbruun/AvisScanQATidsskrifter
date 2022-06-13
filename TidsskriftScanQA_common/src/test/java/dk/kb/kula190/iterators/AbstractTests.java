package dk.kb.kula190.iterators;


import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.common.TreeIterator;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractTests {
    
    
    private static final String indentString = "                                                   ";
    
    private static String getIndent(int indent) {
        String s;
        if (indent > 0) {
            s = indentString.substring(0, indent);
        } else {
            s = "";
        }
        return s;
    }
    
    public abstract TreeIterator getIterator() throws URISyntaxException, IOException;
    
    public void testIterator(PrintStream print, final PrintStream printContent) throws Exception {
        printStructure(getIterator(), print, printContent);
    }
    
    private String printEvent(ParsingEvent next) throws IOException {
        switch (next.getType()) {
            case NodeBegin:
                return "<node name=\"" + next.getName() + "\" location=\"" + next.getLocation() + "\">";
            case NodeEnd:
                return "</node>";
            case Attribute:
                if (next instanceof AttributeParsingEvent) {
                    AttributeParsingEvent attributeParsingEvent = (AttributeParsingEvent) next;
                    return "<attribute name=\""
                           + next.getName()
                           + "\" checksum=\""
                           + attributeParsingEvent.getChecksum()
                           + "\" location=\""
                           + next.getLocation()
                           + "\"/>";
                }
            
            default:
                return next.toString();
        }
    }
    
    public void testIteratorWithSkipping(PrintStream print, final PrintStream printContent) throws Exception {
        
        List<TreeIterator> avisIterators = new ArrayList<>();
        
        
        System.out.println("Print the batch and film, and store the iterators for the aviser");
        int indent = 0;
        while (getIterator().hasNext()) {
            ParsingEvent next = getIterator().next();
            
            String s;
            switch (next.getType()) {
                case NodeBegin:
                    s = getIndent(indent);
                    if (print != null) {
                        print.println(s + printEvent(next));
                    }
                    indent += 2;
                    if (indent >= 2) {
                        TreeIterator avis = getIterator().skipToNextSibling();
                        avisIterators.add(avis);
                        indent -= 2;
                    }
                    break;
                case NodeEnd:
                    indent -= 2;
                    s = getIndent(indent);
                    if (print != null) {
                        print.println(s + printEvent(next));
                    }
                    break;
                case Attribute:
                    s = getIndent(indent);
                    if (print != null) {
                        print.println(s + printEvent(next));
                    }
                    break;
            }
        }
        if (print != null) {
            System.out.println("Print each of the newspapers in order");
        }
        for (TreeIterator avisIterator : avisIterators) {
            if (print != null) {
                print.println("We found this newspaper");
            }
            printStructure(avisIterator, print, printContent);
        }
        
    }
    
    private void printStructure(TreeIterator avisIterator, PrintStream print, final PrintStream printContent) throws
                                                                                                              IOException {
        int indent = 0;
        int files = 0;
        while (avisIterator.hasNext()) {
            ParsingEvent next = avisIterator.next();
            switch (next.getType()) {
                case NodeBegin -> {
                    String s;
                    s = getIndent(indent);
                    if (print != null) {
                        print.println(s + printEvent(next));
                    }
                    indent += 2;
                }
                case NodeEnd -> {
                    String s;
                    indent -= 2;
                    s = getIndent(indent);
                    if (print != null) {
                        print.println(s + printEvent(next));
                    }
                }
                case Attribute -> {
                    String s = getIndent(indent);
                    AttributeParsingEvent attributeEvent = (AttributeParsingEvent) next;
                    if (print != null) {
                        print.println(s + printEvent(next));
                    }
                    s = getIndent(indent + 2);
                    if (printContent != null) {
                        List<String> content = IOUtils.readLines(attributeEvent.getData(), StandardCharsets.UTF_8);
                        printContent.println(s + "[" + content.size() + " lines of content]");
                    }
                    files++;
                }
            }
            
        }
        assertEquals(indent, 0, "Indent is not reset after iteration");
        assertTrue(files > 1, "We have not encountered very much, only " + files + ", is the test data broken?");
    }
}
