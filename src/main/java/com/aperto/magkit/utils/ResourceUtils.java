package com.aperto.magkit.utils;

import info.magnolia.cms.util.Resource;
import static org.apache.commons.lang.StringUtils.*;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

/**
 * Util class for handle magnolia resource.
 * E.g. Helper for accessing the activePage, localContent or selectors.
 *
 * @author frank.sommer (29.05.2008)
 */
public final class ResourceUtils {
    private static final Logger LOGGER = Logger.getLogger(ResourceUtils.class);
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
        if (!isBlank(Resource.getSelector())) {
            String[] strings = split(Resource.getSelector(), '.');
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
        String selector = Resource.getSelector();
        if (isNotBlank(selectorId) && isNotBlank(selector)) {
            String[] strings = split(Resource.getSelector(), '.');
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
        if (!isBlank(Resource.getSelector())) {
            String[] strings = split(Resource.getSelector(), '.');
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

    private ResourceUtils() {
    }
}
