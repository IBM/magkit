package com.aperto.magkit.utils;

import org.junit.Test;
import org.junit.Before;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import com.aperto.magkit.MagKitTest;
import com.mockrunner.mock.web.MockPageContext;
import javax.servlet.jsp.PageContext;
import info.magnolia.context.MgnlContext;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test of the resource utils.
 * @see ResourceUtils
 *
 * @author frank.sommer (11.12.2008)
 */
public class ResourceUtilsTest extends MagKitTest {
    @Test
    public void retrieveValueOfSelector() {
        MgnlContext.getAggregationState().setSelector("aid-6.pid-2.kid-test");
        String value = ResourceUtils.retrieveValueOfSelector("pid");
        assertThat(value, is("2"));
        value = ResourceUtils.retrieveValueOfSelector("kid");
        assertThat(value, is("test"));
        value = ResourceUtils.retrieveValueOfSelector("sid");
        assertThat(value, is(""));
    }

    @Before
    public void createMgnlContext() {
        createPageContext();
    }

    @Override
    protected PageContext createPageContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();
        initMgnlWebContext(request, response, httpSession.getServletContext());
        MgnlContext.getAggregationState().setCharacterEncoding("utf-8");

        return new MockPageContext(new MockServletConfig(), request, response);
    }
}
