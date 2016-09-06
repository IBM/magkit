package com.aperto.magkit.utils;

import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static info.magnolia.jcr.util.PropertyUtil.getValuesStringList;
import static java.util.Collections.emptyList;
import static java.util.Collections.sort;
import static org.apache.commons.collections15.CollectionUtils.collect;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Util class for Property handling.
 *
 * @author frank.sommer
 * @since 09.10.12
 */
public final class PropertyUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtils.class);

    /**
     * Add missing util method for retrieving multi value property values.
     *
     * @param node    containing the multivalue
     * @param relPath relative path to the multi value property
     * @return string values as collection, if not available empty collection and if single value the collection of size one.
     * @see info.magnolia.jcr.util.PropertyUtil
     */
    public static Collection<String> getStringValues(final Node node, final String relPath) {
        Collection<String> values = new ArrayList<>();

        try {
            if (node != null && isNotEmpty(relPath) && node.hasProperty(relPath)) {
                Property property = node.getProperty(relPath);
                if (property.isMultiple()) {
                    values = getValuesStringList(property.getValues());
                } else {
                    values.add(property.getString());
                }
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error retrieving property {}.", relPath, e);
        }

        return values;
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
            final RegexpChildrenCollector<Property> collector = new RegexpChildrenCollector<>(new ArrayList<Property>(), "\\d+", false, 1, Property.class);
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
        return collect(retrieveMultiSelectProperties(multiSelectNode), new PropertyStringTransformer());
    }

    /**
     * Retrieves the String values created by Magnolias MultiSelect.
     *
     * @see #retrieveMultiSelectValues(javax.jcr.Node)
     */
    public static Collection<String> retrieveMultiSelectValues(Node baseNode, String nodeName) {
        return collect(retrieveMultiSelectProperties(baseNode, nodeName), new PropertyStringTransformer());
    }

    /**
     * Retrieves the ordered String values created by Magnolias MultiSelect.
     *
     * @see #retrieveMultiSelectProperties(javax.jcr.Node, String)
     */
    public static Collection<String> retrieveOrderedMultiSelectValues(Node multiSelectNode) {
        List<Property> values = new ArrayList<>();
        values.addAll(retrieveMultiSelectProperties(multiSelectNode));
        sort(values, new PropertyComparator());
        return collect(values, new PropertyStringTransformer());
    }

    /**
     * Retrieves the ordered String values created by Magnolias MultiSelect.
     *
     * @see #retrieveMultiSelectProperties(javax.jcr.Node, String)
     */
    public static Collection<String> retrieveOrderedMultiSelectValues(Node baseNode, String nodeName) {
        List<Property> values = new ArrayList<>();
        values.addAll(retrieveMultiSelectProperties(baseNode, nodeName));
        sort(values, new PropertyComparator());
        return collect(values, new PropertyStringTransformer());
    }

    /**
     * Get the {@link Long} value from a node.
     *
     * @param node         Node
     * @param propertyName Property name of the {@link Long} value.
     * @param defaultValue Default value.
     * @return value
     * @deprecated use {@link info.magnolia.jcr.util.PropertyUtil#getLong(Node, String, Long)} instead
     */
    @Deprecated
    public static Long getLong(Node node, String propertyName, Long defaultValue) {
        Long longValue = defaultValue;
        try {
            if (node != null && isNotEmpty(propertyName) && node.hasProperty(propertyName)) {
                Property property = node.getProperty(propertyName);
                longValue = property.getLong();
            }
        } catch (RepositoryException e) {
            LOGGER.info("Error message was {}", e.getLocalizedMessage());
        }
        return longValue;
    }

    private PropertyUtils() {
    }

    private static class PropertyStringTransformer implements Transformer<Property, String> {
        @Override
        public String transform(Property property) {
            String value = "";
            try {
                value = property.getString();
            } catch (RepositoryException e) {
                LOGGER.error("Error get string value from property.", e);
            }
            return value;
        }
    }

    private static class PropertyComparator implements Comparator<Property> {
        @Override
        public int compare(Property p1, Property p2) {
            int compareValue = 0;
            try {
                compareValue = p1.getName().compareTo(p2.getName());
            } catch (RepositoryException e) {
                LOGGER.error("Error comparing by name of properties.", e);
            }
            return compareValue;
        }
    }
}
