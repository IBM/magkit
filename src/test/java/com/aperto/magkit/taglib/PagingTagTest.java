package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContent;
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

    public void testTag() {
        PagingTag tag = new PagingTag();
        tag.setPages(2);
        tag.setActPage(1);
        PageContext pageContext = runLifeCycle(tag);
        assertNotNull("PageContext is null", pageContext);
        JspWriter jspWriter = pageContext.getOut();
        assertNotNull("JspWriter is null", jspWriter);
        String output = jspWriter.toString();
        assertNotNull("output is null", output);
        assertTrue("Output does not contain expected String '1</strong>'", output.contains("1</strong>"));
        assertTrue("Output does not contain expected String '2</a>'", output.contains("2</a>"));
        assertFalse("Output does not contain expected String '.pid-1.'", output.contains(".pid-1."));
        assertTrue("\"" + output + "\" does not contain expected String 'old.selector.pid-2.'", output.contains("old.selector.pid-2."));
    }
}
