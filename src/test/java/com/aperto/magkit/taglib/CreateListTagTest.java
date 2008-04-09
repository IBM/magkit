package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockNodeData;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;
import org.apache.commons.lang.StringUtils;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Test of the create list tag.
 *
 * @author frank.sommer (17.01.2008)
 */
public class CreateListTagTest extends MagKitTagTest {
    private static final String LOCAL_CONTENT_OBJ = "contentObj";
    private static final String LIST_VALUE_LONG = "list1|entry1;entry2\nlist2|entry1;entry2";
    private static final String LIST_VALUE_SHORT = "entry1;entry2";

    public void testListWithHeadlines() throws JspException {
        CreateListTag tag = new CreateListTag();
        tag.setNodeDataName("list");
        PageContext pageContext = runLifeCycle(tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertTrue(output.startsWith("<h4>"));
        assertEquals("Four list entries are expected.", 4, StringUtils.countMatches(output, "<li>"));
    }

    public void testSimpleList() throws JspException {
        CreateListTag tag = new CreateListTag();
        tag.setNodeDataName("list2");
        tag.setListTag("ol");
        PageContext pageContext = runLifeCycle(tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertTrue(output.startsWith("<ol>"));
        assertEquals("Two list entries are expected.", 2, StringUtils.countMatches(output, "<li>"));
    }

    @Override
    protected PageContext createPageContext() {
        MockContent mockContent = new MockContent("test", ItemType.CONTENT);
        MockNodeData mockNodeData = new MockNodeData("list", LIST_VALUE_LONG);
        mockContent.addNodeData(mockNodeData);
        mockNodeData = new MockNodeData("list2", LIST_VALUE_SHORT);
        mockContent.addNodeData(mockNodeData);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(LOCAL_CONTENT_OBJ, mockContent);
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();
        // init MgnlContext:
        initMgnlWebContext(request, response, httpSession.getServletContext());

        return new MockPageContext(new MockServletConfig(), request, response);
    }
}
