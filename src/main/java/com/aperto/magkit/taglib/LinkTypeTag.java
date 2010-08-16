package com.aperto.magkit.taglib;

import static com.aperto.magkit.utils.LinkTool.convertUUIDtoHandle;
import info.magnolia.cms.core.Content;
import static info.magnolia.context.MgnlContext.getAggregationState;
import static info.magnolia.link.LinkUtil.isExternalLinkOrAnchor;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import org.apache.myfaces.tobago.apt.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Delivers the type ('intern', 'extern', 'download') for the link, e.g. for a class name.
 *
 * @author diana.racho
 * @since 28.04.2008
 */
@Tag(name = "linkType", bodyContent = BodyContent.JSP)
public class LinkTypeTag extends TagSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkTypeTag.class);

    private String _nodeDataName;
    private String _var;
    private String _linkValue;

    @TagAttribute
    public void setNodeDataName(String nodeDataName) {
        _nodeDataName = nodeDataName;
    }

    @TagAttribute
    public void setLinkValue(String linkValue) {
        _linkValue = linkValue;
    }

    public String getNodeDataName() {
        return _nodeDataName;
    }

    @TagAttribute(required = false)
    public void setVar(String var) {
        _var = var;
    }

    public String getVar() {
        return _var;
    }

    /**
     * Writes the type of the link.
     *
     * @return jsp output
     * @throws JspException
     */
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();
        String path = "";
        ServletRequest request = pageContext.getRequest();

        // if nodeData is set, fetch the linkValue from CMS
        if (isNotBlank(_nodeDataName)) {
            Content content = getLocalContent();
            try {
                if (content.hasNodeData(_nodeDataName)) {
                    path = content.getNodeData(_nodeDataName).getString();
                }
            } catch (RepositoryException re) {
                LOGGER.warn("Can not access content node.", re);
            }
        } else {
            if (isNotBlank(_linkValue)) {
                path = _linkValue;
            }
        }

        String className = getCssClassForPath(path);

        if (isNotBlank(className)) {
            if (isNotBlank(_var)) {
                request.setAttribute(_var, className);
            } else {
                try {
                    out.write(className);
                } catch (IOException e) {
                    LOGGER.error("IOException: {}.", e.getMessage(), e);
                }
            }
        }
        return super.doEndTag();
    }

    private String getCssClassForPath(String path) {
        String className = "";
        if (isNotBlank(path)) {
            if (isExternalLinkOrAnchor(path)) {
                className = "extern";
            } else {
                String dmsPath = getDmsPath(path);
                if (isBlank(dmsPath)) {
                    className = "intern";
                } else {
                    className = "download";
                }
            }
        } else {
            LOGGER.info("No parameter is given for LinkTypeTag.");
        }
        return className;
    }

    protected Content getLocalContent() {
        return getAggregationState().getCurrentContent();
    }

    protected String getDmsPath(String path) {
        return convertUUIDtoHandle(path, "dms");
    }
}