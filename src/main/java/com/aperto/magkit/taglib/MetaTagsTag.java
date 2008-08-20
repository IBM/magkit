package com.aperto.magkit.taglib;

import com.aperto.magkit.velocity.SimpleTextTemplate;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.RequestContextAwareTag;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author frank.sommer (21.04.2008)
 */
@Tag(name = "metaTags", bodyContent = BodyContent.JSP)
public class MetaTagsTag extends RequestContextAwareTag {
    private static final Logger LOGGER = Logger.getLogger(MetaTagsTag.class);
    private static final String[] META_PROPERTIES = {"publisher", "company", "copyright", "robots", "page-topics", "siteinfo", "reply-to", "revisit-after", "audience"};
    private static final String[] PAGE_PROPERTIES = {"meta-author", "meta-keywords", "meta-description"};
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
            SimpleTextTemplate template = new SimpleTextTemplate(_veloTemplate);
            String html = template.evaluate(content);
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
        I18nContentSupport i18nSupport = I18nContentSupportFactory.getI18nSupport();
        String language;
        if (i18nSupport != null) {
            language = i18nSupport.getLocale().getLanguage();
        } else {
            language = getRequestContext().getLocale().getLanguage();
        }
        content.put("language", language);
        for (String property : META_PROPERTIES) {
            retrieveDataFromResourceBundle(content, property);
        }
        for (String property : PAGE_PROPERTIES) {
            retrieveDataFromMagnolia(content, property);                        
        }
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

    private void retrieveDataFromResourceBundle(Map<String, String> content, String key) {
        try {
            String messageKey = "meta." + key;
            String value = getContext().getMessage(messageKey);
            if (!StringUtils.isBlank(value) && !messageKey.equals(value)) {
                content.put(key, value);
            }
        } catch (NoSuchMessageException mre) {
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

    protected int doStartTagInternal() throws Exception {
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Helper for testing.
     */
    protected RequestContext getContext() {
        return getRequestContext();
    }
}