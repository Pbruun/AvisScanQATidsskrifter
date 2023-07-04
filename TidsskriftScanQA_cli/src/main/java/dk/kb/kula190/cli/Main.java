package dk.kb.kula190.cli;

import dk.kb.kula190.ResultCollector;
import dk.kb.util.yaml.YAML;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final String IGNORED_FILES_DEFAULT_VALUE = "transfer_complete,transfer_acknowledged,delete_ok";

    public static void main(String... args) throws IOException, URISyntaxException {

        File configFolder = new File(Thread.currentThread()
                                           .getContextClassLoader()
                                           .getResource("AvisScanQA_cli-behaviour.yaml")
                                           .toURI()).getParentFile();
        YAML config = YAML.resolveLayeredConfigs(configFolder.getAbsolutePath() + "/AvisScanQA_cli-*.yaml");
        System.out.println();
        AvisScanQATool tool = new AvisScanQATool(config,
                                                 config.getString("iterator.checksumFile"),
                                                 config.getString("iterator.acknowledgmentFile"),
                                                 config.getList("iterator.filesToIgnore"));
        List<ResultCollector> resultCollector = new ArrayList<>();
        for (String f :args
             ) {
            resultCollector.add(tool.check(Path.of(f).toAbsolutePath()));

        }


        if (config.getBoolean("jdbc.enabled")) {
            //If we preserve the results in DB, just return a success
            System.exit(0);
        } else {
            //Else output the results and return an error code if any failures were detected
            for (ResultCollector r: resultCollector
                 ) {
                System.out.println(r.toReport());
                System.exit(r.getFailures().size());
            }

        }
    }

}
