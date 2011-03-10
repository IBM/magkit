package com.aperto.magkit.filter;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.aperto.magkit.mockito.servlet.HttpServletRequestStubbingOperation.stubContextPath;
import static com.aperto.magkit.mockito.servlet.HttpServletResponseStubbingOperation.stubContentType;
import static com.aperto.magkit.mockito.servlet.ServletMockUtils.mockHttpServletRequest;
import static com.aperto.magkit.mockito.servlet.ServletMockUtils.mockHttpServletResponse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link XmlResponseValidatingServletFilter}.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class XmlResponseValidatingServletFilterTest {

    private static final String VALID_UNIS_XML = "<unis xmlns=\"http://www.studierenimosten.de/extranet/vz/1.2/\">" +
        "  <uni_ref>" +
        "    <uni_ref_uri>http://foo.com</uni_ref_uri>" +
        "    <uni_ref_id>123</uni_ref_id>" +
        "  </uni_ref>" +
        "  <recommendationsWeek>" +
        "    <recommendationWeek>" +
        "      <subjectId>foo</subjectId>" +
        "      <uni>foo</uni>" +
        "      <course>foo</course>" +
        "    </recommendationWeek>" +
        "  </recommendationsWeek>" +
        "</unis>";
    private static final String INVALID_XML = "<foo>bar</foo>";

    private XmlResponseValidatingServletFilter _filter;
    private HttpServletResponse _response;
    private HttpServletRequest _request;
    private XmlResponseValidatingServletFilter.ResponseWrapper _responseWrapper;

    @Before
    public void setUp() {
        _filter = new XmlResponseValidatingServletFilter();
        _filter.setSchemaPath("/test.xsd");
        _request = mockHttpServletRequest(stubContextPath("/"));
        _response = mockHttpServletResponse(stubContentType("text/xml"));
        _responseWrapper = new XmlResponseValidatingServletFilter.ResponseWrapper(_response);
    }

    @Test
    public void validateInvalidXml() throws IOException, ServletException {
        _responseWrapper.getWriter().write(INVALID_XML);
        _filter.initFilterBean();
        assertFalse(_filter.validate(_responseWrapper) == null);
    }

    @Test
    public void validateValidUnisXml() throws IOException, ServletException {
        _responseWrapper.getWriter().write(VALID_UNIS_XML);
        _filter.initFilterBean();
        assertTrue(_filter.validate(_responseWrapper) == null);
    }

    @Test
    public void filterInvalidXml() throws IOException, ServletException {
        _filter.setAppendValidationInfo(false);
        initAndStartFilter(_filter, new TestFilterChain(INVALID_XML));
        verify(_response.getWriter()).write(INVALID_XML);
    }

    @Test(expected = RuntimeException.class)
    public void failOnFilterInvalidXml() throws IOException, ServletException {
        _filter.setFailOnInvalidXml(true);
        initAndStartFilter(_filter, new TestFilterChain(INVALID_XML));
    }

    @Test
    public void filterValidXml() throws IOException, ServletException {
        _filter.setAppendValidationInfo(false);
        initAndStartFilter(_filter, new TestFilterChain(VALID_UNIS_XML));
        verify(_response.getWriter()).write(VALID_UNIS_XML);
    }

    @Test
    public void filterInvalidXmlWithValidationInfo() throws IOException, ServletException {
        initAndStartFilter(_filter, new TestFilterChain(INVALID_XML));
        verify(_response.getWriter()).write(matches(INVALID_XML));
    }

    @Test
    public void filterValidXmlWithValidationInfo() throws IOException, ServletException {
        initAndStartFilter(_filter, new TestFilterChain(VALID_UNIS_XML));
        verify(_response.getWriter()).write(matches(VALID_UNIS_XML));
    }

    @Test(expected = RuntimeException.class)
    public void failOnMissingContentType() throws IOException, ServletException {
        when(_response.getContentType()).thenReturn(null);
        _filter.setFailOnInvalidXml(true);
        initAndStartFilter(_filter, new TestFilterChain(VALID_UNIS_XML));
    }

    @Test(expected = RuntimeException.class)
    public void failOnInvalidContentType() throws IOException, ServletException {
        when(_response.getContentType()).thenReturn("text/html");
        _filter.setFailOnInvalidXml(true);
        initAndStartFilter(_filter, new TestFilterChain(VALID_UNIS_XML));
    }

    @Test
    public void doNotFailOnInvalidContentType() throws IOException, ServletException {
        when(_response.getContentType()).thenReturn("text/html");
        initAndStartFilter(_filter, new TestFilterChain(VALID_UNIS_XML));
    }

    private void initAndStartFilter(XmlResponseValidatingServletFilter filter, FilterChain filterChain) throws IOException, ServletException {
        filter.initFilterBean();
        filter.doFilterInternal(_request, _response, filterChain);
    }

    /**
     * {@link javax.servlet.FilterChain} implementation that writes the string given the constructor to the response.
     */
    protected static class TestFilterChain implements FilterChain {

        private String _content;

        public TestFilterChain(final String content) {
            _content = content;
        }

        public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse) throws IOException, ServletException {
            servletResponse.getWriter().write(_content);
        }
    }
}