package com.aperto.magkit.beans;

import org.apache.log4j.Logger;
import com.aperto.magkit.utils.ImageData;

/**
 * Bean for a entry in the gallery.
 *
 * @author frank.sommer (27.05.2008)
 */
public class GalleryEntry {
    private static final Logger LOGGER = Logger.getLogger(GalleryEntry.class);

    private ImageData _thumbnail;
    private ImageData _image;
    private String _imageTitle;
    private String _imageDescription;

    public ImageData getThumbnail() {
        return _thumbnail;
    }

    public void setThumbnail(ImageData thumbnail) {
        _thumbnail = thumbnail;
    }

    public ImageData getImage() {
        return _image;
    }

    public void setImage(ImageData image) {
        _image = image;
    }

    public String getImageTitle() {
        return _imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        _imageTitle = imageTitle;
    }

    public String getImageDescription() {
        return _imageDescription;
    }

    public void setImageDescription(String imageDescription) {
        _imageDescription = imageDescription;
    }
}
