package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import com.aperto.magkit.mock.MockContent;
import com.aperto.magkit.mock.MockNodeData;

import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.StringContains.containsString;
import static org.hamcrest.text.StringEndsWith.endsWith;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Test of the convert link tag.
 *
 * @author frank.sommer (10.01.2008)
 */
public class ConvertLinkTagTest extends MagKitTagTest {
    private static final String LOCAL_CONTENT_OBJ = "contentObj";
    private static final String CONTEXT_PATH = "/author";
    private static final String LINK_VALUE_EXT = "http://www.aperto.de/test.html";
    private static final String LINK_VALUE_INT = "/sammeln/infos";
    private static final String LINK_VALUE_INT_SHORT = "/sammeln";
    private static final String LINK_VALUE_UUID = "29f35061-bf9f-478c-a4b0-cb9f07a0fc8c";

    @Test
    public void testExternalLink() throws JspException {
        ConvertLinkTag tag = new ConvertLinkTag();
        tag.setNodeDataName("link");
        PageContext pageContext = runLifeCycle(tag, "link", LINK_VALUE_EXT);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, not(containsString(CONTEXT_PATH)));
        assertThat(output, is(LINK_VALUE_EXT));
    }

    @Test
    public void testUuidLink() throws JspException {
        ConvertLinkTag tag = new ConvertLinkTag();
        tag.setNodeDataName("link4");
        PageContext pageContext = runLifeCycle(tag, "link4", LINK_VALUE_UUID);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output.length(), is(0));
    }

    @Test
    public void testInternalLink() throws JspException {
        ConvertLinkTag tag = new ConvertLinkTag();
        tag.setNodeDataName("link2");
        PageContext pageContext = runLifeCycle(tag, "link2", LINK_VALUE_INT);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString(CONTEXT_PATH));
        assertThat(output, containsString(LINK_VALUE_INT));
        assertThat(output, endsWith("html"));
    }

    @Test
    public void testInternalLinkVar() throws JspException {
        ConvertLinkTag tag = new ConvertLinkTag();
        tag.setNodeDataName("link2");
        tag.setVar("link");
        PageContext pageContext = runLifeCycle(tag, "link2", LINK_VALUE_INT);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output.length(), is(0));
        String var = (String) pageContext.getRequest().getAttribute("link");
        assertThat(var, containsString(CONTEXT_PATH));
        assertThat(var, containsString(LINK_VALUE_INT));
        assertThat(var, endsWith("html"));
    }

    @Test
    public void testInternalLinkWithSelector() throws JspException {
        ConvertLinkTag tag = new ConvertLinkTag();
        tag.setNodeDataName("link2");
        tag.setSelector("navid-123");
        PageContext pageContext = runLifeCycle(tag, "link2", LINK_VALUE_INT);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString(CONTEXT_PATH));
        assertThat(output, containsString(LINK_VALUE_INT));
        assertThat(output, endsWith("123.html"));
    }

    @Test
    public void testInternalLinkWithSelectorShort() throws JspException {
        ConvertLinkTag tag = new ConvertLinkTag();
        tag.setNodeDataName("link3");
        tag.setSelector("navid-123");
        PageContext pageContext = runLifeCycle(tag, "link3", LINK_VALUE_INT_SHORT);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString(CONTEXT_PATH));
        assertThat(output, containsString(LINK_VALUE_INT_SHORT));
        assertThat(output, endsWith("123.html"));
    }

    @Test
    public void testLinkWithSpaces() throws JspException {
        ConvertLinkTag tag = new ConvertLinkTag();
        tag.setNodeDataName("link");
        PageContext pageContext = runLifeCycle(tag, "link", "/das ist ein Link");
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString(CONTEXT_PATH));

    }


    protected PageContext runLifeCycle(TagSupport tag, String nodeDataName, String nodeDataValue) throws JspException {
        PageContext pageContext = createPageContext(nodeDataName, nodeDataValue);
        runLifeCycle(tag, pageContext);
        return pageContext;
    }

    protected PageContext createPageContext(String nodeDataName, String nodeDataValue) {
        MockContent mockContent = createMockContent(nodeDataName, nodeDataValue);
        MockHttpSession httpSession = new MockHttpSession();
        MockHttpServletRequest request = createMockRequest(mockContent, httpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();
        initMagnoliaContext(request, response, httpSession, mockContent);
        return new MockPageContext(new MockServletConfig(), request, response);
    }

    private void initMagnoliaContext(MockHttpServletRequest request, MockHttpServletResponse response, MockHttpSession httpSession, MockContent mockContent) {
        initMgnlWebContext(request, response, httpSession.getServletContext());
        MgnlContext.getAggregationState().setCurrentContent(mockContent);
    }

    private MockHttpServletRequest createMockRequest(MockContent mockContent, MockHttpSession httpSession) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(LOCAL_CONTENT_OBJ, mockContent);
        request.setSession(httpSession);
        request.setContextPath(CONTEXT_PATH);
        return request;
    }

    private MockContent createMockContent(String nodeDataName, String nodeDataValue) {
        MockContent mockContent = new MockContent("test", ItemType.CONTENT);
        MockNodeData mockNodeData = new MockNodeData(nodeDataName, nodeDataValue);
        mockContent.addNodeData(mockNodeData);
        return mockContent;
    }
}