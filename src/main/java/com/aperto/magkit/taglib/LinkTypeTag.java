package com.aperto.magkit.taglib;

import java.io.IOException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.aperto.magkit.utils.LinkTool;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.link.LinkHelper;
import info.magnolia.cms.util.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;

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
        if (StringUtils.isNotBlank(_nodeDataName)) {
            Content content = getLocalContent();
            try {
                if (content.hasNodeData(_nodeDataName)) {
                    path = content.getNodeData(_nodeDataName).getString();
                }
            } catch (RepositoryException re) {
                LOGGER.warn("Can not access content node.", re);
            }
        } else {
            if (StringUtils.isNotBlank(_linkValue)) {
                path = _linkValue;
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
        return LinkTool.convertUUIDtoHandle(path, "dms");
    }
}