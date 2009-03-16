package com.aperto.magkit.mail;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.springframework.mail.SimpleMailMessage;

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
public abstract class AbstractMailMessageTemlate implements MailMessageTemplate {

    private String _dynamicTemplateParameterName = "template";

    private Set<String> _allowedParameterNames;

    private Map<String, ? extends Object> _defaultParameters;

    private String _templateName;

    private String _templateEncoding = "UTF-8";

    private SimpleMailMessage _messagePrototype;
    //  ---------------------------------------------------------------------
    //  Configuration
    //  ---------------------------------------------------------------------

    /**
     * This method can be used to override the default dynamic template parameter name 'template'.
     */
    public void setDynamicTemplateParameterName(final String dynamicTemplateParameterName) {
        _dynamicTemplateParameterName = dynamicTemplateParameterName;
    }

    /**
     * Defines a white list of parameter names for filtering.
     * If you omit {@link #_dynamicTemplateParameterName} in this list, it will not disable the dynamic template mechanism.
     *
     * @see # _dynamicTemplateParameterName
     */
    public void setAllowedParameterNames(final Set<String> allowedParameterNames) {
        _allowedParameterNames = allowedParameterNames;
    }

    /**
     * Provides name value pairs which become available on each evaluation per default.
     * The allowed parameter filter will not be applied to this values. On the other hand it will be possible to
     * override this default values on each execution if there exists no filter.
     *
     * @see #setAllowedParameterNames(java.util.Set)
     */
    public void setDefaultParameters(final Map<String, ? extends Object> defaultParameters) {
        _defaultParameters = defaultParameters;
    }

    /**
     * Sets the name of a velocity template.
     * To use dynamic templates you may specify a path - has to end with '/' - or keep this property empty.
     *
     * @see #_dynamicTemplateParameterName
     */
    public void setTemplateName(final String templateName) {
        _templateName = templateName;
    }

    public void setTemplateEncoding(final String templateEncoding) {
        _templateEncoding = templateEncoding;
    }

    /**
     * Creates a new SimpleMailMessage from this existing instance on template evaluation.
     *
     * @see #evaluate(java.util.Map)
     */
    public void setMessagePrototype(final SimpleMailMessage messagePrototype) {
        _messagePrototype = messagePrototype;
    }
    //  ---------------------------------------------------------------------
    //  Interface implementation
    //  ---------------------------------------------------------------------

    public SimpleMailMessage evaluate(Map<String, ? extends Object> parameters) throws Exception {
        // prepare context information
        VelocityContext context = new VelocityContext();
        addContextParameters(context, _defaultParameters);
        addFilteredContextParameters(context, parameters);
        // retrieve and evaluate template
        StringWriter writer = new StringWriter();
        Template template = getTemplate(parameters);
        template.merge(context, writer);
        StringBuffer result = writer.getBuffer();
        // create mail message
        int firstLineSeparator = result.indexOf("\n");
        String subject;
        String text;
        if (firstLineSeparator > -1) {
            subject = result.substring(0, firstLineSeparator).trim();
            text = result.substring(firstLineSeparator + 1);
        } else {
            subject = result.toString();
            text = "";
        }
        SimpleMailMessage mailMessage;
        if (_messagePrototype != null) {
            mailMessage = new SimpleMailMessage(_messagePrototype);
        } else {
            mailMessage = new SimpleMailMessage();
        }
        mailMessage.setText(text);
        mailMessage.setSubject(subject);
        return mailMessage;
    }
    //  ---------------------------------------------------------------------
    //  Inheritence interface
    //  ---------------------------------------------------------------------

    public String getDynamicTemplateParameterName() {
        return _dynamicTemplateParameterName;
    }

    protected Set<String> getAllowedParameterNames() {
        return _allowedParameterNames;
    }

    protected Map<String, ? extends Object> getDefaultParameters() {
        return _defaultParameters;
    }

    protected String getTemplateName() {
        return _templateName;
    }

    protected String getTemplateEncoding() {
        return _templateEncoding;
    }

    protected abstract Template getTemplate(final Map<String, ? extends Object> parameters) throws Exception;
    //  ---------------------------------------------------------------------
    //  Helper
    //  ---------------------------------------------------------------------

    protected void addContextParameters(final VelocityContext context, final Map<String, ? extends Object> parameters) {
        if (parameters != null) {
            for (Map.Entry<String, ? extends Object> property : parameters.entrySet()) {
                String key = property.getKey();
                Object value = property.getValue();
                context.put(key, value);
            }
        }
    }

    protected void addFilteredContextParameters(final VelocityContext context, final Map<String, ? extends Object> parameters) {
        if (parameters != null) {
            if (_allowedParameterNames != null && !_allowedParameterNames.isEmpty()) {
                for (Map.Entry<String, ? extends Object> property : parameters.entrySet()) {
                    String key = property.getKey();
                    if (_allowedParameterNames.contains(key)) {
                        Object value = property.getValue();
                        context.put(key, value);
                    }
                }
            } else {
                // if no filter is defined, the method without filter can be used
                addContextParameters(context, parameters);
            }
        }
    }
}