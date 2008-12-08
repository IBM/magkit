package com.aperto.degewo.taglib;

import org.apache.log4j.Logger;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.ServletRequest;
import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.Resource;
import info.magnolia.cms.link.LinkHelper;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: diana.racho
 * Date: 28.04.2008
 * Time: 11:12:20
 *
 * @author dr
 */
@Tag(name = "linkType", bodyContent = BodyContent.JSP)
public class LinkTypeTag extends TagSupport {
    private static final Logger LOGGER = Logger.getLogger(LinkTypeTag.class);

    private String _nodeDataName;
    private String _var;

    @TagAttribute(required = true)
    public void setNodeDataName(String nodeDataName) {
        _nodeDataName = nodeDataName;
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
        if (!StringUtils.isBlank(_nodeDataName)) {
            Content content = getLocalContent();
            try {
                if (content.hasNodeData(_nodeDataName)) {
                    NodeData data = content.getNodeData(_nodeDataName);
                    path = data.getString();
                }
            } catch (RepositoryException re) {
                LOGGER.warn("Can not access content node.", re);
            }
        }

        String className = getCssClassForPath(path);

        if (StringUtils.isNotBlank(className)) {
            if (StringUtils.isNotBlank(_var)) {
                request.setAttribute(_var, className);
            } else {
                try {
                    out.write(className);
                } catch (IOException e) {
                    LOGGER.error("IOException: " + e.getMessage(), e);
                }
            }
        }
        return super.doEndTag();
    }

    private String getCssClassForPath(String path) {
        String className = "";
        if (StringUtils.isNotBlank(path)) {
            if (LinkHelper.isExternalLinkOrAnchor(path)) {
                className = "extern";
            } else {
                String dmsPath = getDmsPath(path);
                if (StringUtils.isBlank(dmsPath)) {
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
        return Resource.getLocalContentNode();
    }

    protected String getDmsPath(String path) {
        return LinkHelper.convertUUIDtoHandle(path, "dms");
    }
}