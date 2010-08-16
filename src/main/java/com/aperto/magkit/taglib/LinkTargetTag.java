package com.aperto.magkit.taglib;

import info.magnolia.cms.core.Content;
import static info.magnolia.context.MgnlContext.getAggregationState;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.apache.myfaces.tobago.apt.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Creates link target by the given nodedata.
 * <ul>
 * <li>new: <code>target="_blank"</code></li>
 * <li>popup: <code>rel="popup"</code></li>
 * </ul>
 * @author frank.sommer (20.02.2008)
 */
@Tag(name = "linkTarget", bodyContent = BodyContent.JSP)
public class LinkTargetTag extends TagSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkTargetTag.class);

    private String _nodeDataName;

    @TagAttribute
    public void setNodeDataName(String nodeDataName) {
        _nodeDataName = nodeDataName;
    }

    /**
     * Writes the link target html.
     * @return jsp output
     * @throws javax.servlet.jsp.JspException
     */
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();

        if (!isBlank(_nodeDataName)) {
            Content content = getAggregationState().getCurrentContent();
            try {
                if (content.hasNodeData(_nodeDataName)) {
                    String target = content.getNodeData(_nodeDataName).getString();
                    if ("new".equals(target)) {
                        out.write(" target=\"_blank\"");    
                    } else if ("popup".equals(target)) {
                        out.write(" rel=\"popup\"");
                    }
                }
            } catch (RepositoryException re) {
                LOGGER.warn("Can not access content node.", re);
            } catch (IOException e) {
                LOGGER.warn("Can not write to jsp.", e);
            }
        }
        return super.doEndTag();
    }
}