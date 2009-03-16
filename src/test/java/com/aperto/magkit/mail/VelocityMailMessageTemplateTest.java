package com.aperto.magkit.mail;

import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.springframework.mail.SimpleMailMessage;

/**
 * Test cases for {@link VelocityMailMessageTemplate} feature.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class VelocityMailMessageTemplateTest {
    private static final String DYNAMIC_TEMPLATE_PARAM_NAME = "dynamicTemplate";
    private static final String TEXT = "Best regards,\n${someone}";

    //  ---------------------------------------------------------------------
    //  Test cases
    //  ---------------------------------------------------------------------

    @Test
    public void nullParameters() throws Exception {
        SimpleMailMessage message = createTemplate().evaluate(null);
        assertEquals("Hello ${who}!", message.getSubject());
        assertEquals(TEXT, message.getText());
    }

    @Test
    public void noParameters() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        SimpleMailMessage message = createTemplate().evaluate(parameters);
        assertEquals("Hello ${who}!", message.getSubject());
        assertEquals(TEXT, message.getText());
    }

    @Test
    public void message() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("who", "world");
        parameters.put("someone", "your test case");
        SimpleMailMessage message = createTemplate().evaluate(parameters);
        assertEquals("Hello world!", message.getSubject());
        assertEquals("Best regards,\nyour test case", message.getText());
    }

    @Test
    public void defaultParameters() throws Exception {
        Map<String, String> defaultParameters = new HashMap<String, String>();
        defaultParameters.put("who", "world");
        VelocityMailMessageTemplate template = createTemplate();
        template.setDefaultParameters(defaultParameters);
        SimpleMailMessage message = template.evaluate(null);
        assertEquals("Hello world!", message.getSubject());
        assertEquals(TEXT, message.getText());
    }

    @Test
    public void filterParametersAndOverwriteDefault() throws Exception {
        Map<String, String> defaulParameters = new HashMap<String, String>();
        defaulParameters.put("who", "world");
        defaulParameters.put("someone", "your test case");
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("who", "whole world");
        parameters.put("someone", "your lovely test case");
        VelocityMailMessageTemplate template = createTemplate();
        template.setDefaultParameters(defaulParameters);
        template.setAllowedParameterNames(new HashSet<String>(asList("who")));
        SimpleMailMessage message = template.evaluate(parameters);
        assertEquals("Hello whole world!", message.getSubject());
        assertEquals("Best regards,\nyour test case", message.getText());
    }

    @Test
    public void dynamicTemplate() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("who", "world");
        parameters.put(DYNAMIC_TEMPLATE_PARAM_NAME, "VelocityMailMessageTemplateDynamicTest");
        VelocityMailMessageTemplate template = createDynamicTemplate();
        template.setDynamicTemplateParameterName(DYNAMIC_TEMPLATE_PARAM_NAME);
        SimpleMailMessage message = template.evaluate(parameters);
        assertEquals("Hello dynamic world!", message.getSubject());
        assertEquals(TEXT, message.getText());
    }

    @Test(expected = IllegalArgumentException.class)
    public void dynamicTemplateMissingParameter() throws Exception {
        createDynamicTemplate().evaluate(null);
    }

    @Test
    public void dynamicTemplateDoesNotFilterParameter() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("who", "world");
        parameters.put(DYNAMIC_TEMPLATE_PARAM_NAME, "VelocityMailMessageTemplateDynamicTest");
        VelocityMailMessageTemplate template = createDynamicTemplate();
        template.setAllowedParameterNames(new HashSet<String>(asList("who")));
        template.setDynamicTemplateParameterName(DYNAMIC_TEMPLATE_PARAM_NAME);
        SimpleMailMessage message = template.evaluate(parameters);
        assertEquals("Hello dynamic world!", message.getSubject());
        assertEquals(TEXT, message.getText());
    }

    @Test
    public void emptyMessage() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        createEmptyTemplate().evaluate(parameters);
    }

    @Test
    public void messagePrototype() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        VelocityMailMessageTemplate template = createTemplate();
        SimpleMailMessage messagePrototype = new SimpleMailMessage();
        messagePrototype.setFrom("testFrom@aperto.de");
        messagePrototype.setTo("testTo@aperto.de");
        template.setMessagePrototype(messagePrototype);
        SimpleMailMessage message = template.evaluate(parameters);
        assertEquals("testFrom@aperto.de", message.getFrom());
        assertEquals("testTo@aperto.de", message.getTo()[0]);
    }

    //  ---------------------------------------------------------------------
    //  Helper
    //  ---------------------------------------------------------------------

    private VelocityMailMessageTemplate createTemplate() throws Exception {
        VelocityMailMessageTemplate template = new VelocityMailMessageTemplate();
        template.setTemplateName("VelocityMailMessageTemplateTest.vm");
        return template;
    }

    private VelocityMailMessageTemplate createDynamicTemplate() throws Exception {
        VelocityMailMessageTemplate template = new VelocityMailMessageTemplate();
        template.setTemplateName("");
        return template;
    }

    private VelocityMailMessageTemplate createEmptyTemplate() throws Exception {
        VelocityMailMessageTemplate template = new VelocityMailMessageTemplate();
        template.setTemplateName("VelocityMailMessageTemplateEmptyTest.vm");
        return template;
    }
}