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
 * Utility class for convenient, null-safe and defensive access to JCR {@link Property} instances and their typed values.
 * <p>
 * Main functionalities and key features:
 * <ul>
 *   <li>Safe single and multi value retrieval (String, Calendar, Long, Double, Boolean, Binary).</li>
 *   <li>Fallback handling for nullable values (overloads with fallback parameter).</li>
 *   <li>Automatic HTML escaping preservation for Magnolia {@link HTMLEscapingPropertyWrapper} by wrapping returned {@link Value}s.</li>
 *   <li>Helpers for Magnolia MultiSelect properties (collecting and ordering).</li>
 *   <li>Graceful error handling: internal {@link RepositoryException}s are caught and logged; methods never throw.</li>
 * </ul>
 * Usage preconditions:
 * <ul>
 *   <li>Caller provides a valid JCR {@link Node} obtained from an active session; methods accept {@code null} nodes and properties.</li>
 *   <li>Property names / relative paths should follow JCR naming conventions.</li>
 * </ul>
 * Null and error handling:
 * <ul>
 *   <li>Methods returning single values yield {@code null} (or provided fallback) when property/value is not accessible.</li>
 *   <li>Collection-returning methods return an empty list when nothing is available.</li>
 *   <li>Multi-value access returns all values; single-value properties are exposed as a singleton list.</li>
 * </ul>
 * Side effects: This class performs only read operations. No repository mutations are executed.
 * Thread-safety: Stateless – all methods are thread-safe.
 * Example:
 * <pre>
 *   Node article = ...;
 *   String title = PropertyUtils.getStringValue(article, "title", "Untitled");
 *   List&lt;String&gt; tags = PropertyUtils.getStringValues(article, "tags");
 *   Collection&lt;String&gt; ordered = PropertyUtils.retrieveOrderedMultiSelectValues(article, "categories");
 * </pre>
 * Important details:
 * <ul>
 *   <li>HTML escaping: When Magnolia wraps a property with {@link HTMLEscapingPropertyWrapper}, returned {@link Value}s are wrapped to keep escaping semantics.</li>
 *   <li>Delegate wrappers: {@link DelegatePropertyWrapper} may mask {@code null}; {@link #exists(Property)} performs an additional emptiness check.</li>
 * </ul>
 *
 * @author frank.sommer
 * @since 2012-10-09
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
     * Safely retrieves a property by relative path without pre-checking its existence.
     * Exceptions are swallowed and logged to avoid double repository access.
     *
     * @param node    node to read from (may be {@code null})
     * @param relPath relative path to the property (may be {@code null} or empty)
     * @return the property or {@code null} if unavailable
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

    /**
     * Retrieves all properties of the given node.
     *
     * @param node source node (may be {@code null})
     * @return iterator over properties or {@code null} if node is {@code null} or access failed
     */
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

    /**
     * Retrieves properties matching a name pattern (JCR glob-style) from the node.
     *
     * @param node        source node (may be {@code null})
     * @param namePattern glob-like pattern (non-null)
     * @return iterator over matching properties or {@code null} if node is {@code null} or access failed
     */
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

    /**
     * Retrieves properties matching one of several glob name patterns.
     *
     * @param node      source node (may be {@code null})
     * @param nameGlobs array of glob patterns (non-null)
     * @return iterator over matching properties or {@code null} if node is {@code null} or access failed
     */
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
     * Returns all values of a property as a list. For single-valued properties a singleton list is returned.
     * Preserves Magnolia HTML escaping when the property is a {@link HTMLEscapingPropertyWrapper}.
     *
     * @param input property (may be {@code null})
     * @return list of values, never {@code null}
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
        if (input != null && exists(input)) {
            try {
                result = input.isMultiple() ? input.getValues() : new Value[]{input.getValue()};
            } catch (RepositoryException e) {
                LOGGER.debug("Cannot access values of property ", e);
            }
        }
        return result;
    }

    /**
     * Retrieves the first value of a property (for multi-valued properties) or the single value.
     * Preserves Magnolia HTML escaping when applicable.
     *
     * @param input property (may be {@code null})
     * @return first value or {@code null} if unavailable
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
        if (input != null && exists(input)) {
            try {
                result = input.isMultiple() ? ArrayUtils.get(input.getValues(), 0) : input.getValue();
            } catch (RepositoryException e) {
                LOGGER.debug("Cannot access value of property ", e);
            }
        }
        return result;
    }

    /**
     * Retrieves string values of a potentially multi-valued property via node path.
     *
     * @param node    node containing the property (may be {@code null})
     * @param relPath relative property path
     * @return list of string representations; empty if unavailable
     */
    public static List<String> getStringValues(final Node node, final String relPath) {
        Property property = getProperty(node, relPath);
        return getStringValues(property);
    }

    /**
     * Retrieves the string representation of a property's first value.
     *
     * @param property property (may be {@code null})
     * @return string value or {@code null}
     */
    public static String getStringValue(final Property property) {
        return getStringValue(property, null);
    }

    /**
     * Retrieves the string representation of a property's first value with fallback.
     *
     * @param property property (may be {@code null})
     * @param fallback value returned when inaccessible
     * @return string value or fallback
     */
    public static String getStringValue(final Property property, final String fallback) {
        return valueToString(getValue(property), fallback);
    }

    /**
     * Retrieves a string value of a property by path.
     *
     * @param node    node (may be {@code null})
     * @param relPath relative path
     * @return string value or {@code null}
     */
    public static String getStringValue(final Node node, final String relPath) {
        return getStringValue(node, relPath, null);
    }

    /**
     * Retrieves a string value of a property by path with fallback.
     *
     * @param node     node (may be {@code null})
     * @param relPath  relative path
     * @param fallback fallback value
     * @return string value or fallback
     */
    public static String getStringValue(final Node node, final String relPath, final String fallback) {
        return getStringValue(getProperty(node, relPath), fallback);
    }

    /**
     * Returns all string values of a property.
     *
     * @param property property (may be {@code null})
     * @return list of string values; empty if none
     */
    public static List<String> getStringValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToString)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a calendar value from the property.
     *
     * @param property property (may be {@code null})
     * @return calendar or {@code null}
     */
    public static Calendar getCalendarValue(final Property property) {
        return getCalendarValue(property, null);
    }

    /**
     * Retrieves a calendar value with fallback.
     *
     * @param property property (may be {@code null})
     * @param fallback fallback calendar value
     * @return calendar or fallback
     */
    public static Calendar getCalendarValue(final Property property, final Calendar fallback) {
        return valueToCalendar(getValue(property), fallback);
    }

    /**
     * Retrieves a calendar value by node and path.
     *
     * @param node    node (may be {@code null})
     * @param relPath relative path
     * @return calendar or {@code null}
     */
    public static Calendar getCalendarValue(final Node node, final String relPath) {
        return getCalendarValue(node, relPath, null);
    }

    /**
     * Retrieves a calendar value by node and path with fallback.
     *
     * @param node     node (may be {@code null})
     * @param relPath  relative path
     * @param fallback fallback value
     * @return calendar or fallback
     */
    public static Calendar getCalendarValue(final Node node, final String relPath, final Calendar fallback) {
        return getCalendarValue(getProperty(node, relPath), fallback);
    }

    /**
     * Returns all calendar values of a property.
     *
     * @param property property (may be {@code null})
     * @return list of calendar values; empty if none
     */
    public static List<Calendar> getCalendarValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToCalendar)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Returns all calendar values of a property by node path.
     *
     * @param node    node (may be {@code null})
     * @param relPath relative path
     * @return list of calendar values; empty if none
     */
    public static List<Calendar> getCalendarValues(final Node node, final String relPath) {
        return getCalendarValues(getProperty(node, relPath));
    }

    /**
     * Retrieves a long value from the property.
     *
     * @param property property (may be {@code null})
     * @return long value or {@code null}
     */
    public static Long getLongValue(final Property property) {
        return getLongValue(property, null);
    }

    /**
     * Retrieves a long value with fallback.
     *
     * @param property property (may be {@code null})
     * @param fallback fallback value
     * @return long value or fallback
     */
    public static Long getLongValue(final Property property, final Long fallback) {
        return valueToLong(getValue(property), fallback);
    }

    /**
     * Retrieves a long value by node path.
     *
     * @param node    node (may be {@code null})
     * @param relPath relative path
     * @return long value or {@code null}
     */
    public static Long getLongValue(final Node node, final String relPath) {
        return getLongValue(node, relPath, null);
    }

    /**
     * Retrieves a long value by node path with fallback.
     *
     * @param node     node (may be {@code null})
     * @param relPath  relative path
     * @param fallback fallback value
     * @return long value or fallback
     */
    public static Long getLongValue(final Node node, final String relPath, final Long fallback) {
        return getLongValue(getProperty(node, relPath), fallback);
    }

    /**
     * Returns all long values of a property.
     *
     * @param property property (may be {@code null})
     * @return list of long values; empty if none
     */
    public static List<Long> getLongValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToLong)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Returns all long values of a property by node path.
     *
     * @param node    node (may be {@code null})
     * @param relPath relative path
     * @return list of long values; empty if none
     */
    public static List<Long> getLongValues(final Node node, final String relPath) {
        return getLongValues(getProperty(node, relPath));
    }

    /**
     * Retrieves a double value from the property.
     *
     * @param property property (may be {@code null})
     * @return double value or {@code null}
     */
    public static Double getDoubleValue(final Property property) {
        return valueToDouble(getValue(property));
    }

    /**
     * Retrieves a double value with fallback.
     *
     * @param property property (may be {@code null})
     * @param fallback fallback value
     * @return double value or fallback
     */
    public static Double getDoubleValue(final Property property, final Double fallback) {
        return valueToDouble(getValue(property), fallback);
    }

    /**
     * Retrieves a double value by node path.
     *
     * @param node    node (may be {@code null})
     * @param relPath relative path
     * @return double value or {@code null}
     */
    public static Double getDoubleValue(final Node node, final String relPath) {
        return getDoubleValue(node, relPath, null);
    }

    /**
     * Retrieves a double value by node path with fallback.
     *
     * @param node     node (may be {@code null})
     * @param relPath  relative path
     * @param fallback fallback value
     * @return double value or fallback
     */
    public static Double getDoubleValue(final Node node, final String relPath, final Double fallback) {
        return getDoubleValue(getProperty(node, relPath), fallback);
    }

    /**
     * Returns all double values of a property.
     *
     * @param property property (may be {@code null})
     * @return list of double values; empty if none
     */
    public static List<Double> getDoubleValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToDouble)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Returns all double values of a property by node path.
     *
     * @param node    node (may be {@code null})
     * @param relPath relative path
     * @return list of double values; empty if none
     */
    public static List<Double> getDoubleValues(final Node node, final String relPath) {
        return getDoubleValues(getProperty(node, relPath));
    }

    /**
     * Retrieves a boolean value from the property.
     *
     * @param property property (may be {@code null})
     * @return boolean value or {@code null}
     */
    public static Boolean getBooleanValue(final Property property) {
        return getBooleanValue(property, null);
    }

    /**
     * Retrieves a boolean value with fallback.
     *
     * @param property property (may be {@code null})
     * @param fallback fallback value
     * @return boolean value or fallback
     */
    public static Boolean getBooleanValue(final Property property, final Boolean fallback) {
        return valueToBoolean(getValue(property), fallback);
    }

    /**
     * Retrieves a boolean value by node path.
     *
     * @param node    node (may be {@code null})
     * @param relPath relative path
     * @return boolean value or {@code null}
     */
    public static Boolean getBooleanValue(final Node node, final String relPath) {
        return getBooleanValue(node, relPath, null);
    }

    /**
     * Retrieves a boolean value by node path with fallback.
     *
     * @param node     node (may be {@code null})
     * @param relPath  relative path
     * @param fallback fallback value
     * @return boolean value or fallback
     */
    public static Boolean getBooleanValue(final Node node, final String relPath, final Boolean fallback) {
        return getBooleanValue(getProperty(node, relPath), fallback);
    }

    /**
     * Returns all boolean values of a property.
     *
     * @param property property (may be {@code null})
     * @return list of boolean values; empty if none
     */
    public static List<Boolean> getBooleanValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToBoolean)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Returns all boolean values of a property by node path.
     *
     * @param node    node (may be {@code null})
     * @param relPath relative path
     * @return list of boolean values; empty if none
     */
    public static List<Boolean> getBooleanValues(final Node node, final String relPath) {
        return getBooleanValues(getProperty(node, relPath));
    }

    /**
     * Retrieves a binary value from the property.
     *
     * @param property property (may be {@code null})
     * @return binary value or {@code null}
     */
    public static Binary getBinaryValue(final Property property) {
        return getBinaryValue(property, null);
    }

    /**
     * Retrieves a binary value with fallback.
     *
     * @param property property (may be {@code null})
     * @param fallback fallback value
     * @return binary value or fallback
     */
    public static Binary getBinaryValue(final Property property, final Binary fallback) {
        return valueToBinary(getValue(property), fallback);
    }

    /**
     * Retrieves a binary value by node path.
     *
     * @param node    node (may be {@code null})
     * @param relPath relative path
     * @return binary value or {@code null}
     */
    public static Binary getBinaryValue(final Node node, final String relPath) {
        return getBinaryValue(node, relPath, null);
    }

    /**
     * Retrieves a binary value by node path with fallback.
     *
     * @param node     node (may be {@code null})
     * @param relPath  relative path
     * @param fallback fallback value
     * @return binary value or fallback
     */
    public static Binary getBinaryValue(final Node node, final String relPath, final Binary fallback) {
        return getBinaryValue(getProperty(node, relPath), fallback);
    }

    /**
     * Returns all binary values of a property.
     *
     * @param property property (may be {@code null})
     * @return list of binary values; empty if none
     */
    public static List<Binary> getBinaryValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToBinary)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Returns all binary values of a property by node path.
     *
     * @param node    node (may be {@code null})
     * @param relPath relative path
     * @return list of binary values; empty if none
     */
    public static List<Binary> getBinaryValues(final Node node, final String relPath) {
        return getBinaryValues(getProperty(node, relPath));
    }

    /**
     * Retrieves the properties created by Magnolia MultiSelect under the given node.
     * Filtered by numeric property names ("\\d+").
     *
     * @param multiSelectNode node containing multi-select properties (may be {@code null})
     * @return collection of properties; empty if none
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
     * Retrieves Magnolia MultiSelect properties by parent node and child node name.
     *
     * @param baseNode parent node (may be {@code null})
     * @param nodeName multi select child node name
     * @return collection of properties; empty if none
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
     * Retrieves string values from Magnolia MultiSelect node.
     * Missing or empty properties produce empty strings.
     * <p>Public API method may be unused internally but provided for external consumers.</p>
     *
     * @param multiSelectNode node holding multi-select properties (may be {@code null})
     * @return collection of string values; never {@code null}
     */
    @SuppressWarnings("unused")
    public static Collection<String> retrieveMultiSelectValues(Node multiSelectNode) {
        return retrieveMultiSelectProperties(multiSelectNode).stream()
            .map(TO_STRING_VALUE_DEFAULT_EMPTY)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves string values from Magnolia MultiSelect by parent node and child name.
     * <p>Public API method may be unused internally but provided for external consumers.</p>
     *
     * @param baseNode parent node (may be {@code null})
     * @param nodeName multi select child name
     * @return collection of string values; never {@code null}
     */
    @SuppressWarnings("unused")
    public static Collection<String> retrieveMultiSelectValues(Node baseNode, String nodeName) {
        return retrieveMultiSelectProperties(baseNode, nodeName).stream()
            .map(TO_STRING_VALUE_DEFAULT_EMPTY)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves ordered string values (ascending numeric-like property names) from a Magnolia MultiSelect node.
     *
     * @param multiSelectNode node holding multi-select properties (may be {@code null})
     * @return ordered collection of string values; never {@code null}
     */
    public static Collection<String> retrieveOrderedMultiSelectValues(Node multiSelectNode) {
        return retrieveMultiSelectProperties(multiSelectNode).stream()
            .sorted(PROPERTY_NAME_COMPARATOR)
            .map(TO_STRING_VALUE_DEFAULT_EMPTY)
            .collect(Collectors.toList());

    }

    /**
     * Retrieves ordered string values from Magnolia MultiSelect by parent node and child name.
     *
     * @param baseNode parent node (may be {@code null})
     * @param nodeName multi select child name
     * @return ordered collection of string values; never {@code null}
     */
    public static Collection<String> retrieveOrderedMultiSelectValues(Node baseNode, String nodeName) {
        return retrieveMultiSelectProperties(baseNode, nodeName).stream()
            .sorted(PROPERTY_NAME_COMPARATOR)
            .map(TO_STRING_VALUE_DEFAULT_EMPTY)
            .collect(Collectors.toList());
    }

    /**
     * Checks for existence of a property handling Magnolia delegate wrappers that may mask null.
     *
     * @param p property (may be {@code null})
     * @return {@code true} if property appears to exist
     */
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
