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

import info.magnolia.cms.util.SelectorUtil;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

import static info.magnolia.cms.util.SelectorUtil.SELECTOR_DELIMITER;
import static java.lang.Math.max;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

/**
 * Utility class providing helper functions to work with Magnolia URL selectors (e.g. paging or print views).
 * <p>
 * Main functionalities:
 * <ul>
 *   <li>Detection of specific selectors (print, paging).</li>
 *   <li>Retrieval of the active paging number with graceful fallback and validation.</li>
 *   <li>Updating, adding, replacing or removing selectors in a given URL while preserving extension and query string.</li>
 * </ul>
 * Key features & important details:
 * <ul>
 *   <li>Selector parsing is based on Magnolia's {@link SelectorUtil#SELECTOR_DELIMITER}.</li>
 *   <li>Paging values are validated to be positive integers; defaults to {@link #DEF_PAGE} if missing or invalid.</li>
 *   <li>Not allowed selectors can be filtered out during update operations.</li>
 *   <li>URL extension and query parameters are preserved when manipulating selectors.</li>
 * </ul>
 * Usage preconditions:
 * <ul>
 *   <li>Magnolia context must be available when calling {@link #retrieveActivePage()} (uses {@link MgnlContext}).</li>
 * </ul>
 * Null & error handling:
 * <ul>
 *   <li>Null or blank URL input for {@link #updateSelectors(String, String, String, String...)} results in an empty String.</li>
 *   <li>Blank selector values trigger removal of the selector id from the URL.</li>
 * </ul>
 * Side effects: None (all methods are stateless and operate only on provided input or Magnolia context attributes).
 * Thread-safety: Fully thread-safe; methods are stateless and only read Magnolia context or operate on local data.
 * <p>
 * Usage example:
 * <pre>
 *   String updated = SelectorUtils.updateSelectors("/news.pid=2.print.html", "pid", "3", "print");
 *   // Result: /news.pid=3.html
 * </pre>
 *
 * @author frank.sommer (29.05.2008)
 * @since 2008-05-29
 */
public abstract class SelectorUtils {
    public static final int DEF_PAGE = 1;
    public static final String SELECTOR_PRINT = "print";
    public static final String SELECTOR_PAGING = "pid";
    public static final String SELECTOR_PAGING_WITH_DELIMITER = SELECTOR_PAGING + SELECTOR_DELIMITER;
    private static final String DEF_EXTENSION = "html";

    /**
     * Determines whether the current selector string contains the print selector.
     *
     * @return true if the print selector is present; false otherwise
     */
    public static boolean isPrintView() {
        return selectorContains(SELECTOR_PRINT, false);
    }

    /**
     * Determines whether the current selector string contains a paging selector (e.g. pid=...).
     *
     * @return true if a paging selector starting with {@link #SELECTOR_PAGING} exists; false otherwise
     */
    public static boolean isPagingView() {
        return selectorContains(SELECTOR_PAGING, true);
    }

    /**
     * Retrieve the active page number from Magnolia context (selector value for {@link #SELECTOR_PAGING}).
     * Ensures the returned value is a positive integer; defaults to {@link #DEF_PAGE} if missing or invalid.
     *
     * @return the active page number (>= {@link #DEF_PAGE})
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
     * Checks whether the Magnolia selector string contains a specific term.
     *
     * @param search     the search term (selector id or full selector token)
     * @param startsWith if true, matches by prefix (e.g. for key=value selectors); if false, matches case-insensitively by equality
     * @return true if the selector string contains the given term according to the matching rule
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
     * Adds or replaces a selector (id=value) within the provided URL. Removes the selector if the value is blank.
     * Filters out not allowed selector ids. Preserves file extension and query string.
     * If the URL is null or blank an empty String is returned.
     *
     * @param url                 the original URL to modify
     * @param id                  the selector id (e.g. "pid")
     * @param value               the selector value; blank value removes the selector
     * @param notAllowedSelectors selector ids to exclude from the result
     * @return the updated URL with modified selectors or an empty String if input URL is blank
     */
    public static String updateSelectors(String url, String id, String value, String... notAllowedSelectors) {
        String result = trimToEmpty(url);
        if (isNotEmpty(result)) {
            String encodedSelectorValue = EncodingUtils.getUrlEncoded(value);
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

            result = join(path, SELECTOR_DELIMITER, join(newSelectors, SELECTOR_DELIMITER), SELECTOR_DELIMITER, ".", extension);
            if (isNotEmpty(query)) {
                result += "?" + query;
            }
        }
        return result;
    }

    /**
     * Internal helper that builds the new selector list by replacing or adding the specified id=value pair
     * and filtering out not allowed selector ids.
     *
     * @param id                  selector id to add/replace
     * @param encodedSelectorValue URL-encoded selector value
     * @param selectors           existing selector tokens
     * @param notAllowedSelectors ids to remove from the result
     * @return list of resulting selector tokens
     */
    private static List<String> createNewSelectors(final String id, final String encodedSelectorValue, final String[] selectors, final String[] notAllowedSelectors) {
        List<String> newSelectors = new ArrayList<>();
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
}
