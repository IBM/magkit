package com.aperto.magkit.taglib;

import com.aperto.magkit.utils.Item;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
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
 * @author frank.sommer (16.01.2008)
 */
@Tag(name = "createList", bodyContent = BodyContent.JSP)
public class CreateListTag extends TagSupport {
    private static final Logger LOGGER = Logger.getLogger(CreateListTag.class);

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
     * @return jsp output
     * @throws javax.servlet.jsp.JspException
     */
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();

        // if nodeData is set, fetch the linkValue from CMS
        if (!StringUtils.isBlank(_nodeDataName)) {
            Content content = Resource.getLocalContentNode();
            try {
                if (content.hasNodeData(_nodeDataName)) {
                    _listValue = content.getNodeData(_nodeDataName).getString();
                }
            } catch (RepositoryException re) {
                LOGGER.warn("Can not access content node.", re);
            }
        }
        // write list
        if (!StringUtils.isBlank(_listValue)) {
            Item[] items = determineItems(_listValue);
            try {
                for (Item item : items) {
                    String key = item.getKey();
                    if (!StringUtils.isBlank(key)) {
                        StringBuffer sb = new StringBuffer();
                        sb.append("<").append(_hlTag).append(">").append(key).append("</").append(_hlTag).append(">");
                        out.write(sb.toString());
                    }
                    String value = item.getValue();
                    if (!StringUtils.isBlank(value)) {
                        String[] listItems = StringUtils.split(value, ';');
                        out.write("<" + _listTag + ">");
                        for (String listItem : listItems) {
                            StringBuffer sb = new StringBuffer();
                            sb.append("<li>").append(listItem).append("</li>");
                            out.write(sb.toString());
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
        String[] lists = StringUtils.split(newListvalue, '\n');
        Item[] items = new Item[lists.length];
        for (int i = 0; i < lists.length; i++) {
            String[] fields = StringUtils.split(lists[i], '|');
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
