package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubCurrentContent;
import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubMainContent;
import static com.aperto.magkit.mockito.ContentMockUtils.mockContent;
import static com.aperto.magkit.mockito.ContentStubbingOperation.*;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockAggregationState;
import static com.aperto.magkit.mockito.I18nContentSupportMockUtils.mockI18nContentSupport;
import static com.aperto.magkit.mockito.I18nContentSupportStubbingOperation.stubbNodeData;
import com.aperto.magkit.utils.ImageData;
import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.core.Content;
import static org.hamcrest.text.StringContains.containsString;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Test of the image tag.
 *
 * @author frank.sommer
 * @since 21.04.2008
 */
public class ImageTagTest extends MagKitTagTest {
    private ImageTag _tag;
    private Content _paragraphWithImage;

    @Test
    public void testDefaultBehaviour() throws JspException {
        _tag.setNodeDataName("image");
        PageContext pageContext = runLifeCycle(_tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString("<img"));
        assertThat(output, containsString("alt=\"Alttext\""));
        assertThat(output, containsString("width=\"200\""));
    }

    @Test
    public void testManualMeasures() throws JspException {
        _tag.setNodeDataName("image");
        _tag.setHeight("50em");
        _tag.setWidth("20em");
        PageContext pageContext = runLifeCycle(_tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString("<img"));
        assertThat(output, containsString("alt=\"Alttext\""));
        assertThat(output, containsString("height=\"50em\""));
        assertThat(output, containsString("width=\"20em\""));
    }

    @Test
    public void testImageDataUsage() throws JspException {
        _tag.setImageDataName("imageData");
        PageContext pageContext = runLifeCycle(_tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString("<img"));
        assertThat(output, containsString("alt=\"Alttext\""));
    }

    @Test
    public void testScalingImage() throws JspException {
        _tag.setImageDataName("imageData");
        _tag.setScaling(true);
        _tag.setScaleAtWidth(150);
        PageContext pageContext = runLifeCycle(_tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString("<img"));
        assertThat(output, containsString("alt=\"Alttext\""));
        assertThat(output, containsString("width=\"150\""));
        assertThat(output, containsString("height=\"225\""));
    }

    @Override
    protected PageContext createPageContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        request.setAttribute("imageData", getImageData());
        MockHttpServletResponse response = new MockHttpServletResponse();
        return new MockPageContext(new MockServletConfig(), request, response);
    }

    private ImageData getImageData() {
        return new ImageData(_paragraphWithImage.getNodeData("image"), "Alttext");
    }

    @Before
    public void setUp() throws Exception {
        _tag = new ImageTag();
        cleanContext();
        Map<String, String> imageAttributes = new HashMap<String, String>(6);
        imageAttributes.put("height", "300");
        imageAttributes.put("width", "200");
        imageAttributes.put("fileName", "testimage");
        imageAttributes.put("extension", "jpg");
        Content pageWithImage = mockContent("page1",
            stubTitle("layer 2"),
            stubChildContentNode("content",
                stubNodeData("imageAlt", "Alttext"),
                stubNodeData("image", ImageTagTest.class.getResourceAsStream("/testimage.jpg"), imageAttributes)
            )
        );
        _paragraphWithImage = pageWithImage.getContent("content");
        mockAggregationState(
            stubCurrentContent(_paragraphWithImage),
            stubMainContent(pageWithImage)
        );
        mockI18nContentSupport(
            stubbNodeData(_paragraphWithImage, "image", _paragraphWithImage.getNodeData("image"))
        );
    }
}