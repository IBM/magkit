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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Convert uuid to a link from the given node data (@see LinkTool).
 * The local content node (active paragraph in context) will be used as content node. If not set the current active page will be used as fallback.
 * If it is an internal link the contextPath will be added.
 * If the Attribute 'linkValue' is set instead of 'nodeDataName' this value will be processed.
 * @author frank.sommer (07.01.2008)
 */
@Tag(name = "convertLink", bodyContent = BodyContent.JSP)
public class ConvertLinkTag extends TagSupport {
    private static final Logger LOGGER = Logger.getLogger(ConvertLinkTag.class);
    private String _var;
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

    /**
     * A link to be converted into am URL-encoded magnolia link. This value will only be processed if no name for an NodeData is provided.
     * Default is NULL.
     * @param linkValue an URL String
     */
    @TagAttribute
    public void setLinkValue(String linkValue) {
        _linkValue = linkValue;
    }

    /**
     * A flag whether to add the context path at the beginning of the URL (eg. '/author' or '/publish').
     * Default is TRUE.
     * @param addContextPath a String representation of a Boolean value ("true" or "false").
     */
    @TagAttribute
    public void setAddContextPath(String addContextPath) {
        _addContextPath = Boolean.getBoolean(addContextPath);
    }

    /**
     * A flag whether to add a file extension at the end of the URL.
     * If the URL does not end with '.html' or '.htm' a default file extension '.html' will be apended.
     * If the the URL points to a document is the dms module the propper file extenssion will be read from the documents meta data.
     * Default is TRUE.
     * @param addExtension a String representation of a Boolean value ("true" or "false").
     */
    @TagAttribute
    public void setAddExtension(String addExtension) {
        _addExtension = Boolean.getBoolean(addExtension);
    }

    /**
     * The system name of the repository where the linked document is stored. Allowed values are "website", "dms" (if module is installed), "data" (if module is installed).
     * Default is "website".
     * @param altRepo the repository name ("dms" or "website")
     */
    @TagAttribute
    public void setAltRepo(String altRepo) {
        _altRepo = altRepo;
    }

    /**
     * An URL sselector to be added to the end of the URL right before the file extension.
     * Default is NULL.
     * @param selector the URL selector
     */
    @TagAttribute
    public void setSelector(String selector) {
        _selector = selector;
    }

    /**
     * The name of the page context variable where the resulting URL String will be stored.
     * @param var the page context variable name
     */
    @TagAttribute
    public void setVar(String var) {
        _var = var;
    }

    /**
     * Writes the converted link.
     * @return jsp output
     * @throws javax.servlet.jsp.JspException
     */
    public int doEndTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        StringBuilder builder = new StringBuilder(64);
        fetchLinkValuefromCms();

        // convert linkValue write in output
        if (!StringUtils.isBlank(_linkValue)) {
            try {
                if (!LinkHelper.isExternalLinkOrAnchor(_linkValue)) {
                    String link = LinkTool.convertLink(_linkValue, _addExtension, _altRepo);
                    link = LinkTool.insertSelector(link, _selector);
                    if (_addContextPath && StringUtils.isNotBlank(link)) {
                        builder.append(request.getContextPath());
                    }
                    builder.append(link);
                } else {
                    builder.append(_linkValue);
                }
                String link = ((HttpServletResponse) pageContext.getResponse()).encodeURL(builder.toString());
                if (StringUtils.isBlank(_var)) {
                    JspWriter out = pageContext.getOut();
                    out.write(link);
                } else {
                    request.setAttribute(_var, link);
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
     * if nodeData is set, fetch the linkValue from CMS.
     */
    private void fetchLinkValuefromCms() {
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
