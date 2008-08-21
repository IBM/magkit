package com.aperto.magkit.i18n;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.i18n.AbstractLocaleResolver;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Implementiation of the @see{AbstractLocaleResolver}.
 *
 * @author frank.sommer (21.08.2008)
 */
public class MgnlLocaleResolver extends AbstractLocaleResolver {
    private static final Logger LOGGER = Logger.getLogger(MgnlLocaleResolver.class);

    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        I18nContentSupport i18nSupport = I18nContentSupportFactory.getI18nSupport();
        Locale locale = super.getDefaultLocale();
        if (i18nSupport != null) {
            locale = i18nSupport.getLocale();
        }
        LOGGER.debug("Locale resolved to: " + locale.toString());
        return locale;
    }

    public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {
    }
}
