package com.aperto.magkit.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.startsWithIgnoreCase;

/**
 * Helper class for links.
 *
 * @author Frank Sommer (25.10.2007)
 */
public final class LinkTool {
    public static final Pattern UUID_PATTERN = Pattern.compile("^[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}$");

    /**
     * Checks if the given link is a uuid.
     *
     * @see #UUID_PATTERN
     * @param link to check
     * @return true or false
     */
    public static boolean isUuid(String link) {
        boolean isUuid = false;
        if (isNotEmpty(link)) {
            Matcher matcher = UUID_PATTERN.matcher(link);
            isUuid = matcher.matches();
        }
        return isUuid;
    }

    /**
     * Checks, if the link starts with a web protocol.
     *
     * @param linkValue to check
     * @return true for external link
     */
    public static boolean isExternalLink(final String linkValue) {
        return startsWithIgnoreCase(linkValue, "https://") || startsWithIgnoreCase(linkValue, "http://");
    }

    private LinkTool() {
    }
}