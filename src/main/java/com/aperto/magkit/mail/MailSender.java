package com.aperto.magkit.mail;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import java.util.Date;

/**
 * Wrapper for sending mails.
 *
 * @author frank.sommer (07.02.2008)
 */
public class MailSender {
    private static final Logger LOGGER = Logger.getLogger(MailSender.class);
    private String _sender;
    private JavaMailSender _javaMailSender;

    /**
     * Sends a mail.
     */
    public boolean sendMail(String to, String subject, String mailtext) {
        return sendMail(new String[] {to}, subject, mailtext, null);
    }

    /**
     * Sends a mail.
     */
    public boolean sendMail(String to, String subject, String mailtext, String replyTo) {
        return sendMail(new String[] {to}, subject, mailtext, replyTo);
    }

    /**
     * Sends a mail.
     * @param to mail receiver
     * @param subject mail subject
     * @param mailtext mail text
     * @param replyTo mail reply to
     * @return false if a exception is thrown
     */
    public boolean sendMail(String[] to, String subject, String mailtext, String replyTo) {
        boolean successful = false;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(_sender);
        msg.setTo(to);
        msg.setSentDate(new Date());
        msg.setText(mailtext);
        msg.setSubject(subject);
        if (!StringUtils.isBlank(replyTo)) {
            msg.setReplyTo(replyTo);
        }
        try {
            _javaMailSender.send(msg);
            successful = true;
        } catch (Exception e) {
            String message = "Mail sending failed: " + msg;
            LOGGER.error(message, e);
        }
        return successful;
    }

    public void setSender(String sender) {
    	_sender = sender;
    }

    public void setJavaMailSender(JavaMailSender javaMailSender) {
    	_javaMailSender = javaMailSender;
    }
}