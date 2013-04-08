package com.aperto.magkit.utils;

import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Date;

/**
 * Util class for common date handling with Magnolia.
 *
 * @author frank.sommer
 * @since 08.04.13
 */
public final class DateUtils {

    /**
     * Builds from a date the jcr date query string.
     *
     * @param date to format
     * @return date string for a jcr query
     */
    public static String createQueryDate(Date date) {
        String queryDate = DateFormatUtils.format(date, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return queryDate.substring(0, queryDate.length() - 2) + ":" + queryDate.substring(queryDate.length() - 2);
    }

    private DateUtils() {
    }
}
