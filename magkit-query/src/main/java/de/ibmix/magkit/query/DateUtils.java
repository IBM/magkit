package de.ibmix.magkit.query;

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

import org.apache.commons.lang3.time.DateFormatUtils;

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
