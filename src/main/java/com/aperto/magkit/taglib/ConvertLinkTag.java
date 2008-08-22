package com.aperto.magkit.taglib;

import com.aperto.magkit.utils.LinkTool;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.link.LinkHelper;
import info.magnolia.cms.util.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Convert uuid to a link from the given node data (@see LinkTool).
 * If it is an internal link the contextPath will be added.
 * @author frank.sommer (07.01.2008)
 */
@Tag(name = "convertLink", bodyContent = BodyContent.JSP)
public class ConvertLinkTag extends TagSupport {
    private static final Logger LOGGER = Logger.getLogger(ConvertLinkTag.class);

    private String _selector;
    private String _nodeDataName;
    private String _linkValue;
    private String _altRepo = null;
    private boolean _addContextPath = true;
    private boolean _addExtension = true;

    @TagAttribute
    public void setNodeDataName(String nodeDataName) {
        _nodeDataName = nodeDataName;
    }

    @TagAttribute
    public void setLinkValue(String linkValue) {
        _linkValue = linkValue;
    }

    @TagAttribute
    public void setAddContextPath(String addContextPath) {
        _addContextPath = Boolean.getBoolean(addContextPath);
    }

    @TagAttribute
    public void setAddExtension(String addExtension) {
        _addExtension = Boolean.getBoolean(addExtension);
    }

    @TagAttribute
    public void setAltRepo(String altRepo) {
        _altRepo = altRepo;
    }

    @TagAttribute
    public void setSelector(String selector) {
        _selector = selector;
    }

    /**
     * Writes the converted link.
     * @return jsp output
     * @throws javax.servlet.jsp.JspException
     */
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        // if nodeData is set, fetch the linkValue from CMS
        if (!StringUtils.isBlank(_nodeDataName)) {
            Content content = Resource.getLocalContentNode();
            if (content == null) {
                content = Resource.getCurrentActivePage();
            }
            try {
                if (content.hasNodeData(_nodeDataName)) {
                    NodeData data = content.getNodeData(_nodeDataName);
                    int linkType = data.getType();
                    if (linkType == PropertyType.BINARY) {
                        _linkValue = data.getHandle() + '/' + data.getAttribute("fileName") + '.' + data.getAttribute("extension");
                    } else {
                        _linkValue = data.getString();
                    }
                }
            } catch (RepositoryException re) {
                LOGGER.warn("Can not access content node.", re);
            }
        }
        // convert linkValue write in output
        if (!StringUtils.isBlank(_linkValue)) {
            try {
                if (!LinkHelper.isExternalLinkOrAnchor(_linkValue)) {
                    String link = LinkTool.convertLink(_linkValue, _addExtension, _altRepo);
                    link = LinkTool.insertSelector(link, _selector);
                    if (_addContextPath) {
                        out.write(request.getContextPath());
                    }
                    out.write(link);
                } else {
                    out.write(_linkValue);
                }
            } catch (IOException e) {
                LOGGER.error("Error", e);
            }
        } else {
            LOGGER.info("No parameter is given for ConvertLinkTag.");
        }

        return super.doEndTag();
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        _addContextPath = true;
        _addExtension = true;
        _altRepo = null;
        _linkValue = null;
        _nodeDataName = null;
        _selector = "";
        super.release();
    }
}
