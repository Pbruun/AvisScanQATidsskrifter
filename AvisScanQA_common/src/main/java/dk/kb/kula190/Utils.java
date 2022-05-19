package dk.kb.kula190;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static Node asSeparateXML(Node metadataMods) throws ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node mods = document.appendChild(document.adoptNode(metadataMods));
        return document.getDocumentElement();
    }
    
    public static Set<String> fromAnotInB(Set<String> altoFilesFromMets, Set<String> altoFilesVisited) {
        HashSet<String> diff = new HashSet<>(altoFilesFromMets);
        diff.removeAll(altoFilesVisited);
        return diff;
    }

    public static List<String> runTool(String tool, String location, String... strings) throws IOException {
        //TODO proper inferface with no repeated params
        ArrayList<String> command = new ArrayList<>();
        command.add(tool);
        command.addAll(Arrays.asList(strings));
        command.add(location);
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();

        try (InputStream stdOut = process.getInputStream();
             InputStream stdErr = process.getErrorStream();
             ByteArrayOutputStream processOutput = new ByteArrayOutputStream();
             ByteArrayOutputStream processErr = new ByteArrayOutputStream();) {


            while (true) {
                boolean stopped = process.waitFor(100, TimeUnit.MILLISECONDS);

                IOUtils.copy(stdOut, processOutput);
                IOUtils.copy(stdErr, processErr);
                if (stopped) {
                    int returnValue = process.exitValue();
                    if (returnValue != 0) {
                        throw new IOException(tool + " failed with return code "
                                              + returnValue
                                              + " on "
                                              + location
                                              + ". Stdout="
                                              + readProcessOutput(processOutput.toByteArray())
                                              + "\n\nStdErr="
                                              + readProcessOutput(processErr.toByteArray()));
                    }
                    break;
                }
            }
            return readProcessOutput(processOutput.toByteArray());

        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for {} on {}", tool, location, e);
            throw new IOException(e);
        }
    }


    private static List<String> readProcessOutput(byte[] processOutput) throws IOException {
        try (InputStream processOutCollected = new ByteArrayInputStream(processOutput);
             BufferedReader stdOut = new BufferedReader(new InputStreamReader(processOutCollected,
                                                                              StandardCharsets.UTF_8))) {
            List<String> lines = IOUtils.readLines(stdOut);
            return lines;
        }
    }
}
