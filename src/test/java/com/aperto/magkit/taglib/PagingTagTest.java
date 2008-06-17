package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContent;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.junit.matchers.StringContains.containsString;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Test of the paging tag.
 *
 * @author frank.sommer (16.11.2007)
 */
public class PagingTagTest extends MagKitTagTest {
    @Override
    protected PageContext createPageContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockContent mockContent = new MockContent("page", ItemType.CONTENT);
        initMgnlWebContext(request, response, httpSession.getServletContext());
        MgnlContext.getAggregationState().setMainContent(mockContent);
        MgnlContext.getAggregationState().setSelector("old.selector.pid-1");
        return new MockPageContext(new MockServletConfig(), request, response);
    }

    @Test
    public void testTag() {
        PagingTag tag = new PagingTag();
        tag.setPages(2);
        tag.setActPage(1);
        PageContext pageContext = runLifeCycle(tag);
        assertThat(pageContext, notNullValue());
        JspWriter jspWriter = pageContext.getOut();
        assertThat(jspWriter, notNullValue());
        String output = jspWriter.toString();
        assertThat(output, notNullValue());
        assertThat(output, containsString("<li><em>Sie sind hier : </em><strong>1</strong></li>"));
        assertThat(output, containsString("2</a>"));
        assertThat(output, not(containsString(".pid-1.")));
        assertThat(output, containsString("old.selector.pid-2."));
    }

    @Test
    public void testPaddingStart() {
        PagingTag tag = new PagingTag();
        tag.setPages(10);
        tag.setActPage(1);
        PageContext pageContext = runLifeCycle(tag);
        assertThat(pageContext, notNullValue());
        JspWriter jspWriter = pageContext.getOut();
        assertThat(jspWriter, notNullValue());
        String output = jspWriter.toString();
        assertThat(output, notNullValue());
        assertThat(output, containsString("<li><em>Sie sind hier : </em><strong>1</strong></li>"));
        assertThat(output, containsString("2</a>"));
        assertThat(output, not(containsString(".pid-1.")));
        assertThat(output, containsString("5</a></li><li>...</li>"));
    }

    @Test
    public void testPaddingEnd() {
        PagingTag tag = new PagingTag();
        tag.setPages(10);
        tag.setActPage(5);
        PageContext pageContext = runLifeCycle(tag);
        assertThat(pageContext, notNullValue());
        JspWriter jspWriter = pageContext.getOut();
        assertThat(jspWriter, notNullValue());
        String output = jspWriter.toString();
        assertThat(output, notNullValue());
        assertThat(output, containsString("1</a></li>"));
        assertThat(output, not(containsString("2</a></li>")));
        assertThat(output, containsString("<li><em>Sie sind hier : </em><strong>5</strong></li>"));
        assertThat(output, containsString("<li>...</li><li><a href=\"/page.old.selector.pid-3.html\" title=\"zur Seite 3\" >3</a></li>"));
        assertThat(output, containsString("7</a></li><li>...</li>"));
    }

    @Test
    public void testPaddingLast() {
        PagingTag tag = new PagingTag();
        tag.setPages(8);
        tag.setActPage(8);
        PageContext pageContext = runLifeCycle(tag);
        assertThat(pageContext, notNullValue());
        JspWriter jspWriter = pageContext.getOut();
        assertThat(jspWriter, notNullValue());
        String output = jspWriter.toString();
        assertThat(output, notNullValue());
        assertThat(output, containsString("1</a></li>"));
        assertThat(output, not(containsString("2</a></li>")));
        assertThat(output, containsString("<li><em>Sie sind hier : </em><strong>8</strong></li>"));
        assertThat(output, containsString("<li>...</li><li><a href=\"/page.old.selector.pid-4.html\" title=\"zur Seite 4\" >4</a></li>"));
        assertThat(output, containsString("1</a></li><li>...</li>"));
    }

    @Test
    public void testPaddingEndLong() {
        PagingTag tag = new PagingTag();
        tag.setPages(20);
        tag.setActPage(5);
        tag.setLinkedPages(10);
        PageContext pageContext = runLifeCycle(tag);
        assertThat(pageContext, notNullValue());
        JspWriter jspWriter = pageContext.getOut();
        assertThat(jspWriter, notNullValue());
        String output = jspWriter.toString();
        assertThat(output, notNullValue());
        assertThat(output, containsString(">1</a></li>"));
        assertThat(output, containsString(">2</a></li>"));
        assertThat(output, containsString("<li><em>Sie sind hier : </em><strong>5</strong></li>"));
        assertThat(output, containsString("10</a></li><li>...</li>"));
    }
}

