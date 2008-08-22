package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockNodeData;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.StringEndsWith.endsWith;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.junit.matchers.StringContains.containsString;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

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

    @Test
    public void testExternalLink() throws JspException {
        ConvertLinkTag tag = new ConvertLinkTag();
        tag.setNodeDataName("link");
        PageContext pageContext = runLifeCycle(tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, not(containsString(CONTEXT_PATH)));
        assertThat(output, is(LINK_VALUE_EXT));
    }

    @Test
    public void testInternalLink() throws JspException {
        ConvertLinkTag tag = new ConvertLinkTag();
        tag.setNodeDataName("link2");
        PageContext pageContext = runLifeCycle(tag);
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
        PageContext pageContext = runLifeCycle(tag);
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
        PageContext pageContext = runLifeCycle(tag);
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
        PageContext pageContext = runLifeCycle(tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString(CONTEXT_PATH));
        assertThat(output, containsString(LINK_VALUE_INT_SHORT));
        assertThat(output, endsWith("123.html"));
    }

    @Override
    protected PageContext createPageContext() {
        MockContent mockContent = new MockContent("test", ItemType.CONTENT);
        MockNodeData mockNodeData = new MockNodeData("link", LINK_VALUE_EXT);
        mockContent.addNodeData(mockNodeData);
        mockNodeData = new MockNodeData("link2", LINK_VALUE_INT);
        mockContent.addNodeData(mockNodeData);
        mockNodeData = new MockNodeData("link3", LINK_VALUE_INT_SHORT);
        mockContent.addNodeData(mockNodeData);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(LOCAL_CONTENT_OBJ, mockContent);
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        request.setContextPath(CONTEXT_PATH);
        MockHttpServletResponse response = new MockHttpServletResponse();
        // init MgnlContext:
        initMgnlWebContext(request, response, httpSession.getServletContext());
        MgnlContext.getAggregationState().setCurrentContent(mockContent);
        return new MockPageContext(new MockServletConfig(), request, response);
    }
}