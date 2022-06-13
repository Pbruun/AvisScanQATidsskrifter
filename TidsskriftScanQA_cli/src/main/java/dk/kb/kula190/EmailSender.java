package dk.kb.kula190;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

import static dk.kb.util.other.StringListUtils.notNull;


public class EmailSender {
    
    
    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String bodyText;
    private String mimetype;
    private Path[] attachment;



    public EmailSender() {
    }
    
    private EmailSender(String from,
                        String to,
                        String cc,
                        String bcc,
                        String subject,
                        String bodyText,
                        String mimetype,
                        Path... attachment) {
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.bodyText = bodyText;
        this.mimetype = mimetype;
        this.attachment = attachment;
    }
    
    public static EmailSender newInstance(){
        return new EmailSender();
    }
    
    public EmailSender cloneFromThis(){
        return new EmailSender(from, to, cc, bcc, subject, bodyText, mimetype, attachment);
    }
    public EmailSender from(String from) {
        this.from = from;
        return this;
    }
    
    public EmailSender to(String to) {
        this.to = to;
        return this;
    }
    
    public EmailSender cc(String cc) {
        this.cc = cc;
        return this;
    }
    
    public EmailSender bcc(String bcc) {
        this.bcc = bcc;
        return this;
    }
    
    public EmailSender subject(String subject) {
        this.subject = subject;
        return this;
    }

    public EmailSender bodyText(String bodyText) {
        this.bodyText = bodyText;
        this.mimetype = null;
        return this;
    }

    public EmailSender bodyText(String bodyText, String mimetype) {
        this.bodyText = bodyText;
        this.mimetype = mimetype;
        return this;
    }
    
    public EmailSender attachment(Path... attachment){
        this.attachment = attachment;
        return this;
    }
    
    public void send(Properties properties) throws IOException {
        try {
            Session session = Session.getInstance(properties);
        
            Message message = createEmail(session, from, to, cc, bcc, subject, bodyText, mimetype, attachment );
            
            Transport.send(message);
        } catch (MessagingException e) {
            throw new IOException(e);
        }
    }
    
 
    
    protected Message createEmail(Session session,
                                String from,
                                String to,
                                String cc,
                                String bcc,
                                String subject,
                                String bodyText,
                                String mimetype,
                                Path... attachments) throws MessagingException {
        
        // Create a default MimeMessage object.
        Message message = new MimeMessage(session);
        
        // Set From: header field of the header.
        message.setFrom(new InternetAddress(notNull(from)));
        
        // Set To: header field of the header.
        message.setRecipients(Message.RecipientType.TO,
                              InternetAddress.parse(to));
        
        message.setRecipients(Message.RecipientType.CC,
                              InternetAddress.parse(notNull(cc)));
        
        message.setRecipients(Message.RecipientType.BCC,
                              InternetAddress.parse(notNull(bcc)));
        
        // Set Subject: header field
        message.setSubject(notNull(subject));
        
        // Create a multipar message
        Multipart multipart = new MimeMultipart();

        // Create the message part
        BodyPart messageBodyPart = new MimeBodyPart();
        // Now set the actual message
        if (mimetype == null) {
            messageBodyPart.setText(notNull(bodyText));
        } else {
            messageBodyPart.setContent(notNull(bodyText), mimetype);
        }
        // Set text message part
        multipart.addBodyPart(messageBodyPart);
    
        if (attachments != null) {
            //Add each attachment as a MimeBodyPart
            for (Path attachment : attachments) {
                if (attachment == null){
                    continue;
                }
                //if (!attachment.toFile().canRead()){
                //    continue;
                //}
                // Part two is attachment
                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachment.toFile());
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(attachment.getFileName().toString());
                multipart.addBodyPart(messageBodyPart);
            }
        }
        
        // Combine the complete message parts
        message.setContent(multipart);
        
        
        return message;
        
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmailSender that = (EmailSender) o;
        return Objects.equals(from, that.from) &&
               Objects.equals(to, that.to) &&
               Objects.equals(cc, that.cc) &&
               Objects.equals(bcc, that.bcc) &&
               Objects.equals(subject, that.subject) &&
               Objects.equals(bodyText, that.bodyText) &&
               Objects.equals(mimetype, that.mimetype) &&
               Arrays.equals(attachment, that.attachment);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(from, to, cc, bcc, subject, bodyText, mimetype, Arrays.hashCode(attachment));
    }
    
    @Override
    public String toString() {
        return "EmailBuilder{" +
               "from='" + from + '\'' +
               ", to='" + to + '\'' +
               ", cc='" + cc + '\'' +
               ", bcc='" + bcc + '\'' +
               ", subject='" + subject + '\'' +
               ", bodyText='" + bodyText + '\'' +
               ", mimetype='" + mimetype + '\'' +
               '}';
    }
}
