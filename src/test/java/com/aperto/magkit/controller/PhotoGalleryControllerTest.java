package com.aperto.magkit.controller;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.Before;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * Test of the photo gallery controller.
 *
 * @author frank.sommer (28.05.2008)
 */
public class PhotoGalleryControllerTest {
    private PhotoGalleryController _photoGalleryController;

    @Before
    public void initPhotoGalleryController() {
        _photoGalleryController = new PhotoGalleryController();
    }

    @Test
    public void testScaleImageLandscape() throws Exception {
        InputStream oriImgStr = PhotoGalleryControllerTest.class.getResourceAsStream("/testimage.jpg");
        File file = _photoGalleryController.scaleImage(ImageIO.read(oriImgStr), 60, 40);
        BufferedImage img = ImageIO.read(file);
        assertThat(img.getWidth(), is(60));
        assertThat(img.getHeight(), is(39));
    }

    @Test
    public void testScaleImagePotrait() throws Exception {
        InputStream oriImgStr = PhotoGalleryControllerTest.class.getResourceAsStream("/testimage.jpg");
        File file = _photoGalleryController.scaleImage(ImageIO.read(oriImgStr), 40, 60);
        BufferedImage img = ImageIO.read(file);
        assertThat(img.getWidth(), is(40));
        assertThat(img.getHeight(), is(60));
    }

    @Test
    public void testScaleImagePotraitWoCropping() throws Exception {
        InputStream oriImgStr = PhotoGalleryControllerTest.class.getResourceAsStream("/testimage.jpg");
        _photoGalleryController.setCropping(false);
        File file = _photoGalleryController.scaleImage(ImageIO.read(oriImgStr), 40, 60);
        BufferedImage img = ImageIO.read(file);
        assertThat(img.getWidth(), is(40));
        assertThat(img.getHeight(), is(39));
    }

    @Test
    public void testScaleImageGreaterOneSize() throws Exception {
        InputStream oriImgStr = PhotoGalleryControllerTest.class.getResourceAsStream("/testimage.jpg");
        File file = _photoGalleryController.scaleImage(ImageIO.read(oriImgStr), 120, 60);
        BufferedImage img = ImageIO.read(file);
        assertThat(img.getWidth(), is(120));
        assertThat(img.getHeight(), is(60));
    }

    @Test
    public void testScaleImageGreaterBoth() throws Exception {
        InputStream oriImgStr = PhotoGalleryControllerTest.class.getResourceAsStream("/testimage.jpg");
        File file = _photoGalleryController.scaleImage(ImageIO.read(oriImgStr), 120, 150);
        BufferedImage img = ImageIO.read(file);
        assertThat(img.getWidth(), is(119));
        assertThat(img.getHeight(), is(150));
    }
}