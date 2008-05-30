package com.aperto.magkit.controller;

import com.aperto.magkit.beans.GalleryEntry;
import com.aperto.magkit.utils.ImageData;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.link.LinkHelper;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.dms.DMSModule;
import info.magnolia.module.dms.beans.Document;
import info.magnolia.module.dms.util.PathUtil;
import org.apache.log4j.Logger;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

/**
 * Spring controller of the photo gallery.
 * A folder in dms is used for retrieving images. The needed thumbnails were generated at first time in a subfolder.
 * The thumbnail size can be configured by spring and is per default 65x65 pixels.
 * Cropping is per default on, but you can switch it off. Then the thumbnail has the same ratio like original image.
 * <code>
 * <bean id="photoGalleryController" class="com.aperto.magcit.mvc.controller.PhotoGalleryController">
 *       <property name="thumbHeight" value="65" />
 *       <property name="thumbWidth" value="65" />
 *       <property name="cropping" value="true" />
 * </bean>
 * </code>
 *
 * @author frank.sommer
 */
public class PhotoGalleryController extends AbstractController {
    private static final Logger LOGGER = Logger.getLogger(PhotoGalleryController.class);
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
        String dmsRepName = DMSModule.getInstance().getRepository();
        HierarchyManager manager = MgnlContext.getHierarchyManager(dmsRepName);
        Content localContent = Resource.getLocalContentNode();

        try {
            if (localContent != null && localContent.hasNodeData("folder")) {
                String folderPath = LinkHelper.convertUUIDtoHandle(localContent.getNodeData("folder").getString(), dmsRepName);
                Content folder = manager.getContent(folderPath);
                if (folder != null) {
                    Collection images = folder.getChildren(ItemType.CONTENTNODE);
                    if (images.size() > 0) {
                        List<GalleryEntry> imageList = new ArrayList<GalleryEntry>();
                        for (Object imgObj : images) {
                            GalleryEntry galleryEntry = new GalleryEntry();
                            Content content = (Content) imgObj;
                            Document originalDocument = new Document(content);
                            if (ArrayUtils.contains(IMAGE_EXTENSIONS, originalDocument.getFileExtension())) {
                                Document previewDocument = getPreviewImageDocument(originalDocument, manager);
                                galleryEntry.setImage(new ImageData(originalDocument));
                                galleryEntry.setThumbnail(new ImageData(previewDocument));
                                galleryEntry.setImageTitle(NodeDataUtil.getString(content, "subject", originalDocument.getFileName()));
                                galleryEntry.setImageDescription(NodeDataUtil.getString(content, "description"));
                                imageList.add(galleryEntry);
                            }
                        }
                        result.put("imageList", imageList);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception while getting gallery information.", e);
        }

        return new ModelAndView(_viewname, result);
    }

    /**
     * Create an image file that is a scaled version of the original image and returns
     * the document for that image.
     *
     * @param document the original document
     * @param manager  the HierarchyManager
     * @return the new image file
     */
    private Document getPreviewImageDocument(Document document, HierarchyManager manager) throws Exception {
        Document previewDocument = getPreviewDocumentForDocument(document, manager);

        if (!previewDocument.getFileName().equals(document.getFileName())) {
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
            } finally {
                if (oriImgStr != null) {
                    oriImgStr.close();
                }
                if (newImgStr != null) {
                    newImgStr.close();
                }
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
            cropWidth = thumbHeightRatio > oriHeightRatio ? (int) (oriWidth / thumbHeightRatio) : oriWidth;
            int xOffset = Math.max((oriWidth - cropWidth) / 2, 0);
            cropHeight = thumbWidthRatio > oriWidthRatio ? (int) (oriHeight / thumbWidthRatio) : oriHeight;
            int yOffset = Math.max((oriHeight - cropHeight) / 2, 0);
            newImg = oriImgBuff.getSubimage(xOffset, yOffset, cropWidth, cropHeight);
        } else {
            newImg = oriImgBuff;    
        }
        // get scale factor for the new image
        double scaleFactor = scaleFactor(oriWidth, oriHeight, width, height);
        // get the width and height of the new image
        int newWidth = new Double(cropWidth * scaleFactor).intValue();
        int newHeight = new Double(cropHeight * scaleFactor).intValue();
        newImg = newImg.getScaledInstance(newWidth, newHeight, Image.SCALE_AREA_AVERAGING);
        BufferedImage newImgBuff = new BufferedImage(newImg.getWidth(null), newImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
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
        scaleFactor = _cropping ? Math.max(scaleFactorWidth, scaleFactorHeight) : Math.min(scaleFactorWidth, scaleFactorHeight);
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
    private Document getPreviewDocumentForDocument(Document document, HierarchyManager manager) throws Exception {
        Document previewDocument = null;
        if (document.getNode().hasNodeData("relation1")) {
            String path = document.getNode().getNodeData("relation1").getString();
            Content tmp = manager.getContent(path);
            if (tmp != null) {
                previewDocument = new Document(tmp);
            }
        }

        String folderPath = PathUtil.getFolder(document.getPath());
        Content mainFolder = manager.getContent(folderPath);
        if (mainFolder != null) {
            Content previewFolder = ContentUtil.getContent(mainFolder, "previewFolder");
            if (previewFolder == null) {
                previewFolder = mainFolder.createContent("previewFolder");
                mainFolder.save();
            }

            Content previewImage = ContentUtil.getContent(previewFolder, document.getName());
            if (previewImage == null) {
                previewImage = previewFolder.createContent(document.getName(), ItemType.CONTENTNODE);
                previewFolder.save();
            }
            previewDocument = new Document(previewImage);
        }

        return previewDocument;
    }
}
