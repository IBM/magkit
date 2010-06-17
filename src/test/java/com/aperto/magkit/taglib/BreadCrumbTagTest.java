package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;

import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockNodeData;
import static org.apache.commons.lang.StringUtils.countMatches;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
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

/**
 * Test the breadcrumb tag.
 * @author frank.sommer (17.04.2008) 
 */
public class BreadCrumbTagTest extends MagKitTagTest {

    private BreadCrumbTag _tag;

    @Test
    public void testDefaultBreadCrumb() throws JspException {
        PageContext pageContext = runLifeCycle(_tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString("<ol>"));
        assertThat(countMatches(output, "<li>"), is(2));
    }

    @Test
    public void testWithDelimiter() throws JspException {
        _tag.setListStyle(false);
        _tag.setListType(" - ");
        PageContext pageContext = runLifeCycle(_tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, not(containsString("<ol>")));
        assertThat(countMatches(output, " - "), is(2));
    }

    @Test
    public void testListWithDelimiter() throws JspException {
        _tag.setSeparator("/");
        PageContext pageContext = runLifeCycle(_tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, containsString("<ol>"));
        assertThat(countMatches(output, "/</a>"), is(2));
    }

    @Test
    public void testOtherListType() throws JspException {
        _tag.setListType("ul class=\"test\"");
        PageContext pageContext = runLifeCycle(_tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, not(containsString("<ol>")));
        assertThat(output, containsString("<ul class=\"test\">"));
        assertThat(countMatches(output, "<li>"), is(2));
    }

    @Override
    protected PageContext createPageContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockContent rootContent = new MockContent("content", ItemType.CONTENT);

        MockContent parentContent = new MockContent("parent", ItemType.CONTENT);
        parentContent.addNodeData(new MockNodeData("title", "layer 1"));
        parentContent.setParent(rootContent);

        MockContent mockContent = new MockContent("page1", ItemType.CONTENT);
        mockContent.addNodeData(new MockNodeData("title", "layer 2"));
        mockContent.setParent(parentContent);

        MockContent mockContent2 = new MockContent("page2", ItemType.CONTENT);
        mockContent2.addNodeData(new MockNodeData("title", "layer 3"));
        mockContent2.setParent(mockContent);

        initMgnlWebContext(request, response, httpSession.getServletContext());
        MgnlContext.getAggregationState().setMainContent(mockContent2);
        return new MockPageContext(new MockServletConfig(), request, response);
    }

    @Before
    public void initTag() throws Exception {
        _tag = new BreadCrumbTag();
    }
}
