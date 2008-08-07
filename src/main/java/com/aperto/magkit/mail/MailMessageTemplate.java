package com.aperto.magkit.mail;

import java.util.Map;

import org.springframework.mail.SimpleMailMessage;

/**
 * Interface of a mail message templates that creates a {@link SimpleMailMessage} from a template using given parameters.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public interface MailMessageTemplate {

    /**
     * Creates a {@link SimpleMailMessage} from a template using given parameters.
     */
    SimpleMailMessage evaluate(Map<String, ? extends Object> parameters) throws Exception;
}