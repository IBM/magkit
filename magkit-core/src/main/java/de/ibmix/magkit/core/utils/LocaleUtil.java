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
 * Utility class providing static helper methods for working with {@link Locale} and Magnolia i18n configuration.
 *
 * <p>Core functionalities:</p>
 * <ul>
 *   <li>Access to the configured site locales and their ISO language codes.</li>
 *   <li>Determination of a locale (language) from a JCR node or path.</li>
 *   <li>Lookup of display labels for locales based on path information.</li>
 *   <li>Retrieval of available countries mapped (countryName -&gt; countryCode).</li>
 * </ul>
 *
 * <p>Important details:</p>
 * <ul>
 *   <li>The list of site locales is cached the first time {@link #getSiteLocales()} is called. Subsequent changes
 *       to Magnolia's site configuration will NOT be reflected until {@link #resetDefaultSiteLocals()} is invoked.</li>
 *   <li>If no fallback locale is configured, {@link java.util.Locale#ENGLISH} is used as default.</li>
 *   <li>Locale extraction from a path scans path segments and returns the first segment matching a configured language.</li>
 * </ul>
 *
 * <p>Null and error handling:</p>
 * <ul>
 *   <li>Methods returning language codes may return {@code null} or an empty string when no configured locale is found.</li>
 *   <li>{@link #determineLocaleFromContent(Node)} falls back to the default site locale when none is found in the path.</li>
 * </ul>
 * <p>
 * Thread-safety: This class is effectively thread-safe for read operations on cached data after initialization. The
 * cache initialization is not synchronized; concurrent first access may initialize the cache multiple times with the
 * same logical result. Manual reset via {@link #resetDefaultSiteLocals()} is not thread-safe and should only be used
 * in controlled test scenarios.
 * </p>
 * <p>Side effects: Calling {@link #resetDefaultSiteLocals()} clears the static cache and forces re-reading Magnolia's
 * configuration on the next {@link #getSiteLocales()} invocation.
 * </p>
 * Usage example:
 * <pre>{@code
 * // Obtain configured language codes
 * Set<String> languages = LocaleUtil.getConfiguredLanguages();
 * // Determine language from path
 * String lang = LocaleUtil.determineLocaleFromPath("/travel/en/home");
 * // Fallback behavior
 * String contentLang = LocaleUtil.determineLocaleFromContent(node);
 * }</pre>
 *
 * @author jfrantzius
 * @since 2014-05-14
 */
public final class LocaleUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocaleUtil.class);

    /**
     * Cache result of querying Magnolia config (in case Magnolia doesn't).
     */
    private static List<Locale> DEFAULT_LOCALS_CACHE = null;

    private LocaleUtil() {
        // keep checkstyle happy
    }

    /**
     * Builds an ordered set of all configured ISO language codes (Magnolia i18n locales) of the default site.
     * <p>The languages are derived from {@link #getSiteLocales()} by extracting {@link Locale#getLanguage()}.</p>
     *
     * @return ordered set of configured language codes (never null, may be empty)
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
     * Returns the list of {@link Locale}s configured for the default site.
     * <p>Caches the first successful lookup statically. Subsequent changes to Magnolia's configuration are ignored
     * until {@link #resetDefaultSiteLocals()} is called.</p>
     * <p>Note: The returned list may be {@code null} if the i18n configuration is not available.</p>
     *
     * @return list of configured site locales or {@code null} if not resolvable
     */
    public static List<Locale> getSiteLocales() {
        if (DEFAULT_LOCALS_CACHE == null) {
            SiteManager siteManager = Components.getComponent(SiteManager.class);
            I18nContentSupport i18n = siteManager.getDefaultSite().getI18n();
            if (i18n != null) {
                Collection<Locale> locales = i18n.getLocales();
                DEFAULT_LOCALS_CACHE = new ArrayList<>(locales);
                LOGGER.debug("Initialized site locales: {}", DEFAULT_LOCALS_CACHE);
            } else {
                LOGGER.warn("I18nContentSupport not available - site locales remain null.");
            }
        }
        return DEFAULT_LOCALS_CACHE;
    }

    /**
     * Resets the cached site locales so that a subsequent call to {@link #getSiteLocales()} re-reads Magnolia's
     * configuration. Intended for testing only.
     */
    static void resetDefaultSiteLocals() {
        DEFAULT_LOCALS_CACHE = null;
        LOGGER.debug("Site locales cache reset.");
    }

    /**
     * Returns the configured fallback {@link Locale} for the default Magnolia site.
     * <p>Falls back to {@link Locale#ENGLISH} if no fallback is defined or if i18n configuration is absent.</p>
     *
     * @return the fallback locale (never null)
     */
    public static Locale getDefaultSiteLocale() {
        SiteManager siteManager = Components.getComponent(SiteManager.class);
        I18nContentSupport i18n = siteManager.getDefaultSite().getI18n();
        return i18n != null && i18n.getFallbackLocale() != null ? i18n.getFallbackLocale() : Locale.ENGLISH;
    }

    /**
     * Determines the ISO language code from the path of the provided JCR {@link Node}.
     * <p>If no configured language is found in the path, returns the fallback site language.</p>
     *
     * @param node current JCR node (may be null)
     * @return language code found in the node path or the fallback language code if none detected (never null, never empty)
     */
    public static String determineLocaleFromContent(Node node) {
        String handle = node != null ? getPathIfPossible(node) : EMPTY;
        String locale = determineLocaleFromPath(handle);
        return isNotBlank(locale) ? locale : getDefaultSiteLocale().getLanguage();
    }

    /**
     * Determines the ISO language code from a repository path string by checking for configured languages.
     *
     * @param path current path (may be null or empty)
     * @return language code or {@code null} if none of the configured languages occur in the path
     */
    public static String determineLocaleFromPath(String path) {
        Set<String> configuredLocales = getConfiguredLanguages();
        return determineLanguage(path, configuredLocales);
    }

    /**
     * Resolves a human-readable display label for the locale detected in a page node path.
     * <p>The display name is localized to the locale itself (e.g. "Deutsch", "English").</p>
     *
     * @param page current page node (may be null)
     * @return localized display name of the detected locale or empty string if none found
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
     * Scans a path for the first segment that matches one of the provided language codes.
     *
     * @param path current node path (may be null or empty)
     * @param locales collection of configured language codes to match against (not null)
     * @return the first matched language code or {@code null} if none found
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
     * Builds a map of all available countries on the current JVM, sorted by display name insertion order.
     * <p>Each entry maps {@code countryName -> countryCode}. Only unique country codes are included.</p>
     *
     * @return ordered map of country display names to their ISO codes (may be empty, never null)
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
