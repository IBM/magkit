package com.aperto.magkit.velocity;

import org.springframework.mail.SimpleMailMessage;
import java.util.Map;

/**
 * Interface for velocity text template.
 *
 * @author frank.sommer (07.08.2008)
 */
public interface TextTemplate {
    /**
     * Creates a String from a template using given parameters.
     */
    String evaluate(Map<String, ? extends Object> parameters) throws Exception;
}
