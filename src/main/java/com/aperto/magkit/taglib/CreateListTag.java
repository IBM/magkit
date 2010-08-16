package com.aperto.magkit.taglib;

import com.aperto.magkit.utils.Item;
import info.magnolia.cms.core.Content;
import static info.magnolia.context.MgnlContext.getAggregationState;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.split;
import org.apache.myfaces.tobago.apt.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Create a html list from a formatted string.
 * E.g. <br/>
 * Input: List1|item1;item2;item3 <br/>
 * Output: <code><h4>List1</h4><ul><li>item1</li>...</ul></code>
 *
 * @author frank.sommer
 * @since 16.01.2008
 */
@Tag(name = "createList", bodyContent = BodyContent.JSP)
public class CreateListTag extends TagSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateListTag.class);

    private String _nodeDataName;
    private String _listValue;
    private String _hlTag = "h4";
    private String _listTag = "ul";

    @TagAttribute
    /**
     * Node data name with the cared list value.
     */
    public void setNodeDataName(String nodeDataName) {
        _nodeDataName = nodeDataName;
    }

    @TagAttribute
    /**
     * List value.
     */
    public void setListValue(String listValue) {
        _listValue = listValue;
    }

    @TagAttribute
    /**
     * headline tag, default is h4.
     */
    public void setHlTag(String hlTag) {
        _hlTag = hlTag;
    }

    @TagAttribute
    /**
     * list tag, default is ul.
     */
    public void setListTag(String lTag) {
        _listTag = lTag;
    }

    /**
     * Writes the created list.
     *
     * @return jsp output
     * @throws javax.servlet.jsp.JspException
     */
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();
        // if nodeData is set, fetch the linkValue from CMS
        if (!isBlank(_nodeDataName)) {
            Content content = getAggregationState().getCurrentContent();
            try {
                if (content.hasNodeData(_nodeDataName)) {
                    _listValue = content.getNodeData(_nodeDataName).getString();
                }
            } catch (RepositoryException re) {
                LOGGER.warn("Can not access content node.", re);
            }
        }
        // write list
        if (!isBlank(_listValue)) {
            Item[] items = determineItems(_listValue);
            try {
                for (Item item : items) {
                    String key = item.getKey();
                    if (!isBlank(key)) {
                        out.write("<" + _hlTag + ">" + key + "</" + _hlTag + ">");
                    }
                    String value = item.getValue();
                    if (!isBlank(value)) {
                        String[] listItems = split(value, ';');
                        out.write("<" + _listTag + ">");
                        for (String listItem : listItems) {
                            out.write("<li>" + listItem + "</li>");
                        }
                        out.write("</" + _listTag + ">");
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error", e);
            }
        } else {
            LOGGER.info("No parameter is given for ConvertLinkTag.");
        }
        return super.doEndTag();
    }

    private Item[] determineItems(String listValue) {
        String newListvalue = listValue.trim();
        String[] lists = split(newListvalue, '\n');
        Item[] items = new Item[lists.length];
        for (int i = 0; i < lists.length; i++) {
            String[] fields = split(lists[i], '|');
            if (fields.length > 0) {
                if (fields.length == 1) {
                    items[i] = new Item("", fields[0]);
                } else {
                    items[i] = new Item(fields[0], fields[1]);
                }
            }
        }
        return items;
    }
}