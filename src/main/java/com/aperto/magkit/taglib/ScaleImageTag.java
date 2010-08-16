package com.aperto.magkit.taglib;
/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */

import static com.aperto.magkit.utils.LinkTool.convertLink;
import static info.magnolia.cms.beans.config.ContentRepository.WEBSITE;
import info.magnolia.cms.core.*;
import static info.magnolia.cms.core.ItemType.CONTENTNODE;
import info.magnolia.cms.taglibs.util.BaseImageTag;
import static info.magnolia.cms.util.NodeDataUtil.getOrCreate;
import info.magnolia.context.MgnlContext;
import static info.magnolia.context.MgnlContext.getHierarchyManager;
import info.magnolia.module.dms.beans.Document;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.substringAfter;
import org.apache.myfaces.tobago.apt.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import static javax.jcr.PropertyType.BINARY;
import static javax.jcr.PropertyType.STRING;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import static java.io.File.createTempFile;
import java.util.Calendar;

/**
 * Tag that creates a scaled copy of an image. The maximum width and height of the images can be specified via the
 * attributes. <br />
 * <br />
 * If the scaled image with the specified name does not exist in the repository, then this tag will create it and save
 * it. If the scaled image already exists, then it will not be recreated. <br />
 * <br />
 * The name of the node that contains the original image is set by the attribute 'parentContentNode', and the name of
 * the nodeData for the image is set by the attribute 'parentNodeDataName'. If 'parentContentNode' is null, the local
 * content node is used. <br />
 * <br />
 * The name of the content node that contains the new scaled image is set by the attribute 'imageContentNodeName'. This
 * node is created under the original image node. This ensures that, if the original images is deleted, so are all the
 * scaled versions. <br />
 * <br />
 * This tag writes out the handle of the content node that contains the image. <br />
 * <br />
 * You can also scale images from dms.<br />
 * <br />
 *
 * @author Patrick Janssen
 * @author Fabrizio Giustina
 * @author diana.racho
 * @version 1.0
 */
@Tag(name = "scaleImage", bodyContent = BodyContent.EMPTY)
public class ScaleImageTag extends BaseImageTag {

    /**
     * Location for folder for temporary image creation.
     */
    private static final String TEMP_IMAGE_NAME = "tmp-img";

    /**
     * The value of the extension nodeData in the properties node.
     */
    private static final String PROPERTIES_EXTENSION_VALUE = "png";

    /**
     * DMS repository name.
     */
    private static final String REPOSITORY_DMS = "dms";

    /**
     * Attribute: Image maximum height.
     */
    private int _maxHeight = 0;

    /**
     * Attribute: Image maximum width.
     */
    private int _maxWidth = 0;

    /**
     * Attribute: The name of the new content node to create.
     */
    private String _imageContentNodeName;

    /**
     * Attribute: The name of the data node that contains the existing image.
     */
    private String _parentNodeDataName;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ScaleImageTag.class);

    /**
     * Setter for the <code>maxHeight</code> tag attribute.
     *
     * @param maxHeight max height
     */
    @TagAttribute
    public void setMaxHeight(int maxHeight) {
        _maxHeight = maxHeight;
    }

    /**
     * Setter for the <code>maxWidth</code> tag attribute.
     *
     * @param maxWidth max width
     */
    @TagAttribute
    public void setMaxWidth(int maxWidth) {
        _maxWidth = maxWidth;
    }

    /**
     * Setter for the <code>parentContentNodeName</code> tag attribute.
     *
     * @param parentContentName name of parent content node
     */
    @TagAttribute
    public void setParentContentNodeName(String parentContentName) {
        parentContentNodeName = parentContentName;
    }

    /**
     * Setter for the <code>parentNodeDataName</code> tag attribute.
     *
     * @param parentNodeDataName name of image node data
     */
    @TagAttribute(required = true)
    public void setParentNodeDataName(String parentNodeDataName) {
        _parentNodeDataName = parentNodeDataName;
    }

    /**
     * Setter for the <code>imageContentNodeName</code> tag attribute.
     *
     * @param imageContentName name of content node of image
     */
    @TagAttribute(required = true)
    public void setImageContentNodeName(String imageContentName) {
        _imageContentNodeName = imageContentName;
    }

    /**
     * Do thia tag.
     */
    public void doTag() throws JspException {
        Content parentContentNode;
        Content imageContentNode;
        JspWriter out = getJspContext().getOut();
        try {
            AggregationState state = MgnlContext.getAggregationState();
            // set the parent node that contains the original image
            if (isBlank(parentContentNodeName)) {
                parentContentNode = state.getCurrentContent();
            } else {
                HierarchyManager hm = getHierarchyManager(WEBSITE);
                // if this name starts with a '/', then assume it is a node handle
                // otherwise assume that its is a path relative to the local content node
                if (parentContentNodeName.startsWith("/")) {
                    parentContentNode = hm.getContent(parentContentNodeName);
                } else {
                    String handle = state.getCurrentContent().getHandle();
                    parentContentNode = hm.getContent(handle + "/" + parentContentNodeName);
                }
            }
            // check if the new image node exists, if not then create it
            if (parentContentNode.hasContent(_imageContentNodeName)) {
                imageContentNode = parentContentNode.getContent(_imageContentNodeName);
            } else {
                imageContentNode = parentContentNode.createContent(_imageContentNodeName, CONTENTNODE);
                parentContentNode.save();
            }
            // if the node does not have the image data or should be rescaled (i.e., something has c
            // then create the image data
            if (!imageContentNode.hasNodeData(_parentNodeDataName) || rescale(parentContentNode, imageContentNode)) {
                createImageNodeData(parentContentNode, imageContentNode);
            }
            // write out the handle for the new image and exit
            StringBuilder handle = new StringBuilder(imageContentNode.getHandle());
            handle.append("/");
            handle.append(getFilename());
            handle.append("." + PROPERTIES_EXTENSION_VALUE);
            out.write(handle.toString());
        } catch (RepositoryException e) {
            LOGGER.error("RepositoryException occured in ScaleImage tag: {}.", e.getMessage(), e);
        } catch (FileNotFoundException e) {
            LOGGER.error("FileNotFoundException occured in ScaleImage tag: {}.", e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error("IOException occured in ScaleImage tag: {}.", e.getMessage(), e);
        }
        cleanUp();
    }

    /**
     * Checks to see if the previously scaled image needs to be rescaled. This is true when the parent content node has.
     * been updated or the height or width parameters have changed.
     *
     * @param parentContentNode The node containing the scaled image node
     * @param imageContentNode  The scaled image node
     * @return true when the parent content node has been updated or the height or width parameters have changed
     */
    protected boolean rescale(Content parentContentNode, Content imageContentNode) {
        boolean hasChanged;
        Calendar parentModified = parentContentNode.getMetaData().getModificationDate() != null ? parentContentNode
            .getMetaData()
            .getModificationDate() : parentContentNode.getMetaData().getCreationDate();
        Calendar imageModified = imageContentNode.getMetaData().getModificationDate() != null ? imageContentNode
            .getMetaData()
            .getModificationDate() : imageContentNode.getMetaData().getCreationDate();
        if (parentModified.after(imageModified)) {
            hasChanged = true;
        } else {
            int originalHeight = (int) imageContentNode.getNodeData("maxHeight").getLong();
            int originalWidth = (int) imageContentNode.getNodeData("maxWidth").getLong();
            hasChanged = originalHeight != _maxHeight || originalWidth != _maxWidth;
        }
        return hasChanged;
    }

    /**
     * Set objects to null.
     */
    public void cleanUp() {
        _parentNodeDataName = null;
        _imageContentNodeName = null;
        _maxWidth = 0;
        _maxHeight = 0;
    }

    /**
     * Create an image file that is a scaled version of the original image.
     *
     * @param parentContentNode parent node
     * @param imageContentNode  node
     * @throws java.io.IOException           while reading image file
     * @throws javax.jcr.RepositoryException while creating new nodes
     */
    private void createImageNodeData(Content parentContentNode, Content imageContentNode) throws RepositoryException, IOException {
        InputStream oriImgStr = null;
        try {
            if (parentContentNode.getNodeData(_parentNodeDataName).getType() == BINARY) {
                oriImgStr = parentContentNode.getNodeData(_parentNodeDataName).getStream();
            } else if (parentContentNode.getNodeData(_parentNodeDataName).getType() == STRING) {
                String dmsLink = substringAfter(convertLink(parentContentNode.getNodeData(_parentNodeDataName).getString(), false, REPOSITORY_DMS), REPOSITORY_DMS);
                Content dmsContent = getHierarchyManager(REPOSITORY_DMS).getContent(dmsLink);
                oriImgStr = new Document(dmsContent).getFileStream();
            }
            // get the original image, as a buffered image
            BufferedImage oriImgBuff = ImageIO.read(oriImgStr);
            if (oriImgBuff != null) {
                // create the new image file
                File newImgFile = scaleImage(oriImgBuff);
                getOrCreate(imageContentNode, "maxHeight").setValue(_maxHeight);
                getOrCreate(imageContentNode, "maxWidth").setValue(_maxWidth);
                createImageNode(newImgFile, imageContentNode);
                newImgFile.delete();
            } else {
                throw new IOException("Can't read image stream.");
            }
        } finally {
            closeQuietly(oriImgStr);
        }
    }

    /**
     * Create an image file that is a scaled version of the original image.
     *
     * @param oriImgBuff the original image file
     * @return the new image file
     * @throws java.io.IOException when Exception while creating temp image file
     */
    private File scaleImage(BufferedImage oriImgBuff) throws IOException {
        // get the dimesnions of the original image
        int oriWidth = oriImgBuff.getWidth();
        int oriHeight = oriImgBuff.getHeight();
        // get scale factor for the new image
        double scaleFactor = scaleFactor(oriWidth, oriHeight);
        // get the width and height of the new image
        int newWidth = scale(oriWidth, scaleFactor);
        int newHeight = scale(oriHeight, scaleFactor);
        // create the thumbnail as a buffered image
        Image newImg = oriImgBuff.getScaledInstance(newWidth, newHeight, Image.SCALE_AREA_AVERAGING);
        BufferedImage newImgBuff = new BufferedImage(
            newImg.getWidth(null),
            newImg.getHeight(null),
            BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImgBuff.createGraphics();
        g.drawImage(newImg, 0, 0, null);
        g.dispose();
        // create the new image file in the temporary dir
        File newImgFile = createTempFile(TEMP_IMAGE_NAME, PROPERTIES_EXTENSION_VALUE);
        ImageIO.write(newImgBuff, PROPERTIES_EXTENSION_VALUE, newImgFile);
        // return the file
        return newImgFile;
    }

    static int scale(final int oriValue, final double scaleFactor) {
        return (int) (oriValue * scaleFactor);
    }

    /**
     * Calculate the scale factor for the image.
     *
     * @param width  the image width
     * @param height the image height
     * @return the scale factor
     */
    private double scaleFactor(int width, int height) {
        double scaleFactor;
        if (_maxWidth <= 0 && _maxHeight <= 0) {
            // may a copy at the same size
            scaleFactor = 1;
        } else if (_maxWidth <= 0) {
            // use height
            scaleFactor = (double) _maxHeight / (double) height;
        } else if (_maxHeight <= 0) {
            // use width
            scaleFactor = (double) _maxWidth / (double) width;
        } else {
            // create two scale factors, and see which is smaller
            double scaleFactorWidth = (double) _maxWidth / (double) width;
            double scaleFactorHeight = (double) _maxHeight / (double) height;
            scaleFactor = Math.min(scaleFactorWidth, scaleFactorHeight);
        }
        return scaleFactor;
    }

    protected String getFilename() {
        return _parentNodeDataName;
    }
}