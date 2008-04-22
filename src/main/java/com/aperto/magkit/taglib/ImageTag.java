package com.aperto.magkit.taglib;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.taglibs.BaseContentTag;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.aperto.magkit.utils.ImageData;

/**
 * Modified @see ImgTag from magnolia.
 * TODO: Make usage of ImageData
 *
 * @author frank.sommer (21.04.2008)
 */
@Tag(name = "image", bodyContent = BodyContent.JSP)
public class ImageTag extends BaseContentTag {

    private Map<String, String> _htmlAttributes = new HashMap<String, String>();
    private String _altNodeDataName;
    private String _imageDataName;

    /**
     * Setter for the request attribute name of an ImageData object.
     * @param imageDataName Name of the request attribute.
     */
    @TagAttribute
    public void setImageDataName(String imageDataName) {
        _imageDataName = imageDataName;
    }

    /**
     * Setter for <code>nodeDataName</code>.
     * @param nDataName The nodeDataName to set.
     */
    @TagAttribute
    public void setNodeDataName(String nDataName) {
        nodeDataName = nDataName;
    }

    /**
     * Setter for <code>_altNodeDataName</code>.
     * @param altNodeDataName The _altNodeDataName to set.
     */
    @TagAttribute
    public void setAltNodeDataName(String altNodeDataName) {
        _altNodeDataName = altNodeDataName;
    }

    /**
     * Setter for <code>height</code>.
     * @param value html attribute.
     */
    @TagAttribute
    public void setHeight(String value) {
        _htmlAttributes.put("height", value);
    }

    /**
     * Setter for <code>width</code>.
     * @param value html attribute.
     */
    @TagAttribute
    public void setWidth(String value) {
        _htmlAttributes.put("width", value);
    }

    /**
     * Setter for <code>class</code>.
     * @param value html attribute.
     */
    @TagAttribute
    public void setClass(String value) {
        _htmlAttributes.put("class", value);
    }

    /**
     * Setter for <code>style</code>.
     * @param value html attribute.
     */
    @TagAttribute
    public void setStyle(String value) {
        _htmlAttributes.put("style", value);
    }

    /**
     * Setter for <code>id</code>.
     * @param value html attribute.
     */
    @TagAttribute
    public void setId(String value) {
        _htmlAttributes.put("id", value);
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        int returnValue = EVAL_PAGE;
        ImageData imageData = null;
        JspWriter out = pageContext.getOut();

        if (!StringUtils.isBlank(_imageDataName)) {
            imageData = (ImageData) request.getAttribute(_imageDataName);
        }

        if (imageData != null) {
            writeToJsp(out, imageData, request.getContextPath());
            returnValue = super.doEndTag();
        } else {
            Content contentNode = getFirstMatchingNode();
            if (contentNode != null) {
                NodeData imageNodeData = contentNode.getNodeData(nodeDataName);
                if (imageNodeData.isExist()) {
                    imageData = new ImageData(imageNodeData, retrieveAltText(contentNode));
                    if (!StringUtils.isBlank(_htmlAttributes.get("width")) && !StringUtils.isBlank(_htmlAttributes.get("height"))) {
                        imageData.setHeight(_htmlAttributes.get("height"));
                        imageData.setWidth(_htmlAttributes.get("width"));
                    }
                    writeToJsp(out, imageData, request.getContextPath());                    
                    returnValue = super.doEndTag();
                }
            }
        }

        return returnValue;
    }

    private void writeToJsp(JspWriter out, ImageData imageData, String contextPath) {
        // don't modify the original map, remember tag pooling
        Map<String, String> attributes = new HashMap<String, String>(_htmlAttributes);
        try {
            if (StringUtils.lowerCase(imageData.getHandle()).endsWith(".swf")) {
                // TODO: handle flash movies like aperto
                out.write("<object type=\"application/x-shockwave-flash\" data=\"");
                out.write(contextPath);
                out.write(imageData.getHandle());
                out.write("\" ");
                writeAttributes(out, attributes);
                out.write(">");
                out.write("<param name=\"movie\" value=\"");
                out.write(contextPath);
                out.write(imageData.getHandle());
                out.write("\"/>");
                out.write("<param name=\"wmode\" value=\"transparent\"/>");
                out.write("</object>");
            } else {
                attributes.put("alt", imageData.getAlt());
                out.write("<img src=\"");
                out.write(contextPath);
                out.write(imageData.getHandle());
                out.write("\" ");
                writeAttributes(out, attributes);
                out.write("/>");
            }
        } catch (IOException e) {
            // should never happen
            throw new NestableRuntimeException(e);
        }
    }

    private String retrieveAltText(Content contentNode) {
        String altNodeDataNameDef = _altNodeDataName;
        if (StringUtils.isEmpty(altNodeDataNameDef)) {
            altNodeDataNameDef = nodeDataName + "Alt";
        }
        return contentNode.getNodeData(altNodeDataNameDef).getString();
    }

    private void writeAttributes(JspWriter out, Map<String, String> attributes) throws IOException {
        for (String name : attributes.keySet()) {
            String value = attributes.get(name);
            out.write(name);
            out.write("=\"");
            out.write(value);
            out.write("\" ");
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();
        _altNodeDataName = null;
        _htmlAttributes.clear();
    }
}