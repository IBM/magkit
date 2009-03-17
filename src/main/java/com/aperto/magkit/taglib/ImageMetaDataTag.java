package com.aperto.magkit.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.aperto.magkit.utils.ImageData;
import info.magnolia.cms.core.Content;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;

/**
 * Makes the meta data of an image content node available in page context. Exposes width, heigth, filesize and alt
 * text. This tag becomes obsolete with magnolia 4.0 version, where access to metadata is possible by expression
 * language.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
@Tag(name = "imageMetaData")
public class ImageMetaDataTag extends TagSupport {

    private Content _parentContentNode;
    private String _imageNodeName;
    private String _var;

    @TagAttribute(required = true)
    public void setParentContentNode(final Content parentContentNode) {
        _parentContentNode = parentContentNode;
    }

    @TagAttribute(required = true)
    public void setImageNodeName(final String imageNodeName) {
        _imageNodeName = imageNodeName;
    }

    @TagAttribute(required = true)
    public void setVar(final String var) {
        _var = var;
    }

    /**
     * Adds {@link com.aperto.magkit.utils.ImageData} object instance to current page context using name set by 'var' attribute.
     */
    @Override
    public int doStartTag() throws JspException {
        ImageData imageData = new ImageData(_parentContentNode, _imageNodeName);
        pageContext.setAttribute(_var, imageData);
        return super.doStartTag();
    }
}