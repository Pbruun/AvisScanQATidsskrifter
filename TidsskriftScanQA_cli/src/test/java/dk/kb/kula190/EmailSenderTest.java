package dk.kb.kula190;

import dk.kb.util.yaml.YAML;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.activation.MimeType;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class EmailSenderTest {

    @Test
    @Disabled("Do not send mail as automatic test")
    void send() throws IOException, URISyntaxException {
        Properties emailConfig;
        File configFolder = new File(Thread.currentThread()
                                           .getContextClassLoader()
                                           .getResource("AvisScanQA_cli-behaviour.yaml")
                                           .toURI()).getParentFile();
        YAML config = YAML.resolveLayeredConfigs(configFolder.getAbsolutePath() + "/AvisScanQA_cli-*.yaml");
        emailConfig = dk.kb.util.yaml.YAMLUtils.toProperties(config.getSubMap("mail.smtp", true));
        String to = config.getString("mail.to");
        String from = config.getString("mail.from");
        String cc = config.getString("mail.cc");
        String bcc = config.getString("mail.bcc");
        String subject = config.getString("mail.subject");
        String bodyText = config.getString("mail.bodyText");
        String URL = config.getString("mail.URL");
        EmailSender.newInstance()
                   .to(to)//comma-separated
                   .from(from)
                   .cc(cc) //comma-separated
                   .bcc(bcc) //comma-separated
                   .subject(subject)
                   //.bodyText("https://google.com") //Plain text
                   .bodyText(bodyText+" <a href='"+URL+"'>"+URL+"</a>",
                             "text/html") //html email
                   //.attachment(Path.of(attachment))
                   .send(emailConfig);
    }
}