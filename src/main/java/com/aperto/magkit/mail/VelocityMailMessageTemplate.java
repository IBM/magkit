package com.aperto.magkit.mail;

import static org.apache.commons.lang.StringUtils.*;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import java.util.Map;
import java.util.Properties;

/**
 * This class generates {@link SimpleMailMessage}s by evaluating a velocity template stored within the class path.
 * It is an implementation of the {@link MailMessageTemplate} interface.
 * <p/>
 * The first line of the velocity template will be interpreted as mail subject, the remaining lines as text.
 * <p/>
 * The velocity template name may be set on configuration time or dynamically for each evaluation.
 *
 * @author Norman Wiechmann (Aperto AG)
 * @see <a href="http://velocity.apache.org/engine/">The Apache Velocity Engine</a>
 */
public class VelocityMailMessageTemplate extends AbstractMailMessageTemlate {

    private String _templateFileExtension = ".vm";

    private VelocityEngine _velocityEngine;

    public VelocityMailMessageTemplate() throws Exception {
        _velocityEngine = new VelocityEngine();
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "loader");
        properties.setProperty("loader.resource.loader.class", ClasspathResourceLoader.class.getName());
        _velocityEngine.init(properties);
    }

    //  ---------------------------------------------------------------------
    //  Configuration
    //  ---------------------------------------------------------------------

    /**
     * This method can be used to override the default template file extension '.vm'.
     */
    public void setTemplateFileExtension(final String templateFileExtension) {
        _templateFileExtension = templateFileExtension;
    }

    //  ---------------------------------------------------------------------
    //  Inheritence interface implementation
    //  ---------------------------------------------------------------------

    /**
     * Returns the template by the configured name or a dynamic template that is set by a parameter.
     * <p/>
     * If a dynamic template is used, its name is looked up within the given parameters. A name of a dynamic template
     * must not have a path or file extension part.
     *
     * @throws IllegalArgumentException A dynamic template name is required but it was not found or invalid characters were used.
     * @see #setTemplateName(String)
     * @see #getDynamicTemplateParameterName()
     */
    @Override
    protected Template getTemplate(final Map<String, ? extends Object> parameters) throws Exception {
        String templateName = getTemplateName();
        if (isBlank(templateName) || templateName.endsWith("/")) {
            String dynamicTemplateName = null;
            String dynamicTemplateParameterName = getDynamicTemplateParameterName();
            if (parameters != null && parameters.containsKey(dynamicTemplateParameterName)) {
                dynamicTemplateName = parameters.get(dynamicTemplateParameterName).toString();
            }
            if (isNotBlank(dynamicTemplateName) && indexOfAny(dynamicTemplateName, "/.") == -1) {
                templateName += dynamicTemplateName.trim() + _templateFileExtension;
            } else {
                throw new IllegalArgumentException("A dynamic template name is required but it was not found or invalid characters were used.");
            }
        }
        return _velocityEngine.getTemplate(templateName, getTemplateEncoding());
    }
}