package com.aperto.magkit.utils;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.templatingkit.sites.SiteManager;
import info.magnolia.objectfactory.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;

import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static org.apache.commons.lang.StringUtils.*;

/**
 * Static utility methods for locales (languages).<br>
 *
 * @author jfrantzius
 */
public final class LocaleUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocaleUtil.class);

    private LocaleUtil() {
        // keep checkstyle happy
    }

    /**
     * Builds a list with all i18n locales (languages) configured in magnolia,
     * i.e. the default site's locales.
     */
    public static Set<String> getConfiguredLanguages() {
        Collection<Locale> locales = getSiteLocales();
        Set<String> result = new LinkedHashSet<String>();
        for (Locale locale : locales) {
            result.add(locale.getLanguage());
        }
        return result;
    }

    /**
     * Cache result of querying Magnolia config (in case Magnolia doesn't).
     */
    private static List<Locale> c_locales;
    
    /**
     * Return the default site's locales as configured in STK or ETK.
     */
    public static List<Locale> getSiteLocales() {
        if (c_locales == null) {
            SiteManager siteManager = Components.getComponent(SiteManager.class);
            I18nContentSupport i18n = siteManager.getDefaultSite().getI18n();
            Collection<Locale> locales = i18n.getLocales();
            c_locales = new ArrayList<Locale>(locales);
        }
        return c_locales;
    }
    
    /**
     * Return the default site's default locale.
     */
    public static Locale getDefaultSiteLocale() {
        SiteManager siteManager = Components.getComponent(SiteManager.class);
        I18nContentSupport i18n = siteManager.getDefaultSite().getI18n();
        return i18n.getFallbackLocale();
    }
    
    /**
     * Determines the locale string from content.
     */
    public static String determineLocaleFromContent(Node node) {
        String locale = "en";
        try {
            String handle = node.getPath();
            locale = determineLocaleFromPath(handle);
        } catch (RepositoryException e) {
            LOGGER.error("Error message.", e);
        }
        return locale;
    }

    /**
     * Determines the locale string from path.
     *
     * @return null if no locale found in path.
     */
    public static String determineLocaleFromPath(String path) {
        Set<String> configuredLocales = getConfiguredLanguages();
        return determineLanguage(path, configuredLocales);
    }

    /**
     * Determines the locale label from page node.
     *
     * @return empty string if no locale is found for path.
     */
    public static String determineLocaleLabelFromNodePath(Node page) {
        String label = EMPTY;
        String lang = determineLocaleFromPath(getPathIfPossible(page));
        if (isNotBlank(lang)) {
            Locale locale = new Locale(lang);
            label = locale.getDisplayName(locale);
        }
        return label;
    }

    /**
     * Allow for parallel usage of {MbcContentLanguages} as source of configured locales (languages).
     * See class comment.
     */
    public static String determineLanguage(String handle, Collection<String> configuredLocales) {
        String localeString = null;
        String[] pathElements = split(handle, '/');

        for (String pathPart : pathElements) {
            if (configuredLocales.contains(pathPart)) {
                localeString = pathPart;
                break;
            }
        }
        return localeString;
    }

    /**
     * List of all available countries, sorted by name.
     *
     * @return list of countries (countryCode, countryName)
     */
    public static Map<String, String> getAvailableCountries() {
        Locale[] locales = Locale.getAvailableLocales();
        Map<String, String> options = new LinkedHashMap<String, String>();
        List<String> countryList = new ArrayList<String>();
        for (Locale locale : locales) {
            String code = locale.getCountry();
            String name = locale.getDisplayCountry();

            if (isNotEmpty(code) && isNotEmpty(name) && !countryList.contains(code)) {
                countryList.add(code);
                options.put(name, code);
            }
        }
        return options;
    }
}