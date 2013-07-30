package com.aperto.magkit.utils;

import info.magnolia.cms.util.SelectorUtil;
import info.magnolia.context.MgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.aperto.magkit.utils.LinkTool.getEncodedParameterLinkString;
import static info.magnolia.cms.core.Path.SELECTOR_DELIMITER;
import static java.lang.Math.max;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.math.NumberUtils.toInt;

/**
 * Util class for handle with magnolia selectors.
 *
 * @author frank.sommer (29.05.2008)
 */
public final class SelectorUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectorUtils.class);
    public static final int DEF_PAGE = 1;
    public static final String SELECTOR_PRINT = "print";
    public static final String SELECTOR_PAGING = "pid";
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
