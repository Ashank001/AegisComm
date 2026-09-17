package com.gateway.util;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class MailSender {

    private static final String FROM_EMAIL = getEnv("MAIL_FROM", "ashank1301@gmail.com");
    private static final String APP_PASSWORD = getEnv("MAIL_PASSWORD", "");

    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    public static void send(String to, String subject, String content) throws MessagingException {
        if (APP_PASSWORD.isEmpty()) {
            throw new MessagingException("MAIL_PASSWORD environment variable is not configured.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
            new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(to)
        );
        message.setSubject(subject);
        message.setText(content);

        Transport.send(message);
    }
}
