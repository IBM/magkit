package com.aperto.magkit;

import com.mockrunner.mock.web.MockPageContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import junit.framework.TestCase;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockHttpServletResponse;
import info.magnolia.context.WebContext;
import info.magnolia.context.WebContextImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.SecuritySupport;

/**
 * This base class can be used to test custom {@link TagSupport} classes. This class does
 * derive from just {@link TestCase} and will not use the spring application context. However,
 * it makes heavy use of classes in the Spring-Mock package.
 *
 * @author reik.schatz
 *
 */
public abstract class MagKitTagTest extends TestCase {
    /**
     * Returns an empty {@link MockPageContext}.
     */
    protected PageContext createPageContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession httpSession = new MockHttpSession();
        request.setSession(httpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();

        return new MockPageContext(new MockServletConfig(), request, response);
    }

    /**
     * Runs the classic tag life cycle using a new instance of {@link MockPageContext}. This {@link PageContext}
     * will be returned afterwards.
     */
    protected PageContext runLifeCycle(TagSupport tag) {
        PageContext pageContext = createPageContext();
        runLifeCycle(tag, pageContext);
        return pageContext;
    }

    /**
     * Runs the classic tag life cycle using a new instance of {@link MockPageContext}. This {@link PageContext}
     * will be returned afterwards.
     */
    protected PageContext runLifeCycle(TagSupport tag, PageContext pageContext) {
        tag.setPageContext(pageContext);
        try {
            tag.doStartTag();
        } catch (JspException e) {
            fail("Failed to call doStartTag() in " + tag.getClass().getSimpleName() + ". " + e.getMessage());
        }

        try {
            int afterBody = tag.doAfterBody();
            while (afterBody == TagSupport.EVAL_BODY_AGAIN) {
                afterBody = tag.doAfterBody();
            }
        } catch (JspException e) {
            fail("Failed to call doAfterBody() in " + tag.getClass().getSimpleName() + ". " + e.getMessage());    
        }

        try {
            tag.doEndTag();
        } catch (Exception e) {
            fail("Failed to call doEndTag() in " + tag.getClass().getSimpleName() + ". " + e.getMessage());
        }
        return pageContext;
    }

    /**
     * Init the magnolia web context.
     * @param request http servlet request
     * @param response http servlet response
     * @param servletContext servlet context
     */
    public static void initMgnlWebContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext){
        WebContext webContext = new WebContextImpl();
        webContext.init(request, response, servletContext);
        MgnlContext.setInstance(webContext);
        initI18nContentSupport();
        initSecuritySupport();
    }

    /**
     * Init magnolia I18N support.
     */
    public static void initI18nContentSupport(){
        DefaultI18nContentSupport i18n = new DefaultI18nContentSupport();
        i18n.setEnabled(false);
        FactoryUtil.setDefaultImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class);
        FactoryUtil.setInstance(DefaultI18nContentSupport.class, i18n);
    }

    /**
     * Init the security support.
     */
    public static void initSecuritySupport(){
        SecuritySupport ssi = new SecuritySupportImpl();
        FactoryUtil.setDefaultImplementation(SecuritySupport.class, SecuritySupportImpl.class);
        FactoryUtil.setInstance(SecuritySupportImpl.class, ssi);
    }
}
