package com.aperto.magkit.taglib;

import com.aperto.magkit.utils.ContentUtils;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Collection;
import java.util.Iterator;

/**
 * Tag for iterate a nodecollection (e.g. from checkbox-control).
 *
 * @author frank.sommer (15.05.2008)
 */
@Tag(name = "nodeCollIterator", bodyContent = BodyContent.JSP)
public class NodeCollectionTag  extends TagSupport {
    private static final Logger LOGGER = Logger.getLogger(NodeCollectionTag.class);
    private String _contentNodeName;
    private String _var = "nodeValue";
    private Iterator _nodeIterator;

    public String getContentNodeName() {
        return _contentNodeName;
    }

    /**
     * Content node name with the node data collection.
     */
    @TagAttribute (required = true)
    public void setContentNodeName(String contentNodeName) {
        _contentNodeName = contentNodeName;
    }

    /**
     * Setter for <code>var</code>.
     * Default is nodeValue.
     */
    @TagAttribute
    public void setVar(String var) {
        _var = var;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        int returnValue = SKIP_BODY;
        Content content = Resource.getLocalContentNode();
        try {
            if (content != null && !StringUtils.isBlank(_contentNodeName) && content.hasContent(_contentNodeName)) {
                Content collContent = content.getContent(_contentNodeName);
                Collection nodeDataCollection = collContent.getNodeDataCollection();
                nodeDataCollection = ContentUtils.orderNodeDataCollection(nodeDataCollection);
                _nodeIterator = nodeDataCollection.iterator();
                returnValue = doIteration() ? EVAL_BODY_INCLUDE : SKIP_BODY;
            }
        } catch (RepositoryException e){
            LOGGER.info("Contentnode could not retrieved.");
        }

        return returnValue;
    }

    private boolean doIteration() {
        boolean status = false;
        if (_nodeIterator.hasNext()) {
            NodeData nd = (NodeData) _nodeIterator.next();
            pageContext.setAttribute(_var, nd.getString(), PageContext.REQUEST_SCOPE);
            status = true;
        }
        return status;
    }
}
