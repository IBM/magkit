package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import com.aperto.magkit.mock.MockContent;
import com.aperto.magkit.mock.MockNodeData;
import com.aperto.magkit.utils.ImageData;
import com.mockrunner.mock.web.MockPageContext;
import static info.magnolia.cms.core.ItemType.CONTENT;
import static info.magnolia.cms.core.ItemType.CONTENTNODE;
import static info.magnolia.context.MgnlContext.getAggregationState;
import static org.hamcrest.text.StringContains.containsString;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.*;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.*;

/**
 * Test of the image tag.
 *
 * @author frank.sommer
 * @since 21.04.2008
 */
public class ImageTagTest extends MagKitTagTest {
    private ImageTag _tag;

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
        MockContent mockContent = new MockContent("page1", CONTENT);
        mockContent.addNodeData(new MockNodeData("title", "layer 2"));
        MockContent nodeContent = new MockContent("content", CONTENTNODE);
        nodeContent.addNodeData(new MockNodeData("imageAlt", "Alttext"));
        MockNodeData image = new MockNodeData("image", ImageTagTest.class.getResourceAsStream("/testimage.jpg"));
        try {
            image.setAttribute("height", "300");
            image.setAttribute("width", "200");
            image.setAttribute("fileName", "testimage");
            image.setAttribute("extension", "jpg");
        } catch (RepositoryException e) {
            //Nothing
        }
        nodeContent.addNodeData(image);
        mockContent.addContent(nodeContent);
        request.setAttribute("imageData", new ImageData(image, "Alttext"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        initMgnlWebContext(request, response, httpSession.getServletContext());
        initMgnlWebContext(request, response, httpSession.getServletContext());
        getAggregationState().setMainContent(mockContent);
        getAggregationState().setCurrentContent(nodeContent);
        return new MockPageContext(new MockServletConfig(), request, response);
    }

    @Before
    public void initTag() throws Exception {
        _tag = new ImageTag();
    }
}