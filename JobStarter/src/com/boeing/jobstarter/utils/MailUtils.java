package com.boeing.jobstarter.utils;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailUtils {

    public static void sendEmail(String subject, String body)
            throws Exception {

        MimeMessage msg = new MimeMessage(createSession());
        // set message headers
        msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
        msg.addHeader("format", "flowed");
        msg.addHeader("Content-Transfer-Encoding", "8bit");

        msg.setFrom(new InternetAddress(XMLHandler.getTagContent(Globals.TagNames.MAIL_SENDER_MAIL_TAG),
                XMLHandler.getTagContent(Globals.TagNames.MAIL_SENDER_NAME_TAG)));

        msg.setReplyTo(InternetAddress.parse(XMLHandler.getTagContent(Globals.TagNames.MAIL_SENDER_MAIL_TAG),
                false));

        msg.setSubject(subject, "UTF-8");

        msg.setText(body, "UTF-8");

        msg.setSentDate(new Date());

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(XMLHandler.getMailingList()
                .toString(), false));

        Transport.send(msg);

        System.out.println("E-Mail Sent Successfully!!");
    }

    public static void sendEmail(String subject, File file) throws Exception {
        MimeMessage msg = new MimeMessage(createSession());
        // set message headers
        msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
        msg.addHeader("format", "flowed");
        msg.addHeader("Content-Transfer-Encoding", "8bit");

        msg.setFrom(new InternetAddress(XMLHandler.getTagContent(Globals.TagNames.MAIL_SENDER_MAIL_TAG),
                XMLHandler.getTagContent(Globals.TagNames.MAIL_SENDER_NAME_TAG)));

        msg.setReplyTo(InternetAddress.parse(XMLHandler.getTagContent(Globals.TagNames.MAIL_SENDER_MAIL_TAG),
                false));

        msg.setSubject(subject, "UTF-8");
        String body = FileUtils.ReadFile(file);
        msg.setText(body, "UTF-8");

        msg.setSentDate(new Date());

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(XMLHandler.getMailingList()
                .toString(), false));

        Transport.send(msg);

        System.out.println("E-Mail Sent Successfully!!");

    }

    public static void sendEmail(String subject, String body, File[] files) throws Exception {

        MimeMessage msg = new MimeMessage(createSession());
        msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
        msg.addHeader("format", "flowed");
        msg.addHeader("Content-Transfer-Encoding", "8bit");

        msg.setFrom(new InternetAddress(XMLHandler.getTagContent(Globals.TagNames.MAIL_SENDER_MAIL_TAG),
                XMLHandler.getTagContent(Globals.TagNames.MAIL_SENDER_NAME_TAG)));

        msg.setReplyTo(InternetAddress.parse(XMLHandler.getTagContent(Globals.TagNames.MAIL_SENDER_MAIL_TAG),
                false));

        msg.setSubject(subject, "UTF-8");

        msg.setSentDate(new Date());

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(XMLHandler.getMailingList()
                .toString(), false));

        // Create the message body part
        BodyPart messageBodyPart = new MimeBodyPart();

        // Fill the message
        messageBodyPart.setText(body);

        // Create a multipart message for attachment
        Multipart multipart = new MimeMultipart();

        // Set text message part
        multipart.addBodyPart(messageBodyPart);

        // Second part is attachment
        messageBodyPart = new MimeBodyPart();

        for (int i = 0; i < files.length; i++) {
            DataSource source = new FileDataSource(files[i].getPath());
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(files[i].getName());
            multipart.addBodyPart(messageBodyPart);
        }
        // Send the complete message parts
        msg.setContent(multipart);

        // Send message
        Transport.send(msg);
        System.out.println("E-Mail Sent Successfully with attachment!!");
    }

    public static void sendEmail(String subject, File fileBody, File[] files) throws Exception {

        MimeMessage msg = new MimeMessage(createSession());
        msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
        msg.addHeader("format", "flowed");
        msg.addHeader("Content-Transfer-Encoding", "8bit");

        msg.setFrom(new InternetAddress(XMLHandler.getTagContent(Globals.TagNames.MAIL_SENDER_MAIL_TAG),
                XMLHandler.getTagContent(Globals.TagNames.MAIL_SENDER_NAME_TAG)));

        msg.setReplyTo(InternetAddress.parse(XMLHandler.getTagContent(Globals.TagNames.MAIL_SENDER_MAIL_TAG),
                false));

        msg.setSubject(subject, "UTF-8");

        msg.setSentDate(new Date());

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(XMLHandler.getMailingList()
                .toString(), false));

        // Create the message body part
        BodyPart messageBodyPart = new MimeBodyPart();

        // Fill the message
        String body = FileUtils.ReadFile(fileBody);
        msg.setText(body, "UTF-8");

        // Create a multipart message for attachment
        Multipart multipart = new MimeMultipart();

        // Set text message part
        multipart.addBodyPart(messageBodyPart);

        // Second part is attachment
        messageBodyPart = new MimeBodyPart();

        for (int i = 0; i < files.length; i++) {
            DataSource source = new FileDataSource(files[i].getPath());
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(files[i].getName());
            multipart.addBodyPart(messageBodyPart);
        }
        // Send the complete message parts
        msg.setContent(multipart);

        // Send message
        Transport.send(msg);
        System.out.println("E-Mail Sent Successfully with attachment!!");
    }

    private static Session createSession() throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", XMLHandler.getTagContent(Globals.TagNames.MAIL_SMTP_SERVER_TAG));
        if (XMLHandler.getTagContent(Globals.TagNames.MAIL_AUTH_TAG) != "none") {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", XMLHandler.getTagContent(Globals.TagNames.MAIL_SMTP_PORT_TAG));
            String user = XMLHandler.getTagContent(Globals.TagNames.MAIL_USER_TAG);
            String password = XMLHandler.getTagContent(Globals.TagNames.MAIL_PWD_TAG);
            switch (XMLHandler.getTagContent(Globals.TagNames.MAIL_AUTH_TAG)) {
                case "tls":
                    props.put("mail.smtp.starttls.enable", "true");
                    break;
                case "ssl":
                    props.put("mail.smtp.socketFactory.port", XMLHandler
                            .getTagContent(Globals.TagNames.MAIL_SMTP_PORT_TAG));
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    break;
            }
            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            };
            return Session.getDefaultInstance(props, auth);
        } else {
            return Session.getInstance(props, null);
        }
    }
}
