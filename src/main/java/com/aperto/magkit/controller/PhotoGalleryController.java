package com.aperto.magkit.controller;

import com.aperto.magkit.beans.GalleryEntry;
import com.aperto.magkit.utils.ImageData;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import static info.magnolia.cms.core.ItemType.CONTENTNODE;
import static info.magnolia.cms.util.ContentUtil.getContent;
import static info.magnolia.cms.util.NodeDataUtil.getString;
import static info.magnolia.context.MgnlContext.getAggregationState;
import static info.magnolia.context.MgnlContext.getHierarchyManager;
import static info.magnolia.link.LinkUtil.convertUUIDtoHandle;
import static info.magnolia.module.dms.DMSModule.getInstance;
import info.magnolia.module.dms.beans.Document;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.ArrayUtils.contains;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import static java.awt.Image.SCALE_AREA_AVERAGING;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.io.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.*;
import java.util.List;
import static java.util.Locale.ENGLISH;

/**
 * Spring controller of the photo gallery.
 * A folder in dms is used for retrieving images. The needed thumbnails were generated at first time in a subfolder.
 * The thumbnail size can be configured by spring and is per default 65x65 pixels.
 * Cropping is per default on, but you can switch it off. Then the thumbnail has the same ratio like original image.
 * <code>
 * &lt;bean id="photoGalleryController" class="com.aperto.magcit.mvc.controller.PhotoGalleryController"&gt;
 * &lt;property name="thumbHeight" value="65" /&gt;
 * &lt;property name="thumbWidth" value="65" /&gt;
 * &lt;property name="cropping" value="true" /&gt;
 * &lt;/bean&gt;
 * </code>
 * The images are set html escaped in the request variable <em>imageList</em>.
 *
 * @author frank.sommer
 */
public class PhotoGalleryController extends AbstractController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoGalleryController.class);
    private static final String[] IMAGE_EXTENSIONS = {"png", "jpg", "gif", "bmp", "jpeg"};
    private static final String DEFAULT_VIEWNAME = "paragraphs/photoGallery";
    private int _thumbWidth = 65;
    private int _thumbHeight = 65;
    private boolean _cropping = true;
    private String _viewname = DEFAULT_VIEWNAME;

    public void setThumbWidth(int thumbWidth) {
        _thumbWidth = thumbWidth;
    }

    public void setThumbHeight(int thumbHeight) {
        _thumbHeight = thumbHeight;
    }

    public void setCropping(boolean cropping) {
        _cropping = cropping;
    }

    public void setViewname(String viewname) {
        _viewname = viewname;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        Content localContent = getAggregationState().getCurrentContent();
        try {
            if (localContent != null && localContent.hasNodeData("folder")) {
                String folderPath = convertUUIDtoHandle(localContent.getNodeData("folder").getString(), getInstance().getRepository());
                if (isNotBlank(folderPath)) {
                    Content folder = getHierarchyManager(getInstance().getRepository()).getContent(folderPath);
                    if (folder != null) {
                        Collection images = folder.getChildren(CONTENTNODE);
                        if (images.size() > 0) {
                            List<GalleryEntry> imageList = retrieveImageList(images);
                            result.put("imageList", imageList);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception while getting gallery information.", e);
        }
        return new ModelAndView(_viewname, result);
    }

    private List<GalleryEntry> retrieveImageList(Collection images) throws Exception {
        List<GalleryEntry> imageList = new ArrayList<GalleryEntry>();
        for (Object imgObj : images) {
            GalleryEntry galleryEntry = new GalleryEntry();
            Content content = (Content) imgObj;
            Document originalDocument = new Document(content);
            if (contains(IMAGE_EXTENSIONS, originalDocument.getFileExtension().toLowerCase(ENGLISH))) {
                Document previewDocument = getPreviewImageDocument(originalDocument);
                if (previewDocument != null) {
                    galleryEntry.setImage(new ImageData(originalDocument));
                    galleryEntry.setThumbnail(new ImageData(previewDocument));
                    String imageTitle = getString(content, "subject", originalDocument.getFileName());
                    galleryEntry.setImageTitle(escapeHtml(imageTitle));
                    String description = getString(content, "description");
                    galleryEntry.setImageDescription(description);
                    imageList.add(galleryEntry);
                }
            }
        }
        return imageList;
    }

    /**
     * Create an image file that is a scaled version of the original image and returns
     * the document for that image.
     *
     * @param document the original document
     * @return the new image file
     */
    private Document getPreviewImageDocument(Document document) throws Exception {
        HierarchyManager manager = getHierarchyManager(getInstance().getRepository());
        Document previewDocument = getPreviewDocumentForDocument(document, manager);
        if (previewDocument != null && !previewDocument.getFileName().equals(document.getFileName())) {
            InputStream oriImgStr = null;
            InputStream newImgStr = null;
            File newImgFile = null;
            try {
                // get the original image, as a buffered image
                oriImgStr = document.getFileStream();
                BufferedImage oriImgBuff = ImageIO.read(oriImgStr);
                // create the new image file
                newImgFile = scaleImage(oriImgBuff, _thumbWidth, _thumbHeight);
                newImgStr = new FileInputStream(newImgFile);
                previewDocument.setFile(document.getName(), "png", newImgStr, newImgFile.length());
                previewDocument.setFileName(document.getFileName());
                previewDocument.updateMetaData();
                previewDocument.save();
            } catch (IIOException iioe) {
                LOGGER.info("Error reading image: " + document.getName());
                previewDocument = null;
            } finally {
                closeQuietly(oriImgStr);
                closeQuietly(newImgStr);
                if (newImgFile != null) {
                    newImgFile.delete();
                }
            }
        }
        return previewDocument;
    }

    /**
     * A thumbnail were created.
     * If configured at first the images were centrical cropped.
     * Then create a thumbnail image file by scaling the cropped image.
     * This can override by sub classes for another thumbnail scaling.
     *
     * @param oriImgBuff the original image file
     * @return the new image file
     */
    protected File scaleImage(BufferedImage oriImgBuff, int width, int height) throws IOException {
        // get the dimensions ant rations of the original image and the thumb image
        int oriWidth = oriImgBuff.getWidth();
        int oriHeight = oriImgBuff.getHeight();
        double oriWidthRatio = oriWidth > oriHeight ? (double) oriWidth / (double) oriHeight : 1d;
        double oriHeightRatio = oriHeight > oriWidth ? (double) oriHeight / (double) oriWidth : 1d;
        double thumbWidthRatio = width > height ? (double) width / (double) height : 1d;
        double thumbHeightRatio = height > width ? (double) height / (double) width : 1d;
        Image newImg;
        int cropWidth = oriWidth;
        int cropHeight = oriHeight;
        if (_cropping) {
            // create the thumbnail as a buffered image
            cropWidth = (int) (oriWidth * thumbWidthRatio / thumbHeightRatio * oriHeightRatio / oriWidthRatio);
            cropHeight = (int) (oriHeight * thumbHeightRatio / thumbWidthRatio * oriWidthRatio / oriHeightRatio);
            if (cropWidth > oriWidth) {
                cropWidth = oriWidth;
                cropHeight = (int) ((double) cropHeight / (double) cropWidth * (double) oriWidth);
            }
            if (cropHeight > oriHeight) {
                cropHeight = oriHeight;
                cropWidth = (int) ((double) cropWidth / (double) cropHeight * (double) oriHeight);
            }
            int xOffset = max((oriWidth - cropWidth) / 2, 0);
            int yOffset = max((oriHeight - cropHeight) / 2, 0);
            newImg = oriImgBuff.getSubimage(xOffset, yOffset, cropWidth, cropHeight);
        } else {
            newImg = oriImgBuff;
        }
        // get scale factor for the new image
        double scaleFactor = scaleFactor(oriWidth, oriHeight, width, height);
        // get the width and height of the new image
        int newWidth = new Double(cropWidth * scaleFactor).intValue();
        int newHeight = new Double(cropHeight * scaleFactor).intValue();
        newImg = newImg.getScaledInstance(newWidth, newHeight, SCALE_AREA_AVERAGING);
        BufferedImage newImgBuff = new BufferedImage(newImg.getWidth(null), newImg.getHeight(null), TYPE_INT_RGB);
        Graphics2D g = newImgBuff.createGraphics();
        g.drawImage(newImg, 0, 0, null);
        g.dispose();
        // create the new image file in the temporary dir
        File newImgFile = File.createTempFile("tmp-img", "png");
        ImageIO.write(newImgBuff, "png", newImgFile);
        // return the file
        return newImgFile;
    }

    /**
     * Calculate the scale factor for the image.
     *
     * @param width  the image width
     * @param height the image height
     * @return the scale factor
     */
    private double scaleFactor(int width, int height, int maxWidth, int maxHeight) {
        double scaleFactor;
        // create two scale factors, and see which is smaller
        double scaleFactorWidth = (double) maxWidth / (double) width;
        double scaleFactorHeight = (double) maxHeight / (double) height;
        scaleFactor = _cropping ? max(scaleFactorWidth, scaleFactorHeight) : min(scaleFactorWidth, scaleFactorHeight);
        return scaleFactor;
    }

    /**
     * Returns the preview document for the given original document.
     * Also creates the folder for all previewImages if it does not exist.
     *
     * @param document the original docuemnt
     * @param manager  the dms hierarchy manager
     * @return the preview document for the given original document
     */
    private Document getPreviewDocumentForDocument(Document document, HierarchyManager manager) {
        Document previewDocument = null;
        try {
            if (document.getNode().hasNodeData("relation1")) {
                String path = document.getNode().getNodeData("relation1").getString();
                Content tmp = manager.getContent(path);
                if (tmp != null) {
                    previewDocument = new Document(tmp);
                }
            }
            String folderPath = getFolder(document.getPath());
            Content mainFolder = manager.getContent(folderPath);
            if (mainFolder != null) {
                Content previewFolder = getContent(mainFolder, "previewFolder");
                if (previewFolder == null) {
                    previewFolder = mainFolder.createContent("previewFolder");
                    mainFolder.save();
                }
                Content previewImage = getContent(previewFolder, document.getName());
                if (previewImage == null) {
                    previewImage = previewFolder.createContent(document.getName(), CONTENTNODE);
                    previewFolder.save();
                }
                previewDocument = new Document(previewImage);
            }
        } catch (RepositoryException e) {
            LOGGER.info(e.getLocalizedMessage());
        }
        return previewDocument;
    }

    /**
     * Returns the last folder within the given path parameter.
     * <p/>
     * Implementation was copied from info.magnolia.module.dms.util.PathUtil class wich was moved to
     * info.magnolia.cms.util package in the final release of magnolia-module-dms version 1.3.
     */
    protected String getFolder(String path) {
        String res = substringBeforeLast(path, "/");
        return isEmpty(res) ? "/" : res;
    }
}