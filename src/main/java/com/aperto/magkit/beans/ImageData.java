package com.aperto.magkit.beans;

import info.magnolia.cms.core.NodeData;
import com.aperto.magkit.utils.LinkTool;

/**
 * Class to capsulate the image data.
 * @author frank.sommer (23.11.2007)
 */
public class ImageData {
    private String _width;
    private String _height;
    private String _alt;
    private String _handle;
    private String _filesize;
    public static final String ATTR_HEIGHT = "height";
    public static final String ATTR_WIDTH = "width";
    public static final String ATTR_SIZE_KB = "sizeKB";

    /**
     * Constructor with some given data.
     * @param imageNode imageNode
     * @param alt alternative image text
     * @param repository node repository
     */
    public ImageData(NodeData imageNode, String alt, String repository) {
        _alt = alt;
        _handle = LinkTool.getBinaryLink(imageNode, repository);
        _height = imageNode.getAttribute(ATTR_HEIGHT);
        _width = imageNode.getAttribute(ATTR_WIDTH);
        _filesize = imageNode.getAttribute(ATTR_SIZE_KB);
    }

    /**
     * Constructor.
     * @param imageNode imageNode
     * @param alt alternative image text
     */
    public ImageData(NodeData imageNode, String alt) {
        _alt = alt;
        _handle = LinkTool.getBinaryLink(imageNode);
        _height = imageNode.getAttribute(ATTR_HEIGHT);
        _width = imageNode.getAttribute(ATTR_WIDTH);
        _filesize = imageNode.getAttribute(ATTR_SIZE_KB);
    }

    /**
     * Constructor.
     * @param imageNode image node
     */
    public ImageData(NodeData imageNode) {
        _alt = "";
        _handle = LinkTool.getBinaryLink(imageNode);
        _height = imageNode.getAttribute(ATTR_HEIGHT);
        _width = imageNode.getAttribute(ATTR_WIDTH);
        _filesize = imageNode.getAttribute(ATTR_SIZE_KB);
    }

    /**
     * Default constructor.
     */
    public ImageData() {
        _alt = "";
        _handle = "";
        _height = "";
        _width = "";
        _filesize = "";
    }

    public String getWidth() {
        return _width;
    }

    public void setWidth(String width) {
        _width = width;
    }

    public String getHeight() {
        return _height;
    }

    public void setHeight(String height) {
        _height = height;
    }

    public String getAlt() {
        return _alt;
    }

    public void setAlt(String alt) {
        _alt = alt;
    }

    public String getHandle() {
        return _handle;
    }

    public void setHandle(String handle) {
        _handle = handle;
    }

    public String getFilesize() {
        return _filesize;
    }

    public void setFilesize(String filesize) {
        _filesize = filesize;
    }
}