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

import info.magnolia.jcr.wrapper.DelegatePropertyWrapper;
import info.magnolia.jcr.wrapper.HTMLEscapingContentDecorator;
import info.magnolia.jcr.wrapper.HTMLEscapingPropertyWrapper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.ibmix.magkit.core.utils.ValueUtils.valueToBinary;
import static de.ibmix.magkit.core.utils.ValueUtils.valueToBoolean;
import static de.ibmix.magkit.core.utils.ValueUtils.valueToCalendar;
import static de.ibmix.magkit.core.utils.ValueUtils.valueToDouble;
import static de.ibmix.magkit.core.utils.ValueUtils.valueToLong;
import static de.ibmix.magkit.core.utils.ValueUtils.valueToString;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Util class for Property handling.
 *
 * @author frank.sommer
 * @since 09.10.12
 */
public final class PropertyUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtils.class);

    static final Function<Property, String> TO_STRING_VALUE_DEFAULT_EMPTY = p -> StringUtils.defaultString(getStringValue(p));

    static final Comparator<Property> PROPERTY_NAME_COMPARATOR = (p1, p2) -> {
        int compareValue = 0;
        try {
            String name1 = p1 != null ? p1.getName() : EMPTY;
            String name2 = p2 != null ? p2.getName() : EMPTY;
            compareValue = name1.compareTo(name2);
        } catch (RepositoryException e) {
            LOGGER.error("Error comparing by name of properties.", e);
        }
        return compareValue;
    };

    /**
     * Save call on node.getProperty(String).
     * We skip checking if the Property exists and catch the Exception to avoid fetching the Property twice.
     *
     * @param node    the node to read the Property from. May be NULL.
     * @param relPath the path to the Property. May be NULL.
     * @return the addressed Property or NULL if the Node is NULL, the path is NULL or empty or the property does not exist or is not available.
     */
    public static Property getProperty(@Nullable final Node node, @Nullable final String relPath) {
        Property result = null;
        if (node != null && isNotEmpty(relPath)) {
            try {
                result = node.getProperty(relPath);
            } catch (RepositoryException e) {
                LOGGER.debug("Error retrieving property {}.", relPath, e);
            }
        }
        return result;
    }

    public static PropertyIterator getProperties(@Nullable final Node node) {
        PropertyIterator result = null;
        if (node != null) {
            try {
                result = node.getProperties();
            } catch (RepositoryException e) {
                LOGGER.debug("Error retrieving properties from node {}.", NodeUtils.getPath(node), e);
            }
        }
        return result;
    }

    public static PropertyIterator getProperties(@Nullable final Node node, @Nonnull final String namePattern) {
        PropertyIterator result = null;
        if (node != null) {
            try {
                result = node.getProperties(namePattern);
            } catch (RepositoryException e) {
                LOGGER.debug("Error retrieving properties from node {}.", NodeUtils.getPath(node), e);
            }
        }
        return result;
    }

    public static PropertyIterator getProperties(@Nullable final Node node, @Nonnull final String[] nameGlobs) {
        PropertyIterator result = null;
        if (node != null) {
            try {
                result = node.getProperties(nameGlobs);
            } catch (RepositoryException e) {
                LOGGER.debug("Error retrieving properties from node {}.", NodeUtils.getPath(node), e);
            }
        }
        return result;
    }

    /**
     * Getter for property values as List.
     * If property is single valued a List with this single value will be returned.
     * Value will be wrapped into a HtmlEscapingValueDecorator if the property was wrapped with a HTMLEscapingPropertyWrapper.
     *
     * @param input the property or null
     * @return a List&lt;Value&gt; with all values of this property, never null
     */
    public static List<Value> getValues(@Nullable final Property input) {
        Value[] values = getUnwrappedValues(input);
        if (input instanceof HTMLEscapingPropertyWrapper) {
            // We are bypassing the magnolia html encoding of nodes if we work on values instead of the node properties.
            // Here we provide an HTML escaping Value wrapper to overcome this limitation.
            HTMLEscapingContentDecorator decorator = ((HTMLEscapingPropertyWrapper) input).getContentDecorator();
            for (int i = 0; i < values.length; i++) {
                values[i] = new HtmlEscapingValueDecorator(values[i], decorator);
            }
        }
        return Arrays.asList(values);
    }

    private static Value[] getUnwrappedValues(@Nullable final Property input) {
        Value[] result = new Value[0];
        if (exists(input)) {
            try {
                result = input.isMultiple() ? input.getValues() : new Value[]{input.getValue()};
            } catch (RepositoryException e) {
                // ignore and return empty result
                LOGGER.debug("Cannot access values of property ", e);
            }
        }
        return result;
    }

    /**
     * An accessor for the property value object.
     * If property has multiple values the first one will be returned.
     * Value will be wrapped into a HtmlEscapingValueDecorator if the property was wrapped with a HTMLEscapingPropertyWrapper.
     *
     * @param input the Property, may be null
     * @return the value object of the property or NULL if the property is null or has no value
     */
    public static Value getValue(@Nullable final Property input) {
        Value result = getUnwrappedValue(input);
        if (input instanceof HTMLEscapingPropertyWrapper) {
            // We are bypassing the magnolia html encoding of nodes if we work on values instead of the node properties.
            // Here we provide an HTML escaping Value wrapper to overcome this limitation.
            HTMLEscapingContentDecorator decorator = ((HTMLEscapingPropertyWrapper) input).getContentDecorator();
            result = new HtmlEscapingValueDecorator(result, decorator);
        }
        return result;
    }

    private static Value getUnwrappedValue(@Nullable final Property input) {
        Value result = null;
        if (exists(input)) {
            try {
                result = input.isMultiple() ? ArrayUtils.get(input.getValues(), 0) : input.getValue();
            } catch (RepositoryException e) {
                // ignore and return empty result
                LOGGER.debug("Cannot access value of property ", e);
            }
        }
        return result;
    }

    /**
     * Add missing util method for retrieving multi value property values.
     *
     * @param node    containing the multivalue
     * @param relPath relative path to the multi value property
     * @return string values as collection, if not available empty collection and if single value the collection of size one.
     * @see info.magnolia.jcr.util.PropertyUtil
     */
    public static List<String> getStringValues(final Node node, final String relPath) {
        Property property = getProperty(node, relPath);
        return getStringValues(property);
    }

    public static String getStringValue(final Property property) {
        return getStringValue(property, null);
    }

    public static String getStringValue(final Property property, final String fallback) {
        return valueToString(getValue(property), fallback);
    }

    public static String getStringValue(final Node node, final String relPath) {
        return getStringValue(node, relPath, null);
    }

    public static String getStringValue(final Node node, final String relPath, final String fallback) {
        return getStringValue(getProperty(node, relPath), fallback);
    }

    public static List<String> getStringValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToString)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static Calendar getCalendarValue(final Property property) {
        return getCalendarValue(property, null);
    }

    public static Calendar getCalendarValue(final Property property, final Calendar fallback) {
        return valueToCalendar(getValue(property), fallback);
    }

    public static Calendar getCalendarValue(final Node node, final String relPath) {
        return getCalendarValue(node, relPath, null);
    }

    public static Calendar getCalendarValue(final Node node, final String relPath, final Calendar fallback) {
        return getCalendarValue(getProperty(node, relPath), fallback);
    }

    public static List<Calendar> getCalendarValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToCalendar)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static List<Calendar> getCalendarValues(final Node node, final String relPath) {
        return getCalendarValues(getProperty(node, relPath));
    }

    public static Long getLongValue(final Property property) {
        return getLongValue(property, null);
    }

    public static Long getLongValue(final Property property, final Long fallback) {
        return valueToLong(getValue(property), fallback);
    }

    public static Long getLongValue(final Node node, final String relPath) {
        return getLongValue(node, relPath, null);
    }

    public static Long getLongValue(final Node node, final String relPath, final Long fallback) {
        return getLongValue(getProperty(node, relPath), fallback);
    }

    public static List<Long> getLongValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToLong)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static List<Long> getLongValues(final Node node, final String relPath) {
        return getLongValues(getProperty(node, relPath));
    }

    public static Double getDoubleValue(final Property property) {
        return valueToDouble(getValue(property));
    }

    public static Double getDoubleValue(final Property property, final Double fallback) {
        return valueToDouble(getValue(property), fallback);
    }

    public static Double getDoubleValue(final Node node, final String relPath) {
        return getDoubleValue(node, relPath, null);
    }

    public static Double getDoubleValue(final Node node, final String relPath, final Double fallback) {
        return getDoubleValue(getProperty(node, relPath), fallback);
    }

    public static List<Double> getDoubleValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToDouble)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static List<Double> getDoubleValues(final Node node, final String relPath) {
        return getDoubleValues(getProperty(node, relPath));
    }

    public static Boolean getBooleanValue(final Property property) {
        return getBooleanValue(property, null);
    }

    public static Boolean getBooleanValue(final Property property, final Boolean fallback) {
        return valueToBoolean(getValue(property), fallback);
    }

    public static Boolean getBooleanValue(final Node node, final String relPath) {
        return getBooleanValue(node, relPath, null);
    }

    public static Boolean getBooleanValue(final Node node, final String relPath, final Boolean fallback) {
        return getBooleanValue(getProperty(node, relPath), fallback);
    }

    public static List<Boolean> getBooleanValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToBoolean)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static List<Boolean> getBooleanValues(final Node node, final String relPath) {
        return getBooleanValues(getProperty(node, relPath));
    }

    public static Binary getBinaryValue(final Property property) {
        return getBinaryValue(property, null);
    }

    public static Binary getBinaryValue(final Property property, final Binary fallback) {
        return valueToBinary(getValue(property), fallback);
    }

    public static Binary getBinaryValue(final Node node, final String relPath) {
        return getBinaryValue(node, relPath, null);
    }

    public static Binary getBinaryValue(final Node node, final String relPath, final Binary fallback) {
        return getBinaryValue(getProperty(node, relPath), fallback);
    }

    public static List<Binary> getBinaryValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToBinary)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static List<Binary> getBinaryValues(final Node node, final String relPath) {
        return getBinaryValues(getProperty(node, relPath));
    }

    /**
     * Retrieves the properties created by Magnolias MultiSelect.
     *
     * @param multiSelectNode node contains the properties
     * @return collection of properties, never null
     */
    public static Collection<Property> retrieveMultiSelectProperties(Node multiSelectNode) {
        Collection<Property> properties = emptyList();
        try {
            final RegexpChildrenCollector<Property> collector = new RegexpChildrenCollector<>(new ArrayList<>(), "\\d+", false, 1, Property.class);
            if (multiSelectNode != null) {
                multiSelectNode.accept(collector);
                properties = collector.getCollectedChildren();
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error resolving properties from node.", e);
        }
        return properties;
    }

    /**
     * Retrieves the properties created by Magnolias MultiSelect.
     *
     * @param baseNode Parent node of the multi select node
     * @param nodeName Name of the multi select node
     * @return multi select properties
     */
    public static Collection<Property> retrieveMultiSelectProperties(Node baseNode, String nodeName) {
        Collection<Property> properties = emptyList();
        try {
            if (baseNode != null && baseNode.hasNode(nodeName)) {
                Node multiSelectNode = baseNode.getNode(nodeName);
                properties = retrieveMultiSelectProperties(multiSelectNode);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error retrieving multi select values.", e);
        }
        return properties;
    }

    /**
     * Retrieves the String values created by Magnolias MultiSelect.
     *
     * @param multiSelectNode jcr node
     * @return values
     * @see #retrieveMultiSelectProperties(javax.jcr.Node)
     */
    public static Collection<String> retrieveMultiSelectValues(Node multiSelectNode) {
        return retrieveMultiSelectProperties(multiSelectNode).stream()
            .map(TO_STRING_VALUE_DEFAULT_EMPTY)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the String values created by Magnolias MultiSelect.
     *
     * @param baseNode base node
     * @param nodeName node name
     * @return values
     * @see #retrieveMultiSelectValues(javax.jcr.Node)
     */
    public static Collection<String> retrieveMultiSelectValues(Node baseNode, String nodeName) {
        return retrieveMultiSelectProperties(baseNode, nodeName).stream()
            .map(TO_STRING_VALUE_DEFAULT_EMPTY)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the ordered String values created by Magnolias MultiSelect.
     *
     * @param multiSelectNode jcr node
     * @return values
     * @see #retrieveMultiSelectProperties(javax.jcr.Node, String)
     */
    public static Collection<String> retrieveOrderedMultiSelectValues(Node multiSelectNode) {
        return retrieveMultiSelectProperties(multiSelectNode).stream()
            .sorted(PROPERTY_NAME_COMPARATOR)
            .map(TO_STRING_VALUE_DEFAULT_EMPTY)
            .collect(Collectors.toList());

    }

    /**
     * Retrieves the ordered String values created by Magnolias MultiSelect.
     *
     * @param baseNode base node
     * @param nodeName node name
     * @return values
     * @see #retrieveMultiSelectProperties(javax.jcr.Node, String)
     */
    public static Collection<String> retrieveOrderedMultiSelectValues(Node baseNode, String nodeName) {
        return retrieveMultiSelectProperties(baseNode, nodeName).stream()
            .sorted(PROPERTY_NAME_COMPARATOR)
            .map(TO_STRING_VALUE_DEFAULT_EMPTY)
            .collect(Collectors.toList());
    }

    public static boolean exists(Property p) {
        boolean result = p != null;
        // Magnolia likes to hide null in wrappers:
        if (p instanceof DelegatePropertyWrapper) {
            // DelegatePropertyWrapper does not allow to check the wrapped Property for NULL but toString() returns an empty String.
            // better just catch a NullPointerException?
            result = isNotEmpty(p.toString());
        }
        return result;
    }

    private PropertyUtils() {
    }
}
