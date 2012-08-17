package com.aperto.magkit.filter;

import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.link.CompleteUrlPathTransformer;
import info.magnolia.link.Link;
import info.magnolia.link.LinkTransformerManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.jcr.*;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubExtension;
import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubMainContent;
import static com.aperto.magkit.mockito.ContentMockUtils.mockContent;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockAggregationState;
import static info.magnolia.cms.core.MetaData.DEFAULT_META_NODE;
import static org.apache.commons.lang.StringUtils.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test for secure redirect filter.
 *
 * @author frank.sommer
 * @since 17.08.2012
 */
public class SecureRedirectFilterTest {
    private SecureRedirectFilter _redirectFilter;
    private MockHttpServletRequest _request;
    private MockHttpServletResponse _response;
    private FilterChain _chain;

    @Test
    public void testDocrootHttpRequest() throws IOException, ServletException, RepositoryException {
        initContext(null, "js", null);
        _redirectFilter.doFilter(_request, _response, _chain);
        verify(_chain, times(1)).doFilter(_request, _response);
    }

    @Test
    public void testStandardHttpRequest() throws IOException, ServletException, RepositoryException {
        initContext("magkit-stk:pages/folder", null, null);
        _redirectFilter.doFilter(_request, _response, _chain);
        verify(_chain, times(1)).doFilter(_request, _response);
    }

    @Test
    public void testDocrootHttpsRequest() throws IOException, ServletException, RepositoryException {
        initContext(null, "css", null);
        _request.setSecure(true);
        _redirectFilter.doFilter(_request, _response, _chain);
        verify(_chain, times(1)).doFilter(_request, _response);
    }

    @Test
    public void testStandardHttpsRequest() throws IOException, ServletException, RepositoryException {
        initContext("magkit-stk:pages/folder", null, null);
        _request.setSecure(true);
        _redirectFilter.doFilter(_request, _response, _chain);
        verify(_chain, never()).doFilter(_request, _response);
        assertThat(_response.getRedirectedUrl(), equalTo("http://www.aperto.de/folder.html"));
    }

    @Test
    public void testSecureHttpRequest() throws IOException, ServletException, RepositoryException {
        initContext("standard-templating-kit:pages/stkForm", null, null);
        _request.setSecure(false);
        _redirectFilter.doFilter(_request, _response, _chain);
        verify(_chain, never()).doFilter(_request, _response);
        assertThat(_response.getRedirectedUrl(), equalTo("https://www.aperto.de/stkForm.html"));
    }

    @Test
    public void testSecureHttpRequestWithPorts() throws IOException, ServletException, RepositoryException {
        initContext("standard-templating-kit:pages/stkForm", null, ":80");
        _request.setSecure(false);
        _redirectFilter.setHttpPort("80");
        _redirectFilter.setHttpsPort("443");
        _redirectFilter.doFilter(_request, _response, _chain);
        verify(_chain, never()).doFilter(_request, _response);
        assertThat(_response.getRedirectedUrl(), equalTo("https://www.aperto.de:443/stkForm.html"));
    }

    private void initContext(String template, String extension, String portSuffix) throws RepositoryException {
        if (isNotBlank(template)) {
            String nodeName = substringAfterLast(template, "/");
            Content actPage = mockContent(nodeName);
            Session session = mock(Session.class);
            Node node = mock(Node.class);
            Node metaDataNode = mock(Node.class);
            Property property = mock(Property.class);
            when(property.getString()).thenReturn(template);
            when(metaDataNode.getProperty(anyString())).thenReturn(property);
            when(node.getNode(DEFAULT_META_NODE)).thenReturn(metaDataNode);
            when(session.getNode(anyString())).thenReturn(node);
            when(MgnlContext.getJCRSession(anyString())).thenReturn(session);
            Workspace workspace = mock(Workspace.class);
            when(workspace.getName()).thenReturn("website");
            when(actPage.getWorkspace()).thenReturn(workspace);
            mockAggregationState(stubExtension(isBlank(extension) ? "html" : extension), stubMainContent(actPage));

            LinkTransformerManager linkManager = mock(LinkTransformerManager.class);
            CompleteUrlPathTransformer transformer = mock(CompleteUrlPathTransformer.class);
            when(transformer.transform(Matchers.<Link>any())).thenReturn("http://www.aperto.de" + defaultString(portSuffix) + "/" + nodeName + ".html");
            when(linkManager.getCompleteUrl()).thenReturn(transformer);
            _redirectFilter.setLinkTransformer(linkManager);
        } else {
            mockAggregationState();
        }
        if (isBlank(extension)) {
            _response.setContentType("text/html");
        } else {
            _response.setContentType("text/" + extension);
        }
    }

    @Before
    public void initServletContainer() {
        cleanContext();
        _redirectFilter = new SecureRedirectFilter();
        TemplateNameVoter voter = new TemplateNameVoter();
        voter.addTemplate("standard-templating-kit:pages/stkForm");
        _redirectFilter.addSecure(voter);
        _request = new MockHttpServletRequest();
        _response = new MockHttpServletResponse();
        _request.setMethod("GET");
        _chain = mock(FilterChain.class);
    }
}