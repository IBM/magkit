package com.aperto.magkit.utils;

import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.sort;
import static org.apache.commons.collections15.CollectionUtils.collect;

/**
 * Util class for Property handling.
 *
 * @author frank.sommer
 * @since 09.10.12
 */
public final class PropertyUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtils.class);

    /**
     * Retrieves the properties created by Magnolias MultiSelect.
     * @param multiSelectNode node contains the properties
     * @return collection of properties, null if multiSelectNode is null
     */
    public static Collection<Property> retrieveMultiSelectProperties(Node multiSelectNode) {
        Collection<Property> properties = null;
        try {
            final RegexpChildrenCollector<Property> collector = new RegexpChildrenCollector<Property>(new ArrayList<Property>(), "\\d+", false, 1, Property.class);
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
     * Retrieves the String values created by Magnolias MultiSelect.
     * @see #retrieveMultiSelectProperties(javax.jcr.Node)
     */
    public static Collection<String> retrieveMultiSelectValues(Node multiSelectNode) {
        return collect(retrieveMultiSelectProperties(multiSelectNode), new Transformer<Property, String>() {
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
        });
    }

    /**
     * Retrieves the ordered String values created by Magnolias MultiSelect.
     * @see #retrieveMultiSelectValues(javax.jcr.Node)
     * @see #retrieveMultiSelectProperties(javax.jcr.Node)
     */
    public static Collection<String> retrieveOrderedMultiSelectValues(Node multiSelectNode) {
        List<String> values = new ArrayList<String>();
        values.addAll(retrieveMultiSelectValues(multiSelectNode));
        sort(values);
        return values;
    }

    private PropertyUtils() {
    }
}
