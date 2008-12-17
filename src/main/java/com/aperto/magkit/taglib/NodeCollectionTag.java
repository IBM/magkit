package com.aperto.magkit.taglib;

import com.aperto.magkit.utils.ContentUtils;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.Resource;
import static org.apache.commons.lang.StringUtils.isNotBlank;
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
    private boolean _orderByValue = false;
    private boolean _returnNodeData = false;

    public String getContentNodeName() {
        return _contentNodeName;
    }

    /**
     * Tag delivers a NodeData.
     * Default is false. The String value of the NodeData is delivered. 
     */
    @TagAttribute
    public void setReturnNodeData(boolean returnNodeData) {
        _returnNodeData = returnNodeData;
    }

    /**
     * Content node name with the node data collection.
     * If empty iterate over the node datas of the local content node.
     */
    @TagAttribute
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

    /**
     * Ordering by value. Default is false, that means an ordering by node name.
     */
    @TagAttribute
    public void setOrderByValue(boolean orderByValue) {
        _orderByValue = orderByValue;
    }

    protected void prepare() throws JspTagException {
        Content content = Resource.getLocalContentNode();
        try {
            if (content != null) {
                Content collContent = content;
                if (isNotBlank(_contentNodeName) && content.hasContent(_contentNodeName)) {
                    collContent = content.getContent(_contentNodeName);
                }
                Collection nodeDataCollection = collContent.getNodeDataCollection();
                if (_orderByValue) {
                    nodeDataCollection = ContentUtils.orderNodeDataCollectionByValue(nodeDataCollection);
                } else {
                    nodeDataCollection = ContentUtils.orderNodeDataCollection(nodeDataCollection);
                }
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
        Object returnObject;
        if (_returnNodeData) {
            returnObject = _nodeIterator.next();    
        } else {
            returnObject = ((NodeData) _nodeIterator.next()).getString();
        }
        return returnObject;
    }

    /**
     * Release tag.
     */
    @Override
    public void release() {
        super.release();
        _contentNodeName = "";
        _nodeIterator = null;
        _orderByValue = false;
        _returnNodeData = false;
    }
}
