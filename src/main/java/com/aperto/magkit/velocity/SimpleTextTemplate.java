package com.aperto.magkit.velocity;

import org.apache.log4j.Logger;
import org.apache.velocity.app.*;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import java.util.Map;
import java.util.Properties;
import java.io.StringWriter;

/**
 * Simple text velocity engine.
 *
 * @author frank.sommer (07.08.2008)
 */
public class SimpleTextTemplate implements TextTemplate {
    private static final Logger LOGGER = Logger.getLogger(SimpleTextTemplate.class);
    private VelocityEngine _velocityEngine;
    private String _templateName;
    private String _templateEncoding = "UTF-8";

    public String evaluate(Map<String, ? extends Object> parameters) throws Exception {
        // prepare context information
        VelocityContext context = new VelocityContext();
        addParameters(context, parameters);
        // retrieve and evaluate template
        StringWriter writer = new StringWriter();
        Template template = _velocityEngine.getTemplate(_templateName, _templateEncoding);
        LOGGER.debug("Got template from engine.");
        template.merge(context, writer);
        StringBuffer result = writer.getBuffer();
        return result.toString();
    }

    public SimpleTextTemplate() throws Exception {
        _velocityEngine = new VelocityEngine();
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "loader");
        properties.setProperty("loader.resource.loader.class", ClasspathResourceLoader.class.getName());
        _velocityEngine.init(properties);
    }

    public SimpleTextTemplate(String templateName) throws Exception {
        _velocityEngine = new VelocityEngine();
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "loader");
        properties.setProperty("loader.resource.loader.class", ClasspathResourceLoader.class.getName());
        _velocityEngine.init(properties);
        _templateName = templateName;
    }

    protected void addParameters(final VelocityContext context, final Map<String, ? extends Object> parameters) {
        if (parameters != null) {
            for (Map.Entry<String, ? extends Object> property : parameters.entrySet()) {
                String key = property.getKey();
                Object value = property.getValue();
                context.put(key, value);
                if (LOGGER.isDebugEnabled()) {
                    String message = "Put into template engine context: key=" + key + ", value=" + value;
                    LOGGER.debug(message);
                }
            }
        }
    }

    //  ---------------------------------------------------------------------
    //  Configuration
    //  ---------------------------------------------------------------------

    /**
     * Sets the name of a velocity template.
     */
    public void setTemplateName(final String templateName) {
        _templateName = templateName;
    }

    /**
     * Sets the template encoding. Default is UTF-8.
     */
    public void setTemplateEncoding(final String templateEncoding) {
        _templateEncoding = templateEncoding;
    }

    public String getTemplateName() {
        return _templateName;
    }

    public String getTemplateEncoding() {
        return _templateEncoding;
    }
}
