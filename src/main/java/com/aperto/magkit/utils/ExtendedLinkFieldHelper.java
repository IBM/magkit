package com.aperto.magkit.utils;

import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.net.URI;

import static com.aperto.magkit.utils.LinkTool.isAnchor;
import static com.aperto.magkit.utils.LinkTool.isPath;
import static com.aperto.magkit.utils.LinkTool.isUuid;
import static info.magnolia.cms.core.Path.SELECTOR_DELIMITER;
import static org.apache.commons.lang3.StringUtils.chomp;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.lastIndexOf;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBefore;

/**
 * Helper class to get partitions of an uri.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 03.06.2015
 */
@Singleton
public class ExtendedLinkFieldHelper {

    public static final String SUFFIX_ANCHOR = "_anchor";
    public static final String SUFFIX_QUERY = "_query";
    public static final String SUFFIX_SELECTOR = "_selector";

    private static final String TAG_FILE_EXTENSION = ".";
    private static final int DEFAULT_UUID_LENGTH = 36;

    /**
     * Returns the path of an uri. If the uri scheme contains an identifier, the identifier is returned instead, even it is treaded as path internally.
     *
     * @param value the uri string
     * @return the path or the uuid
     */
    public String getBase(final String value) {
        final URI uri = createUri(value);
        String result = null;
        if (uri != null) {
            result = substringBefore(uri.getPath(), SELECTOR_DELIMITER) + substringAfterLast(uri.getPath(), SELECTOR_DELIMITER);
            final String identifier = removeStart(result, "/");
            if (isUuid(identifier)) {
                result = identifier;
            }
        }
        return result;
    }

    /**
     * Removes all components from the given uri.
     *
     * @param value the uri, which might contain additional components
     * @return everything except the path
     */
    public String stripBase(final String value) {
        return mergeComponents(null, getSelectors(value), getQuery(value), getAnchor(value));
    }

    public String getSelectors(final String uri) {
        String nodePath = uri;
        final StringBuilder selectors = new StringBuilder();
        while (containsMoreSelectors(nodePath)) {
            nodePath = substringAfter(nodePath, SELECTOR_DELIMITER);
            if (containsMoreSelectors(nodePath)) {
                selectors.append(substringBefore(nodePath, SELECTOR_DELIMITER)).append(SELECTOR_DELIMITER);
            }
        }
        return selectors.length() == 0 ? null : chomp(selectors.toString(), SELECTOR_DELIMITER);
    }

    public String getQuery(final String uri) {
        final URI obj = createUri(uri);
        return obj == null ? null : obj.getQuery();
    }

    public String getAnchor(final String uri) {
        final URI obj = createUri(uri);
        return obj == null ? null : obj.getFragment();
    }

    public String mergeComponents(final String base, final String selector, final String query, final String anchor) {
        String result = defaultString(base);

        if (isNotBlank(query)) {
            result += "?" + query;
        }
        if (isNotBlank(anchor)) {
            result += "#" + anchor;
        }

        if (isNotBlank(selector)) {
            String baseResult = result;
            if (isNotBlank(anchor)) {
                baseResult = substringBefore(baseResult, "#");
            }
            if (isNotBlank(query)) {
                baseResult = substringBefore(baseResult, "?");
            }
            int extIndex = lastIndexOf(baseResult, TAG_FILE_EXTENSION);
            if (extIndex == -1) {
                extIndex = baseResult.length();
            }

            result = StringUtils.substring(result, 0, extIndex) + SELECTOR_DELIMITER + selector + SELECTOR_DELIMITER + StringUtils.substring(result, extIndex);
        }

        return result;
    }

    /**
     * Creates an URI from given string.
     * @param value a path or an uuid, and additional uri components
     * @return the uri or null
     */
    protected URI createUri(final String value) {
        return isPath(value) || isAnchor(value) ? URI.create(value) : isUuid(substring(value, 0, DEFAULT_UUID_LENGTH)) ? URI.create("/" + defaultString(value)) : null;
    }

    protected boolean containsMoreSelectors(final String uri) {
        return !startsWith(uri, TAG_FILE_EXTENSION) && contains(uri, SELECTOR_DELIMITER);
    }
}
