package com.aperto.magkit.utils;

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
 * Utility methods to handle and convert jcr property values.
 * Here we relay on the value conversion of the jackrabbit BaseValue implementations.
 * This is consistent with property.getString() etc. but not with magnolia PropertyUtil.getValueString(.) (uses different date format).
 *
 * @author wolf.bubenik
 * @since 20.12.18.
 */
public final class ValueUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ValueUtils.class);

    public static String valueToString(@Nullable final Value value) {
        return valueToString(value, null);
    }

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

    public static Calendar valueToCalendar(@Nullable final Value value) {
        return valueToCalendar(value, null);
    }

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

    public static Long valueToLong(@Nullable final Value value) {
        return valueToLong(value, null);
    }

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

    public static Double valueToDouble(@Nullable final Value value) {
        return valueToDouble(value, null);
    }

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

    public static Boolean valueToBoolean(@Nullable final Value value) {
        return valueToBoolean(value, null);
    }

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

    public static Binary valueToBinary(@Nullable final Value value) {
        return valueToBinary(value, null);
    }

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

    public static BigDecimal valueToBigDecimal(@Nullable final Value value) {
        return valueToBigDecimal(value, null);
    }

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

    private ValueUtils() {}
}
