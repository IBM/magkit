package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import com.aperto.magkit.utils.ImageData;
import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockNodeData;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.junit.matchers.StringContains.containsString;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Test of the image tag.
 *
 * @author frank.sommer (21.04.2008)
 */
public class ImageTagTest extends MagKitTagTest {

    private ImageTag _tag;

    @Test
    public void testDefaultBehaviour() {
        _tag.setNodeDataName("image");
        PageContext pageContext = runLifeCycle(_tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString("<img"));
        assertThat(output, containsString("alt=\"Alttext\""));
    }

    @Test
    public void testManualMeasures() {
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
    public void testImageDataUsage() {
        _tag.setImageDataName("imageData");
        PageContext pageContext = runLifeCycle(_tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString("<img"));
        assertThat(output, containsString("alt=\"Alttext\""));
    }

    @Override
    protected PageContext createPageContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        MockContent mockContent = new MockContent("page1", ItemType.CONTENT);
        mockContent.addNodeData(new MockNodeData("title", "layer 2"));
        MockContent nodeContent = new MockContent("content", ItemType.CONTENTNODE);
        nodeContent.addNodeData(new MockNodeData("imageAlt", "Alttext"));
        MockNodeData image = new MockNodeData("image", ImageTagTest.class.getResourceAsStream("/testimage.jpg"));
        nodeContent.addNodeData(image);
        mockContent.addContent(nodeContent);
        request.setAttribute("imageData", new ImageData(image, "Alttext"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        initMgnlWebContext(request, response, httpSession.getServletContext());
        initMgnlWebContext(request, response, httpSession.getServletContext());
        MgnlContext.getAggregationState().setMainContent(mockContent);
        MgnlContext.setAttribute("contentObj", nodeContent);
        return new MockPageContext(new MockServletConfig(), request, response);
    }

    @Before
    public void initTag() throws Exception {
        _tag = new ImageTag();
    }
}
