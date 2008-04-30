package com.aperto.magkit.velocity;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

/**
 * Class for handling velocity templates.
 *
 * @author frank.sommer (05.02.2008)
 */
public class VelocityEngine {
    private static final Logger LOGGER = Logger.getLogger(VelocityEngine.class);

    private String _templateName = null;

    /**
     * Constructor with template name.
     */
    public VelocityEngine(String templateName) throws Exception {
        setTemplateName(templateName);
        LOGGER.debug("VelocityEngine instantiated with templateName: " + getTemplateName());
    }

    /**
     * Generates with the given template the output string. 
     */
    public synchronized String applyTemplate(Map content) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "class");
        properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        // new instance to avoid memory leaks in init()
        org.apache.velocity.app.VelocityEngine velocityEngine = new org.apache.velocity.app.VelocityEngine();
        velocityEngine.init(properties);
        Template template = velocityEngine.getTemplate(getTemplateName(), "UTF-8");
        LOGGER.debug("Got template from engine");
        VelocityContext context = new VelocityContext();
        for (Object o : content.keySet()) {
            String key = (String) o;
            context.put(key, content.get(key));
            if (LOGGER.isDebugEnabled()) {
                String message = "Put into template engine context: key=" + key + ", value=" + content.get(key);
                LOGGER.debug(message);
            }
        }
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.getBuffer().toString();
    }

    //Getter and Setter
    public String getTemplateName() {
        return _templateName;
    }

    public void setTemplateName(String templateName) {
        _templateName = templateName;
    }
}