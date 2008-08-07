package com.aperto.magkit.mail;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import com.aperto.magkit.MagKitTest;

/**
 * TODO: override the retrieveNodeDataStream method for testing.
 * @author frank.sommer (07.08.2008)
 */
public class MagnoliaContentVelocityMailMessageTemplateTest extends MagKitTest {
    private static final String TEXT = "Best regards,\n${someone}";

    @Test
    public void testTemplate() throws Exception {
        /*MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();
        initMgnlWebContext(request, response, httpSession.getServletContext());
        MagnoliaContentVelocityMailMessageTemplate template = new MagnoliaContentVelocityMailMessageTemplate();
        template.setTemplateName("/content/template");
        SimpleMailMessage message = template.evaluate(null);
        assertEquals("Hello ${who}!", message.getSubject());
        assertEquals(TEXT, message.getText());*/
    }
}
