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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Utility methods to handle and convert JCR {@link Value} instances into common Java types.
 * <p>Main functionalities and key features:</p>
 * <ul>
 *   <li>Type-safe conversion of {@link Value} to String, Calendar, Long, Double, Boolean, Binary and BigDecimal.</li>
 *   <li>Uniform null and error handling: if the provided value is null or conversion fails, a provided fallback is returned.</li>
 *   <li>Graceful degradation: exceptions are caught and logged; no exception is thrown to the caller.</li>
 *   <li>Consistent with Jackrabbit {@code BaseValue} conversions (e.g. {@code value.getString()}).</li>
 *   <li>Avoids Magnolia {@code PropertyUtil.getValueString(.)} date formatting differences by relying directly on JCR {@code Value} conversion.</li>
 * </ul>
 *
 * <p>
 * Usage preconditions: All methods accept {@code null} for the {@code value} parameter; optional fallback may also be {@code null}.
 * </p>
 * <p>
 * Null and error handling: If {@code value} is {@code null} or a {@link RepositoryException} is thrown while converting,
 * the respective method returns the provided {@code fallback}. If no fallback was provided, {@code null} is returned.
 * </p>
 * <p>
 * Side effects: On conversion failure a message is logged at ERROR (short message) and DEBUG (with exception) level; no other side effects.
 * </p>
 * <p>
 * Thread-safety: The class is stateless and all methods are pure (aside from logging). It is fully thread-safe.
 * </p>
 * <p>Example usage:</p>
 * <pre>
 *   Value jcrValue = property.getValue();
 *   String asString = ValueUtils.valueToString(jcrValue, "fallback");
 *   Long number = ValueUtils.valueToLong(jcrValue); // may be null if not convertible
 * </pre>
 *
 * @author wolf.bubenik
 * @since 2018-12-20
 */
public final class ValueUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ValueUtils.class);

    /**
     * Convert a JCR {@link Value} to its String representation or {@code null}.
     * Delegates to {@link Value#getString()} and returns {@code null} if conversion fails.
     *
     * @param value the JCR value (may be {@code null})
     * @return the string value or {@code null} if {@code value} is {@code null} or conversion fails
     */
    public static String valueToString(@Nullable final Value value) {
        return valueToString(value, null);
    }

    /**
     * Convert a JCR {@link Value} to its String representation, returning a fallback on failure.
     *
     * @param value    the JCR value (may be {@code null})
     * @param fallback the fallback returned if {@code value} is {@code null} or conversion fails (may be {@code null})
     * @return the converted string or the provided {@code fallback}
     */
    public static String valueToString(@Nullable final Value value, @Nullable final String fallback) {
        String result = null;
        if (value != null) {
            try {
                result = value.getString();
            } catch (RepositoryException e) {
                LOG.error("Cannot convert value into String. Returning null." + e.getMessage());
                LOG.debug("Cannot convert value into String. Returning null." + e);
            }
        }
        return result == null ? fallback : result;
    }

    /**
     * Convert a JCR {@link Value} into a {@link Calendar} or {@code null}.
     * Delegates to {@link Value#getDate()}.
     *
     * @param value the JCR value (may be {@code null})
     * @return the calendar instance or {@code null} if {@code value} is {@code null} or conversion fails
     */
    public static Calendar valueToCalendar(@Nullable final Value value) {
        return valueToCalendar(value, null);
    }

    /**
     * Convert a JCR {@link Value} into a {@link Calendar}, returning a fallback on failure.
     *
     * @param value    the JCR value (may be {@code null})
     * @param fallback the fallback calendar returned if {@code value} is {@code null} or conversion fails (may be {@code null})
     * @return the converted calendar or the provided {@code fallback}
     */
    public static Calendar valueToCalendar(@Nullable final Value value, @Nullable final Calendar fallback) {
        Calendar result = null;
        if (value != null) {
            try {
                result = value.getDate();
            } catch (RepositoryException e) {
                LOG.error("Cannot convert value into Calendar. Returning null." + e.getMessage());
                LOG.debug("Cannot convert value into Calendar. Returning null." + e);
            }
        }
        return result == null ? fallback : result;
    }

    /**
     * Convert a JCR {@link Value} into a {@link Long} or {@code null}.
     * Delegates to {@link Value#getLong()}.
     *
     * @param value the JCR value (may be {@code null})
     * @return the long value or {@code null} if {@code value} is {@code null} or conversion fails
     */
    public static Long valueToLong(@Nullable final Value value) {
        return valueToLong(value, null);
    }

    /**
     * Convert a JCR {@link Value} into a {@link Long}, returning a fallback on failure.
     *
     * @param value    the JCR value (may be {@code null})
     * @param fallback the fallback long returned if {@code value} is {@code null} or conversion fails (may be {@code null})
     * @return the converted long or the provided {@code fallback}
     */
    public static Long valueToLong(@Nullable final Value value, @Nullable final Long fallback) {
        Long result = null;
        if (value != null) {
            try {
                result = value.getLong();
            } catch (RepositoryException e) {
                LOG.error("Cannot convert value into Long. Returning null." + e.getMessage());
                LOG.debug("Cannot convert value into Long. Returning null." + e);
            }
        }
        return result == null ? fallback : result;
    }

    /**
     * Convert a JCR {@link Value} into a {@link Double} or {@code null}.
     * Delegates to {@link Value#getDouble()}.
     *
     * @param value the JCR value (may be {@code null})
     * @return the double value or {@code null} if {@code value} is {@code null} or conversion fails
     */
    public static Double valueToDouble(@Nullable final Value value) {
        return valueToDouble(value, null);
    }

    /**
     * Convert a JCR {@link Value} into a {@link Double}, returning a fallback on failure.
     *
     * @param value    the JCR value (may be {@code null})
     * @param fallback the fallback double returned if {@code value} is {@code null} or conversion fails (may be {@code null})
     * @return the converted double or the provided {@code fallback}
     */
    public static Double valueToDouble(@Nullable final Value value, @Nullable final Double fallback) {
        Double result = null;
        if (value != null) {
            try {
                result = value.getDouble();
            } catch (RepositoryException e) {
                LOG.error("Cannot convert value into Double. Returning null." + e.getMessage());
                LOG.debug("Cannot convert value into Double. Returning null." + e);
            }
        }
        return result == null ? fallback : result;
    }

    /**
     * Convert a JCR {@link Value} into a {@link Boolean} or {@code null}.
     * Delegates to {@link Value#getBoolean()}.
     *
     * @param value the JCR value (may be {@code null})
     * @return the boolean value or {@code null} if {@code value} is {@code null} or conversion fails
     */
    public static Boolean valueToBoolean(@Nullable final Value value) {
        return valueToBoolean(value, null);
    }

    /**
     * Convert a JCR {@link Value} into a {@link Boolean}, returning a fallback on failure.
     *
     * @param value    the JCR value (may be {@code null})
     * @param fallback the fallback boolean returned if {@code value} is {@code null} or conversion fails (may be {@code null})
     * @return the converted boolean or the provided {@code fallback}
     */
    public static Boolean valueToBoolean(@Nullable final Value value, @Nullable final Boolean fallback) {
        Boolean result = null;
        if (value != null) {
            try {
                result = value.getBoolean();
            } catch (RepositoryException e) {
                LOG.error("Cannot convert value into Boolean. Returning null." + e.getMessage());
                LOG.debug("Cannot convert value into Boolean. Returning null." + e);
            }
        }
        return result == null ? fallback : result;
    }

    /**
     * Convert a JCR {@link Value} into a {@link Binary} or {@code null}.
     * Delegates to {@link Value#getBinary()}.
     *
     * @param value the JCR value (may be {@code null})
     * @return the binary value or {@code null} if {@code value} is {@code null} or conversion fails
     */
    public static Binary valueToBinary(@Nullable final Value value) {
        return valueToBinary(value, null);
    }

    /**
     * Convert a JCR {@link Value} into a {@link Binary}, returning a fallback on failure.
     *
     * @param value    the JCR value (may be {@code null})
     * @param fallback the fallback binary returned if {@code value} is {@code null} or conversion fails (may be {@code null})
     * @return the converted binary or the provided {@code fallback}
     */
    public static Binary valueToBinary(@Nullable final Value value, @Nullable final Binary fallback) {
        Binary result = null;
        if (value != null) {
            try {
                result = value.getBinary();
            } catch (RepositoryException e) {
                LOG.error("Cannot convert value into Binary. Returning null." + e.getMessage());
                LOG.debug("Cannot convert value into Binary. Returning null." + e);
            }
        }
        return result == null ? fallback : result;
    }

    /**
     * Convert a JCR {@link Value} into a {@link BigDecimal} or {@code null}.
     * Delegates to {@link Value#getDecimal()}.
     *
     * @param value the JCR value (may be {@code null})
     * @return the decimal value or {@code null} if {@code value} is {@code null} or conversion fails
     */
    public static BigDecimal valueToBigDecimal(@Nullable final Value value) {
        return valueToBigDecimal(value, null);
    }

    /**
     * Convert a JCR {@link Value} into a {@link BigDecimal}, returning a fallback on failure.
     *
     * @param value    the JCR value (may be {@code null})
     * @param fallback the fallback decimal returned if {@code value} is {@code null} or conversion fails (may be {@code null})
     * @return the converted decimal or the provided {@code fallback}
     */
    public static BigDecimal valueToBigDecimal(@Nullable final Value value, @Nullable final BigDecimal fallback) {
        BigDecimal result = null;
        if (value != null) {
            try {
                result = value.getDecimal();
            } catch (RepositoryException e) {
                LOG.error("Cannot convert value into BigDecimal. Returning null." + e.getMessage());
                LOG.debug("Cannot convert value into BigDecimal. Returning null." + e);
            }
        }
        return result == null ? fallback : result;
    }

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ValueUtils() {}
}
