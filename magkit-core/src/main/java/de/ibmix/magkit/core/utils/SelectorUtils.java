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
 * Util class for handle with magnolia selectors.
 *
 * @author frank.sommer (29.05.2008)
 */
public abstract class SelectorUtils {
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
     *
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
     * Checks, if the selector contains the search term.
     *
     * @param search     search term
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
     * @param url                 url to manipulate
     * @param id                  Id of the selector, e.g. 'pid'
     * @param value               Value of the selector id
     * @param notAllowedSelectors array of not allowed selector ids
     * @return url with updated selectors or empty string if url is null or empty
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
