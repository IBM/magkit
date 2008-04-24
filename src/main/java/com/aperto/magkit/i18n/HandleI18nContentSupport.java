package com.aperto.magkit.i18n;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.context.MgnlContext;
import java.util.Locale;

/**
 * Extends the default content support by retrieving the locale from handle not only by a subnode.
 * The actual node is also neccessary.
 *
 * @author frank.sommer (24.04.2008)
 */
public class HandleI18nContentSupport extends DefaultI18nContentSupport {
    private static final Logger LOGGER = Logger.getLogger(HandleI18nContentSupport.class);

    /**
     * Determines the locale from the current uri.
     * e.g. /content/de.html
     * @return locale from the uri.
     */
    @Override
    public Locale determineLocale() {
        final String i18nUri = MgnlContext.getAggregationState().getCurrentURI();
        Locale locale = getFallbackLocale();

        String[] handleParts = StringUtils.split(i18nUri, '/');
        for (String part : handleParts) {
            String[] localeArr = StringUtils.split(part, "_");
            if (localeArr.length == 1) {
                locale = new Locale(StringUtils.substringBefore(localeArr[0], "."));
            } else if (localeArr.length == 2) {
                locale = new Locale(localeArr[0], StringUtils.substringBefore(localeArr[1], "."));
            }

            if (isLocaleSupported(locale)) {
                LOGGER.debug("Supported locale found: " + locale.getLanguage());
                break;
            }
        }

        if (!isLocaleSupported(locale)) {
            locale = getFallbackLocale();
        }
        return locale;
    }
}
