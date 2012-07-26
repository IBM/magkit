package com.aperto.magkit.utils;

import info.magnolia.cms.util.SelectorUtil;
import info.magnolia.context.MgnlContext;

import static com.aperto.magkit.utils.LinkTool.getEncodedParameterLinkString;
import static org.apache.commons.lang.StringUtils.*;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class for handle magnolia resource.
 * E.g. Helper for accessing the activePage, localContent or selectors.
 *
 * @author frank.sommer (29.05.2008)
 */
public final class SelectorUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectorUtils.class);
    public static final String SELECTOR_PRINT = "print";
    public static final String SELECTOR_PAGING = "pid";
    public static final String SELECTOR_DELIMITER = "-";
    public static final String SELECTOR_PAGING_WITH_DELIMITER = SELECTOR_PAGING + SELECTOR_DELIMITER;

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
     * Retrieve the actual page number from selector. Default is 1.
     */
    public static int retrieveActivePage() {
        int actPage = 1;
        if (!isBlank(SelectorUtil.getSelector())) {
            String[] strings = split(SelectorUtil.getSelector(), '.');
            for (String s : strings) {
                boolean found = false;
                if (s.contains(SELECTOR_PAGING_WITH_DELIMITER)) {
                    found = true;
                    actPage = NumberUtils.toInt(s.substring(SELECTOR_PAGING_WITH_DELIMITER.length()), 1);
                }
                if (found) {
                    break;
                }
            }
        }
        return actPage;
    }

    /**
     * Retrieves the value of the wanted selector. <br />
     * E.g.: "pid-5" ==&gt; "5". 
     * @param selectorId of the wanted selector value
     * @return value of selector, empty string if not found.
     */
    public static String retrieveValueOfSelector(String selectorId) {
        String value = "";
        String selector = SelectorUtil.getSelector();
        if (isNotBlank(selectorId) && isNotBlank(selector)) {
            String[] strings = split(SelectorUtil.getSelector(), '.');
            String idWithDelimiter = selectorId + SELECTOR_DELIMITER;
            for (String s : strings) {
                if (s.startsWith(idWithDelimiter)) {
                    value = s.substring(idWithDelimiter.length());
                    break;
                }
            }
        }
        return value;
    }

    /**
     * Checks, if the selector contains the search pattern.
     * @param search pattern
     * @param startsWith selector starts only with pattern
     */
    public static boolean selectorContains(String search, boolean startsWith) {
        boolean contains = false;
        if (!isBlank(SelectorUtil.getSelector())) {
            String[] strings = split(SelectorUtil.getSelector(), '.');
            for (String s : strings) {
                if (startsWith) {
                    contains = s.startsWith(search);
                } else {
                    contains = s.equalsIgnoreCase(search);
                }
                if (contains) {
                    break;    
                }
            }
        }
        return contains;
    }

    /**
     * Add or replace the selector with the id and value for the current page url.
     * If the value is blank, the selector id will be removed.
     * Not allowed selectors are removed.
     *
     * @param id Id of the selector, e.g. 'pid'.
     * @param value Value of the selector id.
     * @param notAllowedSelectors array of not allowed selector ids.
     * @return url of the current page with updated selectors.
     */
    public static String updateSelectors(String id, String value, String... notAllowedSelectors) {
        boolean found = false;

        // build the URI
        StringBuilder link = new StringBuilder(MgnlContext.getContextPath());
        String handle = MgnlContext.getAggregationState().getHandle();
        link.append(handle);

        // check the actual selectors
        String actSelector = SelectorUtil.getSelector();
        String[] parts = split(actSelector, '.');
        for (String part : parts) {
            if (part.startsWith(id + SELECTOR_DELIMITER)) {
                if (isNotBlank(value)) {
                    link.append('.').append(id).append(SELECTOR_DELIMITER).append(value);
                }
                LOGGER.debug("Replace selector {} with value {}.", id, value);
                found = true;
            } else if (notAllowedSelectors.length > 0) {
                for (String allowed : notAllowedSelectors) {
                    if (!part.startsWith(allowed + SELECTOR_DELIMITER)) {
                        link.append('.').append(part);
                        break;
                    }
                }
            } else {
                link.append('.').append(part);
            }
        }

        // add the selector, if not found
        if (!found) {
            link.append('.').append(id).append(SELECTOR_DELIMITER).append(value);
        }

        // add the extension and the query string
        link.append('.').append(MgnlContext.getAggregationState().getExtension());
        link.append(getEncodedParameterLinkString());
        return link.toString();
    }

    private SelectorUtils() {
    }
}
