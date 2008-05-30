package com.aperto.magkit.controls;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.gui.dialog.DialogFile;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * It is made to validate an especial image that should be uploaded via a mgnl dialog. It validates
 * filesize, height, width and fileextension.
 * <p/>
 * If you use this control, the user wont be able to upload "bad" images.
 *
 * @author rainer.blumenthal, frank.sommer
 *         Date: 02.04.2008
 *         Time: 15:15:17
 */

public class DialogValidateImageFile extends DialogFile {
    private static final Logger LOGGER = Logger.getLogger(DialogValidateImageFile.class);
    private String _errorMessage = "";
    /**
     * Cms config attribute for allowed extensions.
     */
    private static final String CONFIG_ALLOWED_EXTENSIONS = "allowedExtensions";
    /**
     * Cms config attribute for maximum allowed filesize.
     */
    private static final String CONFIG_MAX_FILESIZE = "maxFilesize";
    /**
     * Cms config attribute for maximum allowed image height.
     */
    private static final String CONFIG_MAX_HEIGHT = "maxHeight";
    /**
     * Cms config attribute for maximum allowed image width.
     */
    private static final String CONFIG_MAX_WIDTH = "maxWidth";

    // default values
    private static final String DEFAULT_EXTENSIONS = "png,jpg,gif,jpeg";
    // 512 K
    private static final String DEFAULT_SIZE = "524288";
    private static final String DEFAULT_HEIGHT = "600";
    private static final String DEFAULT_WIDTH = "500";

    /**
     * Validates the format, the extension, the size and the dimension.
     * Following validation messages has to be set in cms property file:
     * <ul>
     * <li>cms.validator.unknownFile</li>
     * <li>cms.validator.filesize</li>
     * <li>cms.validator.format</li>
     * <li>cms.validator.dimension</li>
     * </ul>
     */
    public boolean validate() {
        boolean isValid = super.validate();

        if (isValid) {
            MultipartForm mf = (MultipartForm) getRequest().getAttribute("multipartform");
            Document doc = mf.getDocument("image");

            // for "Remove Image" in Dialog - still working
            if (doc != null) {
                String fileExtension = doc.getExtension().toLowerCase();
                isValid = validateImageFile(doc, fileExtension);
                if (!isValid) {
                    setValidationMessage(_errorMessage);
                }
            }
        }
        return isValid;
    }

    private boolean validateImageFile(Document doc, String fileExtension) {
        boolean valid = true;
        File file = doc.getFile();
        InputStream inputStream = null;
        try {
            inputStream = doc.getStream();
            BufferedImage imgBuffer = ImageIO.read(inputStream);

            // get configs from control config in cms
            String allowedFileExtensions = getConfigValue(CONFIG_ALLOWED_EXTENSIONS, DEFAULT_EXTENSIONS);
            long maxFilesize = NumberUtils.toInt(getConfigValue(CONFIG_MAX_FILESIZE, DEFAULT_SIZE));
            int maxHeight = NumberUtils.toInt(getConfigValue(CONFIG_MAX_HEIGHT, DEFAULT_HEIGHT));
            int maxWidth = NumberUtils.toInt(getConfigValue(CONFIG_MAX_WIDTH, DEFAULT_WIDTH));
            // validate file size
            if (file.length() > maxFilesize) {
                _errorMessage = "cms.validator.filesize";
                LOGGER.debug("The file could not be uploaded - max. " + maxFilesize / 1024 + " KB allowed.");
                valid = false;
            }
            //validate file extensions
            if (valid && !StringUtils.contains(allowedFileExtensions, fileExtension)) {
                _errorMessage = "cms.validator.format";
                LOGGER.debug("Tried to upload wrong filetype, it is not one of the following: " + allowedFileExtensions);
                valid = false;
            }
            //validate image dimension

            if (valid && (imgBuffer.getHeight() > maxHeight || imgBuffer.getWidth() > maxWidth)) {
                _errorMessage = "cms.validator.dimension";
                LOGGER.debug("Image Height is bigger then " + maxHeight + " - cannot save Image.");
                valid = false;
            }
        } catch (IOException ioe) {
            _errorMessage = "cms.validator.unknownFile";
            LOGGER.info("Can not read from inputstream.");
            valid = false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.info("Could not close inputstream.");
                }
            }
        }
        return valid;
    }
}
