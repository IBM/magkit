package com.aperto.magkit.dialogs.fields;


import static com.aperto.magkit.utils.ExtendedLinkFieldHelper.SUFFIX_ANCHOR;
import static com.aperto.magkit.utils.ExtendedLinkFieldHelper.SUFFIX_QUERY;
import static com.aperto.magkit.utils.ExtendedLinkFieldHelper.SUFFIX_SELECTOR;
import static com.aperto.magkit.utils.LinkTool.isExternalLink;
import static com.aperto.magkit.utils.LinkTool.isPath;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import javax.inject.Inject;
import javax.jcr.Node;

import com.aperto.magkit.utils.ExtendedLinkFieldHelper;
import com.aperto.magkit.utils.NodeUtils;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;

/**
 * The Transformer splits the value into single components and stores them in distinct properties on write operation. It merges the distinct property values on read operation into a single value.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 03.06.2015
 */
public class ExtendedLinkTransformer extends BasicTransformer<String> {

    private String _baseName;

    private ExtendedLinkFieldHelper _extendedLinkFieldHelper;

    public ExtendedLinkTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<String> type) {
        super(relatedFormItem, definition, type);
        _baseName = definition.getName();
    }

    @Override
    public void writeToItem(final String newValue) {
        Property<String> property = relatedFormItem.getItemProperty(_baseName);
        if (isNotBlank(newValue)) {

            if (property == null) {
                property = new DefaultProperty<String>(String.class, null);
                relatedFormItem.addItemProperty(_baseName, property);
            }

            if (!isExternalLink(newValue)) {

                final String path = _extendedLinkFieldHelper.getBase(newValue);
                final String anchor = _extendedLinkFieldHelper.getAnchor(newValue);
                final String query = _extendedLinkFieldHelper.getQuery(newValue);
                final String selector = _extendedLinkFieldHelper.getSelectors(newValue);

                setPropertyValue(_baseName + SUFFIX_ANCHOR, anchor);
                setPropertyValue(_baseName + SUFFIX_QUERY, query);
                setPropertyValue(_baseName + SUFFIX_SELECTOR, selector);

                if (isNotBlank(path)) {
                    property.setValue(path);
                } else {
                    property.setValue(null);
                }
            } else {
                property.setValue(newValue);
            }
        } else if (property != null) {
            property.setValue(null);
        }
    }

    @Override
    public String readFromItem() {
        String result;
        String base = getPropertyValue(_baseName);
        final String anchor = getPropertyValue(_baseName + SUFFIX_ANCHOR);
        final String query = getPropertyValue(_baseName + SUFFIX_QUERY);
        final String selector = getPropertyValue(_baseName + SUFFIX_SELECTOR);

        if (!isExternalLink(base) && isNotBlank(base)) {
            if (isPath(base)) {
                Node targetNode = NodeUtils.getNodeByIdentifier(RepositoryConstants.WEBSITE, base);
                if (targetNode != null) {
                    base = NodeUtil.getNodeIdentifierIfPossible(targetNode);
                }
            }
            result = _extendedLinkFieldHelper.mergeComponents(base, selector, query, anchor);
        } else {
            result = base;
        }

        return result;
    }

    private String getPropertyValue(final String id) {
        final Property property = relatedFormItem.getItemProperty(id);
        return property != null && property.getValue() != null ? String.valueOf(property.getValue()) : null;
    }

    private void setPropertyValue(final String id, final String value) {
        relatedFormItem.addItemProperty(id, isNotBlank(value) ? new DefaultProperty<String>(value) : new DefaultProperty<String>(null));
    }

    @Inject
    public void setExtendedLinkFieldHelper(final ExtendedLinkFieldHelper extendedLinkFieldHelper) {
        _extendedLinkFieldHelper = extendedLinkFieldHelper;
    }
}
