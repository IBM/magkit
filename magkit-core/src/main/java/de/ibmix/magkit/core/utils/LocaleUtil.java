package de.ibmix.magkit.core.utils;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.split;

/**
 * Static utility methods for locales (languages).
 *
 * @author jfrantzius
 * @since 2014-05-14
 */
public final class LocaleUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocaleUtil.class);

    private LocaleUtil() {
        // keep checkstyle happy
    }

    /**
     * Builds a list with all i18n locales (languages) configured in magnolia,
     * i.e. the default site's locales.
     *
     * @return configured languages
     */
    public static Set<String> getConfiguredLanguages() {
        Collection<Locale> locales = getSiteLocales();
        Set<String> result = new LinkedHashSet<>();
        for (Locale locale : locales) {
            result.add(locale.getLanguage());
        }
        return result;
    }

    /**
     * Cache result of querying Magnolia config (in case Magnolia doesn't).
     */
    private static List<Locale> c_defaultSiteLocals;

    /**
     * Return the default site's locales as configured.
     * ! Note that this list is cached as static class constant.
     * All changes of site configuration after first call of this method will not take effect here.
     *
     * @return the list of all configured locals, never null
     */
    public static List<Locale> getSiteLocales() {
        if (c_defaultSiteLocals == null) {
            SiteManager siteManager = Components.getComponent(SiteManager.class);
            I18nContentSupport i18n = siteManager.getDefaultSite().getI18n();
            if (i18n != null) {
                Collection<Locale> locales = i18n.getLocales();
                c_defaultSiteLocals = new ArrayList<>(locales);
            }
        }
        return c_defaultSiteLocals;
    }

    // introduced for testing. However, caching the locales in a static class field may not be a good idea at all.
    static void resetDefaultSiteLocals() {
        c_defaultSiteLocals = null;
    }

    /**
     * Return the default site's fallback locale.
     *
     * @return the site fallback local or Locale.ENGLISH if non has been configured
     */
    public static Locale getDefaultSiteLocale() {
        SiteManager siteManager = Components.getComponent(SiteManager.class);
        I18nContentSupport i18n = siteManager.getDefaultSite().getI18n();
        return i18n != null && i18n.getFallbackLocale() != null ? i18n.getFallbackLocale() : Locale.ENGLISH;
    }

    /**
     * Determines the ISO language code from content node path.
     * Fallback to the default site fallback locale.
     *
     * @param node  current jcr node
     * @return the configured language code from node path or the default site language code if non has been found in path
     */
    public static String determineLocaleFromContent(Node node) {
        String handle = node != null ? getPathIfPossible(node) : EMPTY;
        String locale = determineLocaleFromPath(handle);
        return isNotBlank(locale) ? locale : getDefaultSiteLocale().getLanguage();
    }

    /**
     * Determines the ISO language code from the node path.
     *
     * @param path current path
     * @return the language code or null if no configured locale found in path.
     */
    public static String determineLocaleFromPath(String path) {
        Set<String> configuredLocales = getConfiguredLanguages();
        return determineLanguage(path, configuredLocales);
    }

    /**
     * Determines the locale label from page node path.
     *
     * @param page current page node
     * @return the language name or empty string if no locale is found for path.
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
     * Extract the first ISO language code from the path string, that is a configured site language.
     *
     * @param path current node path
     * @param locales list of locale strings
     * @return the first language code from the string or null if not found
     */
    public static String determineLanguage(String path, Collection<String> locales) {
        String localeString = null;
        String[] pathElements = split(path, '/');

        for (String pathPart : pathElements) {
            if (locales.contains(pathPart)) {
                localeString = pathPart;
                break;
            }
        }
        return localeString;
    }

    /**
     * List of all available countries, sorted by name.
     *
     * @return Map of countries and their names (countryName, countryCode)
     */
    public static Map<String, String> getAvailableCountries() {
        Locale[] locales = Locale.getAvailableLocales();
        Map<String, String> options = new LinkedHashMap<>();
        List<String> countryList = new ArrayList<>();
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
