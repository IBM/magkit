package com.aperto.magkit.i18n;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.LocaleDefinition;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Collection;

/**
 * Implements the I18nContent Support to set the locale from handles real node in the repository.
 *
 * @author frank.sommer (24.04.2008)
 */
public class HandleI18nContentSupport implements I18nContentSupport {
    private static final Logger LOGGER = Logger.getLogger(HandleI18nContentSupport.class);

    /**
     * English is the default fallback language.
     */
    private Locale _fallbackLocale = new Locale("en");

    /**
     * The active locales.
     */
    private Map<String, Locale> _locales = new HashMap<String, Locale>();

    /**
     * Is I18N enabled.
     */
    private boolean _enabled;

    public Locale getLocale() {
        return MgnlContext.getAggregationState().getLocale();
    }

    /**
     * Setter for locale.
     */
    public void setLocale(Locale locale) {
        MgnlContext.getAggregationState().setLocale(locale);
    }

    public Locale getFallbackLocale() {
        return _fallbackLocale;
    }

    public void setFallbackLocale(Locale fallbackLocale) {
        _fallbackLocale = fallbackLocale;
    }

    //CHECKSTYLE:OFF
    // method names have to be implements as they are.
    /**
     * Do nothing with the uri.
     */
    public String toI18NURI(String uri) {
        return uri;
    }

    /**
     * Do nothing with the uri.
     */
    public String toRawURI(String i18nUri) {
        return i18nUri;
    }
    //CHECKSTYLE:ON

    /**
     * Determines the locale from the current uri.
     * e.g. /content/de.html --> locale de
     * e.g. /content/de_mandant.html --> locale de
     * e.g. /content/de_DE/test.html --> locale de_DE
     * @return locale from the uri.
     */
    public Locale determineLocale() {
        final String i18nUri = MgnlContext.getAggregationState().getCurrentURI();
        Locale locale = getFallbackLocale();

        String[] handleParts = StringUtils.split(i18nUri, '/');
        for (String part : handleParts) {
            String[] localeArr = StringUtils.split(part, "_");
            if (localeArr.length == 1) {
                locale = new Locale(StringUtils.substringBefore(localeArr[0], "."));
                if (isLocaleSupported(locale)) {
                    break;
                }
            } else if (localeArr.length == 2) {
                locale = new Locale(localeArr[0], StringUtils.substringBefore(localeArr[1], "."));
                if (isLocaleSupported(locale)) {
                    break;
                } else {
                    locale = new Locale(localeArr[0]);
                    if (isLocaleSupported(locale)) {
                        break;
                    }
                }
            } else if (localeArr.length > 2) {
                locale = new Locale(localeArr[0], localeArr[1]);
                if (isLocaleSupported(locale)) {
                    break;
                }
            } else {
                locale = new Locale(localeArr[0]);
                if (isLocaleSupported(locale)) {
                    break;
                }
            }
        }

        if (!isLocaleSupported(locale)) {
            locale = getFallbackLocale();
        } else {
            LOGGER.debug("Supported locale found: " + locale.getLanguage());            
        }
        return locale;
    }

    /**
     * Checks if the Locale is supported, added in mgnl config.
     */
    protected boolean isLocaleSupported(Locale locale) {
        return locale != null && _locales.containsKey(locale.toString());
    }

    /**
     * Gets the normal node data.
     */
    public NodeData getNodeData(Content node, String name, Locale locale) throws RepositoryException {
        // return the node data
        return node.getNodeData(name);
    }

    /**
     * Gets the normal node data.
     */
    public NodeData getNodeData(Content node, String name) {
        // return the node data
        return node.getNodeData(name);
    }

    public boolean isEnabled() {
        return _enabled;
    }

    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    public Collection getLocales() {
        return _locales.values();
    }

    /**
     * Adds a locale.
     */
    public void addLocale(LocaleDefinition ld) {
        if (ld.isEnabled()) {
            _locales.put(ld.getId(), ld.getLocale());
        }
    }
}
