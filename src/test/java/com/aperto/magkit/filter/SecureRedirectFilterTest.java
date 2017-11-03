package com.aperto.magkit.filter;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.WebContext;
import info.magnolia.link.CompleteUrlPathTransformer;
import info.magnolia.link.LinkTransformerManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubExtension;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockAggregationState;
import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for secure redirect filter.
 *
 * @author frank.sommer
 * @since 17.08.2012
 */
public class SecureRedirectFilterTest {
    private SecureRedirectFilter _redirectFilter;
    private HttpServletRequest _request;
    private HttpServletResponse _response;
    private FilterChain _chain;

    @Test
    public void testDocrootHttpRequest() throws IOException, ServletException, RepositoryException {
        initContext(null, "js", null);
        _redirectFilter.doFilter(_request, _response, _chain);
        verify(_chain, times(1)).doFilter(_request, _response);
    }

    @Test
    public void testStandardHttpRequest() throws IOException, ServletException, RepositoryException {
        initContext("magkit:pages/folder", null, null);
        _redirectFilter.doFilter(_request, _response, _chain);
        verify(_chain, times(1)).doFilter(_request, _response);
    }

    @Test
    public void testDocrootHttpsRequest() throws IOException, ServletException, RepositoryException {
        initContext(null, "css", null);
        when(_request.isSecure()).thenReturn(true);
        _redirectFilter.doFilter(_request, _response, _chain);
        verify(_chain, times(1)).doFilter(_request, _response);
    }

    @Test
    public void testStandardHttpsRequest() throws IOException, ServletException, RepositoryException {
        initContext("magkit:pages/folder", null, null);
        when(_request.isSecure()).thenReturn(true);
        _redirectFilter.doFilter(_request, _response, _chain);
        verify(_chain, never()).doFilter(_request, _response);
        verify(_response).sendRedirect("http://www.aperto.de/folder.html");
    }

    @Ignore
    @Test
    public void testSecureHttpRequest() throws IOException, ServletException, RepositoryException {
        initContext("standard-templating-kit:pages/stkForm", null, null);
        when(_request.isSecure()).thenReturn(false);
        _redirectFilter.doFilter(_request, _response, _chain);
        verify(_chain, never()).doFilter(_request, _response);
        verify(_response).sendRedirect("https://www.aperto.de/stkForm.html");
    }

    @Ignore
    @Test
    public void testSecureHttpRequestWithPorts() throws IOException, ServletException, RepositoryException {
        initContext("standard-templating-kit:pages/stkForm", null, ":80");
        when(_request.isSecure()).thenReturn(false);
        _redirectFilter.setHttpPort("80");
        _redirectFilter.setHttpsPort("443");
        _redirectFilter.doFilter(_request, _response, _chain);
        verify(_chain, never()).doFilter(_request, _response);
        verify(_response).sendRedirect("https://www.aperto.de:443/stkForm.html");
    }

    private void initContext(String template, String extension, String portSuffix) throws RepositoryException {
        if (isNotBlank(template)) {
            String nodeName = substringAfterLast(template, "/");
            Node actPage = mock(Node.class);
            when(actPage.getName()).thenReturn(nodeName);
            Session session = mock(Session.class);
            Property property = mock(Property.class);
            when(property.getString()).thenReturn(template);
            when(actPage.getProperty(anyString())).thenReturn(property);
            when(session.getNode(anyString())).thenReturn(actPage);
            WebContext webContext = mockWebContext();
            when(webContext.getJCRSession(anyString())).thenReturn(session);
            Workspace workspace = mock(Workspace.class);
            when(workspace.getName()).thenReturn(WEBSITE);
            when(actPage.getSession()).thenReturn(session);
            when(session.getWorkspace()).thenReturn(workspace);
            AggregationState aggregationState = mockAggregationState(stubExtension(isBlank(extension) ? "html" : extension));
            when(aggregationState.getMainContentNode()).thenReturn(actPage);

            LinkTransformerManager linkManager = mock(LinkTransformerManager.class);
            CompleteUrlPathTransformer transformer = mock(CompleteUrlPathTransformer.class);
            when(transformer.transform(Matchers.any())).thenReturn("http://www.aperto.de" + defaultString(portSuffix) + "/" + nodeName + ".html");
            when(linkManager.getCompleteUrl()).thenReturn(transformer);
            _redirectFilter.setLinkTransformer(linkManager);
        } else {
            mockAggregationState();
        }
        if (isBlank(extension)) {
            when(_response.getContentType()).thenReturn("text/html");
        } else {
            when(_response.getContentType()).thenReturn("text/" + extension);
        }
    }

    @Before
    public void initServletContainer() {
        cleanContext();
        _redirectFilter = new SecureRedirectFilter();
        TemplateNameVoter voter = new TemplateNameVoter();
        voter.addTemplate("standard-templating-kit:pages/stkForm");
        _redirectFilter.addSecure(voter);
        _request = mock(HttpServletRequest.class);
        _response = mock(HttpServletResponse.class);
        when(_request.getMethod()).thenReturn("GET");
        _chain = mock(FilterChain.class);
    }
}