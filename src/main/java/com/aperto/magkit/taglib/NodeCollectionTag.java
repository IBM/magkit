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
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.LoopTagSupport;
import java.util.Collection;
import java.util.Iterator;

/**
 * Tag for iterate a nodecollection (e.g. from checkbox-control).
 *
 * @author frank.sommer (15.05.2008)
 */
@Tag(name = "nodeCollIterator", bodyContent = BodyContent.JSP)
public class NodeCollectionTag extends LoopTagSupport {
    private static final Logger LOGGER = Logger.getLogger(NodeCollectionTag.class);
    private String _contentNodeName;
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
     * Variable name of the current object in request.
     */
    @TagAttribute
    public void setVar(String string) {
        super.setVar(string);
    }

    /**
     * Name of the var status object.
     */
    @TagAttribute
    public void setVarStatus(String string) {
        super.setVarStatus(string);
    }

    protected void prepare() throws JspTagException {
        Content content = Resource.getLocalContentNode();
        try {
            if (content != null && !StringUtils.isBlank(_contentNodeName) && content.hasContent(_contentNodeName)) {
                Content collContent = content.getContent(_contentNodeName);
                Collection nodeDataCollection = collContent.getNodeDataCollection();
                nodeDataCollection = ContentUtils.orderNodeDataCollection(nodeDataCollection);
                _nodeIterator = nodeDataCollection.iterator();
            }
        } catch (RepositoryException e){
            LOGGER.info("Contentnode could not retrieved.");
        }
    }

    protected boolean hasNext() throws JspTagException {
        return _nodeIterator.hasNext();
    }

    protected Object next() throws JspTagException {
        return ((NodeData) _nodeIterator.next()).getString();
    }

    /**
     * Release tag.
     */
    @Override
    public void release() {
        super.release();
        _contentNodeName = "";
        _nodeIterator = null;
    }
}
