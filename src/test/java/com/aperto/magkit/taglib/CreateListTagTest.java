package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubCurrentContent;
import static com.aperto.magkit.mockito.ContentMockUtils.mockContent;
import static com.aperto.magkit.mockito.ContentStubbingOperation.stubNodeData;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockAggregationState;
import static org.apache.commons.lang.StringUtils.countMatches;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.text.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Test of the create list tag.
 *
 * @author frank.sommer
 * @since 17.01.2008
 */
public class CreateListTagTest extends MagKitTagTest {
    private static final String LIST_VALUE_LONG = "list1|entry1;entry2\nlist2|entry1;entry2";
    private static final String LIST_VALUE_SHORT = "entry1;entry2";

    @Before
    public void setUp() {
        cleanContext();
        mockAggregationState(
            stubCurrentContent(
                mockContent("test",
                    stubNodeData("list", LIST_VALUE_LONG),
                    stubNodeData("list2", LIST_VALUE_SHORT)
                )
            )
        );
    }

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
}