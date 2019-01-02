package com.aperto.magkit.utils;

import info.magnolia.jcr.wrapper.DelegatePropertyWrapper;
import info.magnolia.jcr.wrapper.HTMLEscapingContentDecorator;
import info.magnolia.jcr.wrapper.HTMLEscapingPropertyWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.aperto.magkit.utils.ValueUtils.valueToBinary;
import static com.aperto.magkit.utils.ValueUtils.valueToBoolean;
import static com.aperto.magkit.utils.ValueUtils.valueToCalendar;
import static com.aperto.magkit.utils.ValueUtils.valueToDouble;
import static com.aperto.magkit.utils.ValueUtils.valueToLong;
import static com.aperto.magkit.utils.ValueUtils.valueToString;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

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
     * @param node the node to read the Property from. May be NULL.
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

    /**
     * Getter for property values as List.
     * If property is single valued a List with this single value will be returned.
     * Value will be wrapped into a HtmlEscapingValueDecorator if the property was wrapped with a HTMLEscapingPropertyWrapper.
     *
     * @param input the property or null
     * @return a List<Value> with all values of this property, never null
     */
    public static List<Value> getValues(@Nullable final Property input) {
        List<Value> result = Collections.emptyList();
        if (exists(input)) {
            try {
                Value[] values = input.isMultiple() ? input.getValues() : new Value[]{input.getValue()};
                if (input instanceof HTMLEscapingPropertyWrapper) {
                    // We are bypassing the magnolia html encoding of nodes if we work on values instead of the node properties.
                    // Here we provide an HTML escaping Value wrapper to overcome this limitation.
                    HTMLEscapingContentDecorator decorator = ((HTMLEscapingPropertyWrapper) input).getContentDecorator();
                    for (int i = 0; i < values.length; i++) {
                        values[i] = new HtmlEscapingValueDecorator(values[i], decorator);
                    }
                }
                result = Arrays.asList(values);
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
        Value result = null;
        if (exists(input)) {
            try {
                result = input.isMultiple() ? input.getValues()[0] : input.getValue();
                if (input instanceof HTMLEscapingPropertyWrapper) {
                    // We are bypassing the magnolia html encoding of nodes if we work on values instead of the node properties.
                    // Here we provide an HTML escaping Value wrapper to overcome this limitation.
                    HTMLEscapingContentDecorator decorator = ((HTMLEscapingPropertyWrapper) input).getContentDecorator();
                    result = new HtmlEscapingValueDecorator(result, decorator);
                }
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
        return valueToString(getValue(property));
    }

    public static List<String> getStringValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToString)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static Calendar getCalendarValue(final Property property) {
        return valueToCalendar(getValue(property));
    }

    public static List<Calendar> getCalendarValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToCalendar)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static Long getLongValue(final Property property) {
        return valueToLong(getValue(property));
    }

    public static List<Long> getLongValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToLong)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static Double getDoubleValue(final Property property) {
        return valueToDouble(getValue(property));
    }

    public static List<Double> getDoubleValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToDouble)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static Boolean getBooleanValue(final Property property) {
        return valueToBoolean(getValue(property));
    }

    public static List<Boolean> getBooleanValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToBoolean)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static Binary getBinaryValue(final Property property) {
        return valueToBinary(getValue(property));
    }

    public static List<Binary> getBinaryValues(final Property property) {
        return getValues(property)
            .stream()
            .map(ValueUtils::valueToBinary)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
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
