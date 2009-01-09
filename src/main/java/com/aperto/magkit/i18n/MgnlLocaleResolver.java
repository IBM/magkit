package com.aperto.magkit.i18n;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.context.MgnlContext;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

/**
 * Implementiation of the {@link AbstractLocaleResolver} that determines the current locale used for rendering content
 * by magnolia.
 *
 * @author Frank Sommer, Aperto AG (2008-08-21)
 */
public class MgnlLocaleResolver extends AbstractLocaleResolver {
    private static final Logger LOGGER = Logger.getLogger(MgnlLocaleResolver.class);

    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        Locale locale;
        I18nContentSupport i18nSupport = I18nContentSupportFactory.getI18nSupport();
        // To get a locale from i18n support implementation a magnolia context is required. Otherwise the getLocale()
        // method will fail with an exception.
        if (i18nSupport != null && MgnlContext.hasInstance()) {
            locale = i18nSupport.getLocale();
        } else {
            locale = super.getDefaultLocale();
        }
        LOGGER.debug("Locale resolved to: " + locale.toString());
        return locale;
    }

    public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {
    }
}