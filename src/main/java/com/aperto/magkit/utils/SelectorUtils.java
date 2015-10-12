package com.aperto.magkit.utils;

import static info.magnolia.cms.core.Path.SELECTOR_DELIMITER;
import static java.lang.Math.max;
import static org.apache.commons.codec.CharEncoding.UTF_8;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBefore;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.apache.commons.lang.math.NumberUtils.toInt;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import info.magnolia.cms.util.SelectorUtil;
import info.magnolia.context.MgnlContext;

/**
 * Util class for handle with magnolia selectors.
 *
 * @author frank.sommer (29.05.2008)
 */
public final class SelectorUtils {
    public static final int DEF_PAGE = 1;
    public static final String SELECTOR_PRINT = "print";
    public static final String SELECTOR_PAGING = "pid";
    public static final String SELECTOR_PAGING_WITH_DELIMITER = SELECTOR_PAGING + SELECTOR_DELIMITER;
    private static final String DEF_EXTENSION = "html";

    /**
     * Checks, if a print selector is given.
     */
    public static boolean isPrintView() {
        return selectorContains(SELECTOR_PRINT, false);
    }

    /**
     * Checks a paging selector is given.
     */
    public static boolean isPagingView() {
        return selectorContains(SELECTOR_PAGING, true);
    }

    /**
     * Retrieve the actual page number from selector. Default is {@link #DEF_PAGE}.
     * @return positiv integer value of the page selector.
     */
    public static int retrieveActivePage() {
        int actPage = DEF_PAGE;
        String pagingValue = MgnlContext.getAttribute(SELECTOR_PAGING);
        if (isNotBlank(pagingValue)) {
            actPage = toInt(pagingValue, DEF_PAGE);
            actPage = max(actPage, DEF_PAGE);
        }
        return actPage;
    }

    /**
     * Retrieves the value of the wanted selector.
     * @param selectorId of the wanted selector value
     * @return value of selector
     * @deprecated use MgnlContext.getAttribute() for new selector handling
     */
    public static String retrieveValueOfSelector(String selectorId) {
        return MgnlContext.getAttribute(selectorId);
    }

    /**
     * Checks, if the selector contains the search term.
     * @param search search term
     * @param startsWith selector starts only with search term
     */
    public static boolean selectorContains(String search, boolean startsWith) {
        boolean contains = false;
        if (isNotBlank(SelectorUtil.getSelector())) {
            String[] parts = split(SelectorUtil.getSelector(), SELECTOR_DELIMITER);
            for (String part : parts) {
                if (startsWith) {
                    contains = part.startsWith(search);
                } else {
                    contains = part.equalsIgnoreCase(search);
                }
                if (contains) {
                    break;    
                }
            }
        }
        return contains;
    }

    /**
     * Add or replace the selector with the id and value for the given url.
     * If the value is blank, the selector id will be removed.
     * Not allowed selectors are removed.
     *
     * @param url url to manipulate
     * @param id Id of the selector, e.g. 'pid'
     * @param value Value of the selector id
     * @param notAllowedSelectors array of not allowed selector ids
     * @return url with updated selectors or empty string if url is null or empty
     */
    public static String updateSelectors(String url, String id, String value, String... notAllowedSelectors) {
        String result = trimToEmpty(url);
        if (isNotEmpty(result)) {
            String encodedSelectorValue = urlEncode(value);
            String extensionWithQueryString = substringAfterLast(result, ".");
            String pathWithSelector = substringBeforeLast(result, ".");
            String extension = DEF_EXTENSION;
            String query = "";

            if (isBlank(extensionWithQueryString)) {
                if (result.contains("?")) {
                    query = substringAfter(result, "?");
                    pathWithSelector = substringBefore(result, "?");
                }
            } else if (extensionWithQueryString.contains("?")) {
                extension = substringBefore(extensionWithQueryString, "?");
                query = substringAfter(extensionWithQueryString, "?");
            } else {
                extension = extensionWithQueryString;
            }

            String path = substringBefore(pathWithSelector, SELECTOR_DELIMITER);
            String selectorString = substringAfter(pathWithSelector, SELECTOR_DELIMITER);
            String[] selectors = split(selectorString, SELECTOR_DELIMITER);
            List<String> newSelectors = createNewSelectors(id, encodedSelectorValue, selectors, notAllowedSelectors);

            result = join(new String[]{path, SELECTOR_DELIMITER, join(newSelectors, SELECTOR_DELIMITER), SELECTOR_DELIMITER, ".", extension});
            if (isNotEmpty(query)) {
                result += "?" + query;
            }
        }
        return result;
    }

    private static List<String> createNewSelectors(final String id, final String encodedSelectorValue, final String[] selectors, final String[] notAllowedSelectors) {
        List<String> newSelectors = new ArrayList<String>();
        boolean selectorFound = false;

        for (String selector : selectors) {
            String newSelector = selector;
            String selectorId = substringBefore(newSelector, "=");
            if (selectorId.equals(id)) {
                newSelector = selectorId + "=" + encodedSelectorValue;
                selectorFound = true;
            }
            if (!ArrayUtils.contains(notAllowedSelectors, selectorId)) {
                newSelectors.add(newSelector);
            }
        }

        if (!selectorFound) {
            newSelectors.add(id + "=" + encodedSelectorValue);
        }
        return newSelectors;
    }

    private static String urlEncode(final String value) {
        String encodedSelectorValue = null;
        if (isNotEmpty(value)) {
            try {
                encodedSelectorValue = URLEncoder.encode(value, UTF_8);
            } catch (UnsupportedEncodingException e) {
                // should not happen
            }
        }
        return encodedSelectorValue;
    }

    private SelectorUtils() {
    }
}
