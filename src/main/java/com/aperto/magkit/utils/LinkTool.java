package com.aperto.magkit.utils;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang.StringUtils.startsWithIgnoreCase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Helper class for links.
 *
 * @author Frank Sommer (25.10.2007)
 */
public final class LinkTool {
    public static final Pattern UUID_PATTERN = Pattern.compile("^[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}$");

    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final int HTTP_PORT = 80;
    public static final int HTTPS_PORT = 443;
    public static final String PROTOCOL_DELIMITER = "://";
    public static final String HTTP_PREFIX = HTTP + PROTOCOL_DELIMITER;
    public static final String HTTPS_PREFIX = HTTPS + PROTOCOL_DELIMITER;

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
     * Null-safe check, if the link starts with a web protocol.
     *
     * @param linkValue to check
     * @return true for external link
     */
    public static boolean isExternalLink(@Nullable final String linkValue) {
        return startsWithIgnoreCase(linkValue, HTTPS_PREFIX) || startsWithIgnoreCase(linkValue, HTTP_PREFIX);
    }

    /**
     * Null-safe check, if the link starts with a slash.
     *
     * @param value to check
     * @return true for path links
     */
    public static boolean isPath(@Nullable final String value) {
        return startsWith(value, "/");
    }

    /**
     * Null-safe check if value starts with #.
     * @param value to check
     * @return true for url fragment with starting #
     */
    public static boolean isAnchor(@Nullable final String value) {
        return startsWith(value, "#");
    }

    private LinkTool() {
    }
}
