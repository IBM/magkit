package com.aperto.magkit.mail;

import info.magnolia.cms.beans.runtime.Document;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
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
     * Sends a simple mail.
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

    /**
     * Sends a mail with attachment.
     */
    public boolean sendMail(String to, String subject, String mailtext, String replyTo, Document file) {
        return sendMail(new String[] {to}, subject, mailtext, replyTo, file);            
    }

    /**
     * Sends a mail with attachment.
     * @param to recipients
     * @param subject subject
     * @param mailtext mailtext
     * @param replyTo reply to
     * @param file attachment
     * @return successful
     */
    public boolean sendMail(String[] to, String subject, String mailtext, String replyTo, Document file) {
        boolean successful = false;

        MimeMessage message = _javaMailSender.createMimeMessage();
        try {
            // use the true flag to indicate you need a multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setText(mailtext);
            helper.setSubject(subject);
            if (!StringUtils.isBlank(replyTo)) {
                helper.setReplyTo(replyTo);
            }
            if (file != null) {
                helper.addAttachment(file.getFileNameWithExtension(), file.getFile());
            }
            _javaMailSender.send(helper.getMimeMessage());
            successful = true;
        } catch (MessagingException e) {
            LOGGER.info(e.getLocalizedMessage());
        } catch (Exception e) {
            LOGGER.error("Mail sending failed.", e);
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