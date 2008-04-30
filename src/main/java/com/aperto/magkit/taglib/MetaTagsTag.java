package com.aperto.magkit.taglib;

import com.aperto.magkit.velocity.VelocityEngine;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 *
 * @author frank.sommer (21.04.2008)
 */
@Tag(name = "metaTags", bodyContent = BodyContent.JSP)
public class MetaTagsTag extends TagSupport {
    private static final Logger LOGGER = Logger.getLogger(LinkTargetTag.class);    
    private String _veloTemplate = "com/aperto/magkit/velocity/meta.vm";

    /**
     * Setter for the used velocity template.
     * @param veloTemplate Name of the request attribute.
     */
    @TagAttribute
    public void setVeloTemplate(String veloTemplate) {
        _veloTemplate = veloTemplate;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {
        Map<String, String> content = retrieveData();
        try {
            VelocityEngine engine = new VelocityEngine(_veloTemplate);
            String html = engine.applyTemplate(content);
            JspWriter writer = pageContext.getOut();
            writer.write(html);
        } catch (IOException ioe) {
            LOGGER.info("Can not write to jsp.");        
        } catch (Exception e) {
            LOGGER.warn("Can not create html for meta-tags.", e);
        }
        return super.doEndTag();
    }

    private Map<String, String> retrieveData() {
        Map<String, String> content = new HashMap<String, String>();
        ResourceBundle resourceBundle = ResourceBundle.getBundle("language");
        retrieveDataFromResourceBundle(content, resourceBundle, "publisher");
        retrieveDataFromResourceBundle(content, resourceBundle, "company");
        retrieveDataFromResourceBundle(content, resourceBundle, "copyright");
        retrieveDataFromResourceBundle(content, resourceBundle, "robots");
        retrieveDataFromResourceBundle(content, resourceBundle, "page-topics");
        
        retrieveDataFromMagnolia(content, "meta-author");
        retrieveDataFromMagnolia(content, "meta-keywords");
        retrieveDataFromMagnolia(content, "meta-description");
        return content;
    }

    private void retrieveDataFromMagnolia(Map<String, String> content, String nodeDataName) {
        Content actPage = Resource.getActivePage();
        if (actPage != null) {
            try {
                String value = NodeDataUtil.inheritString(actPage, nodeDataName);
                if (!StringUtils.isBlank(value)) {
                    content.put(nodeDataName, value);
                }
            } catch (RepositoryException e) {
                LOGGER.info("There is no page property for " + nodeDataName);
            }
        }
    }

    private void retrieveDataFromResourceBundle(Map<String, String> content, ResourceBundle resourceBundle, String key) {
        try {
            String value = resourceBundle.getString("meta." + key);
            if (!StringUtils.isBlank(value)) {
                content.put(key, value);
            }
        } catch (MissingResourceException mre) {
            LOGGER.info("No value found for " + key);
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();
        _veloTemplate = "com/aperto/magkit/velocity/meta.vm";
    }
}