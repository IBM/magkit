package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import com.aperto.magkit.mock.MockContent;
import com.aperto.magkit.mock.MockNodeData;
import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.core.ItemType;
import static info.magnolia.context.MgnlContext.getAggregationState;
import static org.apache.commons.lang.StringUtils.countMatches;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.text.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.springframework.mock.web.*;

import javax.servlet.jsp.*;

/**
 * Test of the create list tag.
 *
 * @author frank.sommer
 * @since 17.01.2008
 */
public class CreateListTagTest extends MagKitTagTest {
    private static final String LIST_VALUE_LONG = "list1|entry1;entry2\nlist2|entry1;entry2";
    private static final String LIST_VALUE_SHORT = "entry1;entry2";

    @Test
    public void testListWithHeadlines() throws JspException {
        CreateListTag tag = new CreateListTag();
        tag.setNodeDataName("list");
        PageContext pageContext = runLifeCycle(tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, startsWith("<h4>"));
        assertThat(countMatches(output, "<li>"), is(4));
    }

    @Test
    public void testSimpleList() throws JspException {
        CreateListTag tag = new CreateListTag();
        tag.setNodeDataName("list2");
        tag.setListTag("ol");
        PageContext pageContext = runLifeCycle(tag);
        JspWriter jspWriter = pageContext.getOut();
        String output = jspWriter.toString();
        assertThat(output, startsWith("<ol>"));
        assertThat(countMatches(output, "<li>"), is(2));
    }

    @Override
    protected PageContext createPageContext() {
        MockContent mockContent = new MockContent("test", ItemType.CONTENT);
        MockNodeData mockNodeData = new MockNodeData("list", LIST_VALUE_LONG);
        mockContent.addNodeData(mockNodeData);
        mockNodeData = new MockNodeData("list2", LIST_VALUE_SHORT);
        mockContent.addNodeData(mockNodeData);
        MockHttpServletRequest request = new MockHttpServletRequest();       
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();
        // init MgnlContext:
        initMgnlWebContext(request, response, httpSession.getServletContext());
        getAggregationState().setCurrentContent(mockContent);
        return new MockPageContext(new MockServletConfig(), request, response);
    }
}