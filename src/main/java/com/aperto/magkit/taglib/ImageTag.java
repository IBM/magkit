package com.aperto.magkit.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import com.aperto.magkit.utils.ImageData;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.taglibs.BaseContentTag;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;

/**
 * Modified @see ImgTag from magnolia.
 *
 * @author frank.sommer (21.04.2008)
 */
@Tag(name = "image", bodyContent = BodyContent.JSP)
public class ImageTag extends BaseContentTag {

    private static final String ATTR_WIDTH = "width";
    private static final String ATTR_HEIGHT = "height";

    private Map<String, String> _htmlAttributes = new HashMap<String, String>();
    private String _nodeDataName;
    private String _altNodeDataName;
    private String _imageDataName;
    private boolean _scaling = false;
    private int _scaleAtHeight = 0;
    private int _scaleAtWidth = 0;

    /**
     * Setter for the request attribute name of an ImageData object.
     *
     * @param imageDataName Name of the request attribute.
     */
    @TagAttribute
    public void setImageDataName(String imageDataName) {
        _imageDataName = imageDataName;
    }

    /**
     * Setter for <code>nodeDataName</code>.
     *
     * @param nodeDataName The nodeDataName to set.
     */
    @TagAttribute
    public void setNodeDataName(String nodeDataName) {
        _nodeDataName = nodeDataName;
        super.setNodeDataName(nodeDataName);
    }

    /**
     * Setter for <code>_altNodeDataName</code>.
     *
     * @param altNodeDataName The _altNodeDataName to set.
     */
    @TagAttribute
    public void setAltNodeDataName(String altNodeDataName) {
        _altNodeDataName = altNodeDataName;
    }

    /**
     * Setter for <code>height</code>.
     *
     * @param value html attribute.
     */
    @TagAttribute
    public void setHeight(String value) {
        _htmlAttributes.put(ATTR_HEIGHT, value);
    }

    /**
     * Setter for <code>width</code>.
     *
     * @param value html attribute.
     */
    @TagAttribute
    public void setWidth(String value) {
        _htmlAttributes.put(ATTR_WIDTH, value);
    }

    /**
     * Setter for <code>class</code>.
     *
     * @param value html attribute.
     */
    @TagAttribute
    public void setClass(String value) {
        _htmlAttributes.put("class", value);
    }

    /**
     * Setter for <code>style</code>.
     *
     * @param value html attribute.
     */
    @TagAttribute
    public void setStyle(String value) {
        _htmlAttributes.put("style", value);
    }

    /**
     * Setter for <code>id</code>.
     *
     * @param value html attribute.
     */
    @TagAttribute
    public void setId(String value) {
        _htmlAttributes.put("id", value);
    }

    /**
     * Setter for <code>alt</code>.
     *
     * @param value html attribute.
     */
    @TagAttribute
    public void setAlt(String value) {
        _htmlAttributes.put("alt", value);
    }

    /**
     * Activate the scaling of the image.
     * Default is false.
     *
     * @see #_scaleAtHeight
     * @see #_scaleAtWidth
     */
    @TagAttribute
    public void setScaling(boolean scaling) {
        _scaling = scaling;
    }

    /**
     * If scaling true, the image will be scaled to this width.
     * For contortion set the width attribute.
     *
     * @see #_scaling
     */
    @TagAttribute
    public void setScaleAtWidth(int scaleAtWidth) {
        _scaleAtWidth = scaleAtWidth;
    }

    /**
     * If scaling true, the image will be scaled to this height.
     * For contortion set the height attribute.
     *
     * @see #_scaling
     */
    @TagAttribute
    public void setScaleAtHeight(int scaleAtHeight) {
        _scaleAtHeight = scaleAtHeight;
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

        if (imageData == null) {
            Content contentNode = getFirstMatchingNode();
            if (contentNode != null) {
                NodeData imageNodeData = contentNode.getNodeData(getNodeDataName());
                if (imageNodeData.isExist()) {
                    imageData = new ImageData(imageNodeData, retrieveAltText(contentNode));
                }
            }
        }

        if (imageData != null) {
            checkMeasures(imageData);
            writeToJsp(out, imageData, request.getContextPath());
            returnValue = super.doEndTag();
        }

        return returnValue;
    }

    private void checkMeasures(ImageData imageData) {
        if (!StringUtils.isBlank(_htmlAttributes.get(ATTR_WIDTH)) && !StringUtils.isBlank(_htmlAttributes.get(ATTR_HEIGHT))) {
            imageData.setHeight(_htmlAttributes.get(ATTR_HEIGHT));
            imageData.setWidth(_htmlAttributes.get(ATTR_WIDTH));
        }
    }

    private void writeToJsp(JspWriter out, ImageData imageData, String contextPath) {
        // don't modify the original map, remember tag pooling
        Map<String, String> attributes = new HashMap<String, String>(_htmlAttributes);
        attributes.put(ATTR_HEIGHT, imageData.getHeight());
        attributes.put(ATTR_WIDTH, imageData.getWidth());
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
                calculateNewMeasures(attributes);
                writeAttributes(out, attributes);
                out.write("/>");
            }
        } catch (IOException e) {
            // should never happen
            throw new NestableRuntimeException(e);
        }
    }

    private void calculateNewMeasures(Map<String, String> attributes) {
        if (_scaling && (_scaleAtHeight > 0 || _scaleAtWidth > 0)) {
            int width = NumberUtils.toInt(attributes.get(ATTR_WIDTH));
            int height = NumberUtils.toInt(attributes.get(ATTR_HEIGHT));
            if ((_scaleAtWidth > 0) && (width > 0)) {
                attributes.put(ATTR_WIDTH, Integer.toString(_scaleAtWidth));
                attributes.put(ATTR_HEIGHT, Integer.toString((height * _scaleAtWidth) / width));
            } else if (height > 0) {
                attributes.put(ATTR_WIDTH, Integer.toString((width * _scaleAtHeight) / height));
                attributes.put(ATTR_HEIGHT, Integer.toString(_scaleAtHeight));
            }
        }
    }

    private String retrieveAltText(Content contentNode) {
        String altNodeDataNameDef = _altNodeDataName;
        if (StringUtils.isEmpty(altNodeDataNameDef)) {
            altNodeDataNameDef = getNodeDataName() + "Alt";
        }
        return contentNode.getNodeData(altNodeDataNameDef).getString();
    }

    private void writeAttributes(JspWriter out, Map<String, String> attributes) throws IOException {
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            out.write(entry.getKey());
            out.write("=\"");
            out.write(entry.getValue());
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

    protected String getNodeDataName() {
        return _nodeDataName;
    }
}