package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubMainContent;
import static com.aperto.magkit.mockito.ContentMockUtils.mockContent;
import static com.aperto.magkit.mockito.ContentStubbingOperation.*;
import com.aperto.magkit.mockito.ContextMockUtils;
import static com.aperto.magkit.mockito.ContextMockUtils.mockAggregationState;
import static com.aperto.magkit.mockito.ServerConfigurationMockUtils.mockServerConfiguration;
import static com.aperto.magkit.mockito.ServerConfigurationStubbingoperation.stubbDefaultExtension;
import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.core.Content;
import static org.apache.commons.lang.StringUtils.EMPTY;
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
 *
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
        Content main = mockContent("page2", stubTitle("layer 3"), stubNodeData("navTitle", EMPTY));
        mockContent("content",
            stubNodeData("navTitle", EMPTY),
            stubChild("parent",
                stubNodeData("navTitle", EMPTY),
                stubTitle("layer 1"),
                stubChild("page1",
                    stubNodeData("navTitle", EMPTY),
                    stubTitle("layer 2"),
                    stubChildren(main)
                )
            )
        );
        mockAggregationState(stubMainContent(main));
        mockServerConfiguration(stubbDefaultExtension("html"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();
        return new MockPageContext(new MockServletConfig(), request, response);
    }

    @Before
    public void initTag() throws Exception {
        _tag = new BreadCrumbTag();
        ContextMockUtils.cleanContext();
    }
}
