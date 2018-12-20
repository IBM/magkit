package com.aperto.magkit.utils;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static info.magnolia.jcr.util.PropertyUtil.getValuesStringList;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.collect;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Util class for Property handling.
 *
 * @author frank.sommer
 * @since 09.10.12
 */
public final class PropertyUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtils.class);

    private static final Transformer<Property, String> PROPERTY_TO_STRING = property -> {
        String value = null;
        try {
            value = property.getString();
        } catch (RepositoryException e) {
            LOGGER.warn("Error get string value from property.", e);
        }
        return StringUtils.defaultString(value);
    };

    private static final Comparator<Property> PROPERTY_NAME_COMPARATOR = (p1, p2) -> {
        int compareValue = 0;
        try {
            compareValue = p1.getName().compareTo(p2.getName());
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
     * Add missing util method for retrieving multi value property values.
     *
     * @param node    containing the multivalue
     * @param relPath relative path to the multi value property
     * @return string values as collection, if not available empty collection and if single value the collection of size one.
     * @see info.magnolia.jcr.util.PropertyUtil
     */
    public static Collection<String> getStringValues(final Node node, final String relPath) {
        Property property = getProperty(node, relPath);
        return getStringValues(property);
    }

    public static List<String> getStringValues(final Property property) {
        List<String> result = Collections.emptyList();
        if (property != null) {
            try {
                if (property.isMultiple()) {
                    // TODO: Here we bypass the HtmlEncodingWrapper of Magnolia properties!
                    result = getValuesStringList(property.getValues());
                } else {
                    result = new ArrayList<>(1);
                    result.add(property.getString());
                }
            } catch (RepositoryException e) {
                LOGGER.error("Error retrieving property String values.", e);
            }
        }
        return result;
    }

    /**
     * Retrieves the properties created by Magnolias MultiSelect.
     *
     * @param multiSelectNode node contains the properties
     * @return collection of properties, null if multiSelectNode is null
     */
    public static Collection<Property> retrieveMultiSelectProperties(Node multiSelectNode) {
        Collection<Property> properties = null;
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
            if (baseNode.hasNode(nodeName)) {
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
        return collect(retrieveMultiSelectProperties(multiSelectNode), PROPERTY_TO_STRING);
    }

    /**
     * Retrieves the String values created by Magnolias MultiSelect.
     *
     * @see #retrieveMultiSelectValues(javax.jcr.Node)
     */
    public static Collection<String> retrieveMultiSelectValues(Node baseNode, String nodeName) {
        return collect(retrieveMultiSelectProperties(baseNode, nodeName), PROPERTY_TO_STRING);
    }

    /**
     * Retrieves the ordered String values created by Magnolias MultiSelect.
     *
     * @see #retrieveMultiSelectProperties(javax.jcr.Node, String)
     */
    public static Collection<String> retrieveOrderedMultiSelectValues(Node multiSelectNode) {
        List<Property> values = new ArrayList<>();
        values.addAll(retrieveMultiSelectProperties(multiSelectNode));
        values.sort(PROPERTY_NAME_COMPARATOR);
        return collect(values, PROPERTY_TO_STRING);
    }

    /**
     * Retrieves the ordered String values created by Magnolias MultiSelect.
     *
     * @see #retrieveMultiSelectProperties(javax.jcr.Node, String)
     */
    public static Collection<String> retrieveOrderedMultiSelectValues(Node baseNode, String nodeName) {
        List<Property> values = new ArrayList<>();
        values.addAll(retrieveMultiSelectProperties(baseNode, nodeName));
        values.sort(PROPERTY_NAME_COMPARATOR);
        return collect(values, PROPERTY_TO_STRING);
    }

    private PropertyUtils() {
    }
}
