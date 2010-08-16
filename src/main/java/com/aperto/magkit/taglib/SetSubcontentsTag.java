package com.aperto.magkit.taglib;

import static info.magnolia.cms.beans.config.ContentRepository.WEBSITE;
import info.magnolia.cms.core.Content;
import static info.magnolia.cms.core.ItemType.CONTENT;
import info.magnolia.cms.core.NodeData;
import static info.magnolia.cms.util.ContentUtil.getContent;
import static info.magnolia.context.MgnlContext.getAggregationState;
import static org.apache.commons.collections15.ComparatorUtils.naturalComparator;
import static org.apache.commons.collections15.ComparatorUtils.reversedComparator;
import static org.apache.commons.collections15.ComparatorUtils.transformedComparator;
import org.apache.commons.collections15.Transformer;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;

import javax.jcr.PropertyType;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.*;

/**
 * Retrieves a List of Content Objects that are childs of the specified content
 * and stores it in the pageContext using the provided value for 'var' as key.
 * Supported filtering criteria are:
 * <ul>
 *  <li>Item Type</li>
 *  <li>name pattern</li>
 * </ul>
 * Supports sorting by a nodeData value of the child content nodes.
 *
 * @author Wolf Bubenik (Aperto AG)
 */
@Tag(name = "subcontents")
public class SetSubcontentsTag extends TagSupport {
    private static final String ITEM_TYPE_CONTENT = CONTENT.getSystemName();

    private String _var;
    private String _parentPath;
    private String _itemType = ITEM_TYPE_CONTENT;
    private String _namePattern = "*";
    private String _repository = WEBSITE;
    private String _sortContentNodeName;
    private String _sortNodeDataName;
    private boolean _reverseSortingOrder = false;
    private String _varSize = "subcontents.size";

    /**
     * The key used to store the resulting list in the PageContext.
     * Required, no default.
     * @param var a String as PageContext key
     */
    @TagAttribute(required = true)
    public void setVar(final String var) {
        _var = var;
    }

    /**
     * The path to the Content node to extract the childs from.
     * Default is the current active page.
     * @param parentPath the path to the parent of the desired child nodes
     */
    @TagAttribute()
    public void setParentPath(final String parentPath) {
        _parentPath = parentPath;
    }

    /**
     * Filter criteria: The Item type to return. Default is "mgnl:content" for content nodes.
     * Allowed are all values that has been declared in info.magnolia.cms.core.ItemType:
     * "mgnl:content", "mgnl:contentNode", "mgnl:user", "mgnl:role", "mgnl:group", "mgnl:reserve" for ItemType System,
     * "jcr:content", "nt:folder".
     * @param itemType a String with the item type of the desired child nodes
     */
    @TagAttribute()
    public void setItemType(String itemType) {
        _itemType = itemType;
    }

    /**
     * Filter criteria: The name pattern for the nodes to return. Default is "*" for all nodes.
     * @param namePattern  a String with the name pattern for the desired child nodes
     */
    @TagAttribute()
    public void setNamePattern(String namePattern) {
        _namePattern = namePattern;
    }

    /**
     * The name of the repository where to read the parent element from.
     * One of 'website', 'data', 'dms'. Default is 'website'.
     * @param repository a String to select the repository
     */
    @TagAttribute()
    public void setRepository(String repository) {
        _repository = repository;
    }

    /**
     * Sorting criteria: The name of the sub Content to be used as sorting criteria.
     * If not specified the current Content node will be used.
     * If not existing, the current content element will be added at the end of the list.
     * Default is NULL - the current conten node.
     * @param sortContentNodeName  a String with the name of the content node
     */
    @TagAttribute()
    public void setSortContentNodeName(String sortContentNodeName) {
        _sortContentNodeName = sortContentNodeName;
    }

    /**
     * Sorting criteria: The name of the NodeData element of the specified content node to be used for sorting.
     * If not given the result list will not be sorted. It keeps the original order the contents have been added to its parent node.
     * If not existing, the current content element will be added at the end of the list.
     * If the NodeData value is not comparable (does not implement the Interface java.util.Comparable),
     * the current content element will be added at the end of the list.
     * Default is NULL (no sorting).
     * @param sortNodeDataName a String with the name of the node data element
     */
    @TagAttribute()
    public void setSortNodeDataName(String sortNodeDataName) {
        _sortNodeDataName = sortNodeDataName;
    }

    /**
     * Sorting criteria: Declares whether the sorting order should be reversed.
     * Default is 'false'.
     * @param reverseSortingOrder a boolean value ('true' or 'false').
     */
    @TagAttribute()
    public void setReverseSortingOrder(boolean reverseSortingOrder) {
        _reverseSortingOrder = reverseSortingOrder;
    }

    /**
     * The key used to store the resulting list size in the PageContext.
     * Default is 'subcontents.size'. 
     * @param varSize a String as PageContext key
     */
    @TagAttribute()
    public void setVarSize(String varSize) {
        _varSize = varSize;
    }

    /**
     * Loads the content node specified by parentPath and puts the list of its childs into the page context.
     * If no path to a parent node has been declared the current active page will be used.
     */
    @Override
    public int doStartTag() throws JspException {
        Content parent = getParentContent();
        List subContents = getChildsContents(parent);
        sort(subContents);
        pageContext.setAttribute(_var, subContents);
        pageContext.setAttribute(_varSize, subContents.size());
        return super.doStartTag();
    }

    private void sort(List subContents) {
        if (isNotEmpty(_sortNodeDataName)) {
            Collections.sort(subContents, getComparator());
        }
    }

    private Transformer getTransformer() {
        return new ContentNodeDataTransformer(_sortContentNodeName, _sortNodeDataName);
    }

    private Comparator getComparator() {
        Comparator comparator = _reverseSortingOrder ? reversedComparator(null) : naturalComparator();
        comparator = new UncomparableAtEndComparatorWrapper(comparator);
        return transformedComparator(comparator, getTransformer());
    }

    private List getChildsContents(Content parent) {
        List result = Collections.emptyList();
        if (parent != null) {
            result = (List) parent.getChildren(_itemType, _namePattern);
        }
        return result;
    }

    private Content getParentContent() {
        Content parent;
        if (isNotEmpty(_parentPath)) {
            parent = getContent(_repository, _parentPath);
        } else {
            parent = getAggregationState().getMainContent();
        }
        return parent;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        _var = null;
        _itemType = ITEM_TYPE_CONTENT;
        _namePattern = "*";
        _parentPath = null;
        _repository = WEBSITE;
        _sortContentNodeName = null;
        _sortNodeDataName = null;
        _reverseSortingOrder = false;
        super.release();
    }

    /**
     * This transformer tries to get the NodeData value from a Content object
     * that shoult be used for comparing (sorting) Content objects.
     *
     */
    private static final class ContentNodeDataTransformer implements Transformer<Content, Comparable> {

        private String _sortContentNodeName;
        private String _sortNodeDataName;

        private ContentNodeDataTransformer(String sortContentNodeName, String sortNodeDataName) {
            _sortContentNodeName = sortContentNodeName;
            _sortNodeDataName = sortNodeDataName;
        }

        /**
         * Tansforms a Content object into the value object of one of its NodeData values.
         * @param c the Content object to be transformed into a NodeData
         * @return the NodeData of the Content object or NULL if it does not exist.
         */
        public Comparable transform(Content c) {
            Content subContent = c;
            NodeData result = null;
            if (isNotEmpty(_sortContentNodeName) && subContent != null) {
                subContent = c.getChildByName(_sortContentNodeName);
            }
            if (isNotEmpty(_sortNodeDataName) && subContent != null) {
                result = subContent.getNodeData(_sortNodeDataName);
            }
            return getNodeDataValue(result);
        }

        private Comparable getNodeDataValue(NodeData data) {
            Comparable result = null;
            if (data != null) {
                int type = data.getType();
                switch (type) {
                    case PropertyType.STRING:
                        result = data.getString();
                        break;
                    case PropertyType.DATE:
                        result = data.getDate();
                        break;
                    case PropertyType.DOUBLE:
                        result = data.getDouble();
                        break;
                    case PropertyType.LONG:
                        result = data.getLong();
                        break;
                    case PropertyType.NAME:
                        result = data.getName();
                        break;
                    case PropertyType.PATH:
                        result = data.getHandle();
                        break;
                    default:
                        break;
                }
            }
            return result;
        }
    }

    /**
     * A Comparator that handles null values for objects to compare.
     * This may happen if the transformer could not locate the defined content node names (missing sorting criteria).
     * If the sorting criteria is missing the object (Content) will be added at the end of the list.
     */
    private static final class UncomparableAtEndComparatorWrapper implements Comparator<Object> {

        private Comparator<Object> _wrappedComparator = null;

        private UncomparableAtEndComparatorWrapper(Comparator<Object> wrappedComparator) {
            _wrappedComparator = wrappedComparator;
        }
                
        public int compare(Object obj1, Object obj2) {
            int result;
            if (notComparable(obj1)) {
                result = 1;
            } else if (notComparable(obj2)) {
                result = -1;
            } else {
                result = _wrappedComparator.compare(obj1, obj2);
            }
            return result;
        }

        private boolean notComparable(Object o) {
            return o == null || !Comparable.class.isAssignableFrom(o.getClass());
        }
    }
}
