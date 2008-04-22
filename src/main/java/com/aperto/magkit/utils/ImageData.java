package com.aperto.magkit.utils;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import org.apache.commons.lang.StringUtils;
import javax.jcr.RepositoryException;

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
    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(LinkTool.class);

    /**
     * Constructor with some given data.
     * @param imageNode imageNode
     * @param alt alternative image text
     * @param repository node repository
     */
    public ImageData(NodeData imageNode, String alt, String repository) {
        _alt = alt;
        _handle = LinkTool.getBinaryLink(imageNode, repository);
        _height = imageNode.getAttribute(FileProperties.PROPERTY_HEIGHT);
        _width = imageNode.getAttribute(FileProperties.PROPERTY_WIDTH);
        _filesize = imageNode.getAttribute(FileProperties.SIZE);
    }

    /**
     * Constructor.
     * @param imageNode imageNode
     * @param alt alternative image text
     */
    public ImageData(NodeData imageNode, String alt) {
        _alt = alt;
        _handle = LinkTool.getBinaryLink(imageNode);
        _height = imageNode.getAttribute(FileProperties.PROPERTY_HEIGHT);
        _width = imageNode.getAttribute(FileProperties.PROPERTY_WIDTH);
        _filesize = imageNode.getAttribute(FileProperties.SIZE);
    }

    /**
     * Constructor.
     * @param content content with image node data
     * @param imageKey node data key for image.
     */
    public ImageData(Content content, String imageKey) {
        _alt = "";
        _handle = "";
        _height = "";
        _width = "";
        _filesize = "";
        try {
            if (content.hasNodeData(imageKey)) {
                NodeData imageNode = content.getNodeData(imageKey);
                if (imageNode != null) {
                    retrieveDataFromNode(imageNode);
                }
            }
            String altKey = imageKey + "Alt";
            if (content.hasNodeData(altKey)) {
                NodeData altNode = content.getNodeData(altKey);
                if (altNode != null && !StringUtils.isBlank(altNode.getString())) {
                    _alt = altNode.getString();
                }
            }
        } catch (RepositoryException e) {
            LOGGER.warn("Can not access repository.", e);
        }
    }

    /**
     * Constructor.
     * @param content content with image node data.
     * @param imageKey node data key for image.
     * @param altKey node data key for alt text.
     */
    public ImageData(Content content, String imageKey, String altKey) {
        _alt = "";
        _handle = "";
        _height = "";
        _width = "";
        _filesize = "";
        try {
            if (content.hasNodeData(imageKey)) {
                NodeData imageNode = content.getNodeData(imageKey);
                if (imageNode != null) {
                    retrieveDataFromNode(imageNode);
                }
            }
            if (content.hasNodeData(altKey)) {
                NodeData altNode = content.getNodeData(altKey);
                if (altNode != null && !StringUtils.isBlank(altNode.getString())) {
                    _alt = altNode.getString();
                }
            }
        } catch (RepositoryException e) {
            LOGGER.warn("Can not access repository.", e);
        }
    }

    /**
     * Constructor.
     * @param imageNode image node
     */
    public ImageData(NodeData imageNode) {
        retrieveDataFromNode(imageNode);
    }

    private void retrieveDataFromNode(NodeData imageNode) {
        _alt = "";
        _handle = LinkTool.getBinaryLink(imageNode);
        _height = imageNode.getAttribute(FileProperties.PROPERTY_HEIGHT);
        _width = imageNode.getAttribute(FileProperties.PROPERTY_WIDTH);
        _filesize = imageNode.getAttribute(FileProperties.SIZE);
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