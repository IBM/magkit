package com.aperto.magkit.taglib;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static info.magnolia.cms.util.Resource.getCurrentActivePage;
import static info.magnolia.cms.util.Resource.getLocalContentNode;
import static com.aperto.magkit.utils.LinkTool.insertSelector;
import static com.aperto.magkit.utils.LinkTool.convertLink;
import static info.magnolia.cms.link.LinkHelper.isExternalLinkOrAnchor;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.codec.EncoderException;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
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
    private String _linkValue = EMPTY;
    private String _altRepo = null;
    private boolean _addContextPath = true;
    private boolean _addExtension = true;
    private Content _contentNode = null;

    @TagAttribute
    public void setNodeDataName(String nodeDataName) {
        _nodeDataName = nodeDataName;
    }

    /**
     * A link to be converted into am URL-encoded magnolia link. This value will only be processed if no name for an NodeData is provided.
     * Default is "".
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
     * The system name of the fallback repository where to get the linked content from if it could not be fount in the default repository ('website').
     * Allowed values are "website", "dms" (if module is installed), "data" (if module is installed).
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
     * The magnolia content instance to read the node data from. 
     * If not given the content node or current active page from the magnolia context will be used.
     * @param contentNode an instance of info.magnolia.cms.core.Content
     */
    @TagAttribute
    public void setContentNode(Content contentNode) {
        _contentNode = contentNode;
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
        if (isNotBlank(_linkValue)) {
            try {
                if (!isExternalLinkOrAnchor(_linkValue)) {
                    String link = convertLink(_linkValue, _addExtension, _altRepo);
                    link = insertSelector(link, _selector);
                    if (_addContextPath && isNotBlank(link)) {
                        builder.append(request.getContextPath());
                    }
                    builder.append(link);
                } else {
                    builder.append(_linkValue);
                }
                String link = builder.toString();
                if (isBlank(_var)) {
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
        Content content = getContentNode();
        if (content != null && isNotBlank(_nodeDataName)) {
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
        } else {
            LOGGER.info("Given content node is null or node data name is not specified. Using given linkValue.");
        }
    }

    private Content getContentNode() {
        Content content = _contentNode;
        if (content == null) {
            content = getLocalContentNode();
            if (content == null) {
                content = getCurrentActivePage();
            }
        }
        return content;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        _addContextPath = true;
        _addExtension = true;
        _altRepo = null;
        _linkValue = EMPTY;
        _nodeDataName = null;
        _selector = EMPTY;
        _contentNode = null;
        super.release();
    }
}
