package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubMainContent;
import static com.aperto.magkit.mockito.ContentMockUtils.mockContent;
import static com.aperto.magkit.mockito.ContentStubbingOperation.*;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockAggregationState;
import static com.aperto.magkit.mockito.I18nContentSupportMockUtils.mockI18nContentSupport;
import static com.aperto.magkit.mockito.I18nContentSupportStubbingOperation.stubbLocale;
import info.magnolia.cms.core.Content;
import org.apache.commons.lang.StringUtils;
import static org.easymock.classextension.EasyMock.*;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.text.StringContains.containsString;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.support.RequestContext;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.util.Locale;

/**
 * Test the breadcrumb.
 *
 * @author frank.sommer (17.04.2008)
 */
public class MetaTagsTagTest extends MagKitTagTest {
    private MetaTagsTag _metaTagsTag;
    private PageContext _pageContext;

    @Test
    public void testDefaultMetaTags() throws JspException {
        _metaTagsTag.doEndTag();
        JspWriter jspWriter = _pageContext.getOut();
        String output = jspWriter.toString();
        Assert.assertThat(output, containsString("<meta name=\"author\" content=\"aperto\" />"));
        Assert.assertThat(output, containsString("<meta name=\"description\" content=\"toll\" />"));
        Assert.assertThat(output, containsString("<meta name=\"publisher\" content=\"aperto\" />"));
        Assert.assertThat(StringUtils.countMatches(output, "<meta"), CoreMatchers.is(4));
    }

    protected void mockMgnlContext() {
        Content mainContent = mockContent("page2", stubTitle("layer 3"));
        mockContent("parent",
            stubNodeData("meta-author", "aperto"),
            stubChild("page1",
                stubNodeData("meta-description", "toll"),
                stubChildren(mainContent)
            )
        );
        mockI18nContentSupport(stubbLocale(Locale.GERMAN));
        mockAggregationState(
            stubMainContent(mainContent)
        );
    }

    @Before
    public void setUp() throws Exception {
        cleanContext();
        mockMgnlContext();
        _metaTagsTag = new MetaTagsTag() {
            @Override
            protected RequestContext getContext() {
                RequestContext mockContext = createMock(RequestContext.class);
                expect(mockContext.getMessage("meta.publisher")).andReturn("aperto").times(1);
                expect(mockContext.getMessage((String) anyObject())).andThrow(new NoSuchMessageException("code"));
                replay(mockContext);
                return mockContext;
            }
        };
        _pageContext = createPageContext();
        _metaTagsTag.setPageContext(_pageContext);
    }
}
