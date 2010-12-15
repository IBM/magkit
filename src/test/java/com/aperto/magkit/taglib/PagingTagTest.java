package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.*;
import static com.aperto.magkit.mockito.ContentMockUtils.mockContent;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockAggregationState;
import static com.aperto.magkit.mockito.I18nContentSupportMockUtils.mockI18nContentSupport;
import static com.aperto.magkit.mockito.I18nContentSupportStubbingOperation.stubbLocale;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.text.StringContains.containsString;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.util.Locale;

/**
 * Test of the paging tag.
 *
 * @author frank.sommer (16.11.2007)
 */
public class PagingTagTest extends MagKitTagTest {

    @Before
    public void setUp() {
        cleanContext();
        mockAggregationState(
            stubMainContent(mockContent("page")),
            stubCharacterEncoding("UTF-8"),
            stubSelector("old.selector.pid-1")
        );
        mockI18nContentSupport(stubbLocale(Locale.GERMAN));
    }

    @Test
    public void testTag() throws JspException {
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
    public void testPaddingStart() throws JspException {
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
    public void testWithoutCapsulate() throws JspException {
        PagingTag tag = new PagingTag();
        tag.setPages(5);
        tag.setActPage(1);
        tag.setEncapsulate("false");
        tag.setActiveClass("active");
        PageContext pageContext = runLifeCycle(tag);
        assertThat(pageContext, notNullValue());
        JspWriter jspWriter = pageContext.getOut();
        assertThat(jspWriter, notNullValue());
        String output = jspWriter.toString();
        assertThat(output, notNullValue());
        assertThat(output, not(containsString("<div>")));
        assertThat(output, containsString("<li class=\"active\"><em>Sie sind hier : </em><strong>1</strong></li>"));
        assertThat(output, containsString("2</a>"));
        assertThat(output, not(containsString(".pid-1.")));
    }

    @Test
    public void testPaddingEnd() throws JspException {
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
    public void testPaddingLast() throws JspException {
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
    public void testPaddingEndLong() throws JspException {
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