package com.aperto.magkit.taglib;

import com.aperto.magkit.MagKitTagTest;
import com.aperto.magkit.mock.MockContent;
import com.aperto.magkit.mock.MockNodeData;

import com.mockrunner.mock.web.MockPageContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringUtils;
import static org.easymock.classextension.EasyMock.anyObject;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.text.StringContains.containsString;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.NoSuchMessageException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.servlet.support.RequestContext;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.util.Locale;

/**
 * Test the breadcrumb.
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

    @Override
    protected PageContext createPageContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockContent parentContent = new MockContent("parent", ItemType.CONTENT);
        parentContent.addNodeData(new MockNodeData("meta-author", "aperto"));

        MockContent mockContent = new MockContent("page1", ItemType.CONTENT);
        mockContent.addNodeData(new MockNodeData("meta-description", "toll"));
        mockContent.setParent(parentContent);

        MockContent mockContent2 = new MockContent("page2", ItemType.CONTENT);
        mockContent2.addNodeData(new MockNodeData("title", "layer 3"));
        mockContent2.setParent(mockContent);

        initMgnlWebContext(request, response, httpSession.getServletContext());
        MgnlContext.setLocale(new Locale("de"));
        MgnlContext.getAggregationState().setMainContent(mockContent2);
        return new MockPageContext(new MockServletConfig(), request, response);
    }

    @Before
    public void initTag() throws Exception {
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
