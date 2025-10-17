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
 * Utility class for common date handling within Magnolia queries.
 * <p>
 * Provides helper functionality to format {@link java.util.Date} instances
 * into JCR compatible ISO-8601 date-time literals including milliseconds and
 * a timezone offset with colon (pattern: {@code yyyy-MM-dd'T'HH:mm:ss.SSS±HH:MM}).
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Generates JCR/XPath compliant date literal usable inside Magnolia repository queries.</li>
 *   <li>Ensures the timezone offset contains a colon (e.g. {@code +02:00}) as required by ISO-8601
 *       and JCR specifications.</li>
 *   <li>Stateless, immutable and therefore thread-safe.</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * Date now = new Date();
 * String jcrDate = DateUtils.createQueryDate(now);
 * // Example result: "2025-10-17T14:53:21.123+02:00"
 * </pre>
 *
 * <h2>Null & Error Handling</h2>
 * Passing {@code null} to {@link #createQueryDate(Date)} will result in a {@link NullPointerException}
 * thrown by the underlying formatter.
 *
 * <h2>Thread-safety</h2>
 * The class is thread-safe because it is stateless and only uses immutable formatting operations.
 *
 * @author frank.sommer
 * @since 08.04.13
 */
public final class DateUtils {

    /**
     * Formats the given date into a JCR query compatible ISO-8601 string with millisecond precision and a colon in the
     * timezone offset (pattern: {@code yyyy-MM-dd'T'HH:mm:ss.SSS±HH:MM}).
     *
     * @param date the date to format (must not be {@code null})
     * @return formatted JCR query date string
     * @throws NullPointerException if {@code date} is {@code null}
     */
    public static String createQueryDate(Date date) {
        String queryDate = DateFormatUtils.format(date, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return queryDate.substring(0, queryDate.length() - 2) + ":" + queryDate.substring(queryDate.length() - 2);
    }

    private DateUtils() {
    }
}
