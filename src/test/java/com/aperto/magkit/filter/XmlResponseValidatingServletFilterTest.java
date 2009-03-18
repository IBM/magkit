package com.aperto.magkit.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.mockrunner.mock.web.MockHttpServletRequest;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

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
    private MockHttpServletResponse _response = new MockHttpServletResponse();
    private XmlResponseValidatingServletFilter.ResponseWrapper _responseWrapper;

    @Before
    public void setUp() {
        _filter = new XmlResponseValidatingServletFilter();
        _filter.setSchemaPath("/test.xsd");
        _response = new MockHttpServletResponse();
        _responseWrapper = new XmlResponseValidatingServletFilter.ResponseWrapper(_response);
    }

    @Test
    public void validateInvalidXml() throws IOException {
        _responseWrapper.getWriter().write(INVALID_XML);
        assertFalse(_filter.validate(_responseWrapper) == null);
    }

    @Test
    public void validateValidUnisXml() throws IOException {
        _responseWrapper.getWriter().write(VALID_UNIS_XML);
        assertTrue(_filter.validate(_responseWrapper) == null);
    }

    @Test
    public void filterInvalidXml() throws IOException, ServletException {
        _filter.setAppendValidationInfo(false);
        _filter.doFilterInternal(new MockHttpServletRequest(), _response, new TestFilterChain(INVALID_XML));
        assertThat(_response.getContentAsString(), equalTo(INVALID_XML));
    }

    @Test(expected = RuntimeException.class)
    public void failOnFilterInvalidXml() throws IOException, ServletException {
        _filter.setFailOnInvalidXml(true);
        _filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), new TestFilterChain(INVALID_XML));
    }

    @Test
    public void filterValidXml() throws IOException, ServletException {
        _filter.setAppendValidationInfo(false);
        _filter.doFilterInternal(new MockHttpServletRequest(), _response, new TestFilterChain(VALID_UNIS_XML));
        assertThat(_response.getContentAsString(), equalTo(VALID_UNIS_XML));
    }

    @Test
    public void filterInvalidXmlWithValidationInfo() throws IOException, ServletException {
        _filter.doFilterInternal(new MockHttpServletRequest(), _response, new TestFilterChain(INVALID_XML));
        assertTrue(_response.getContentAsString().startsWith(INVALID_XML));
        assertTrue(_response.getContentAsString().length() > INVALID_XML.length());
        assertTrue(_response.getContentAsString().substring(INVALID_XML.length()).contains("valid:false"));
    }

    @Test
    public void filterValidXmlWithValidationInfo() throws IOException, ServletException {
        _filter.doFilterInternal(new MockHttpServletRequest(), _response, new TestFilterChain(VALID_UNIS_XML));
        assertTrue(_response.getContentAsString().startsWith(VALID_UNIS_XML));
        assertTrue(_response.getContentAsString().length() > VALID_UNIS_XML.length());
        assertTrue(_response.getContentAsString().substring(VALID_UNIS_XML.length()).contains("valid:true"));
    }

    /**
     * {@link javax.servlet.FilterChain} implementation that writes the string given the contructor to the response.
     */
    private static class TestFilterChain implements FilterChain {

        private String _content;

        public TestFilterChain(final String content) {
            _content = content;
        }

        public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse) throws IOException, ServletException {
            servletResponse.getWriter().write(_content);
        }
    }
}