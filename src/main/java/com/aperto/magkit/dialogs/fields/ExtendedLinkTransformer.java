package com.aperto.magkit.dialogs.fields;

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


import com.aperto.magkit.utils.ExtendedLinkFieldHelper;
import com.aperto.magkit.utils.NodeUtils;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.ObjectProperty;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;

import javax.inject.Inject;
import javax.jcr.Node;

import static com.aperto.magkit.utils.ExtendedLinkFieldHelper.SUFFIX_ANCHOR;
import static com.aperto.magkit.utils.ExtendedLinkFieldHelper.SUFFIX_QUERY;
import static com.aperto.magkit.utils.ExtendedLinkFieldHelper.SUFFIX_SELECTOR;
import static com.aperto.magkit.utils.LinkTool.isExternalLink;
import static com.aperto.magkit.utils.LinkTool.isPath;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * The Transformer splits the value into single components and stores them in distinct properties on write operation. It merges the distinct property values on read operation into a single value.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 03.06.2015
 */
public class ExtendedLinkTransformer extends BasicTransformer<String> {


    private ExtendedLinkFieldHelper _extendedLinkFieldHelper;

    public ExtendedLinkTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<String> type, I18NAuthoringSupport i18nAuthoringSupport) {
        super(relatedFormItem, definition, type, i18nAuthoringSupport);
    }

    @Override
    public void writeToItem(final String newValue) {
        Property<String> property = relatedFormItem.getItemProperty(definePropertyName());
        if (isNotBlank(newValue)) {

            if (property == null) {
                property = new ObjectProperty<>(EMPTY);
                relatedFormItem.addItemProperty(definePropertyName(), property);
            }

            if (!isExternalLink(newValue)) {

                final String path = _extendedLinkFieldHelper.getBase(newValue);
                final String anchor = _extendedLinkFieldHelper.getAnchor(newValue);
                final String query = _extendedLinkFieldHelper.getQuery(newValue);
                final String selector = _extendedLinkFieldHelper.getSelectors(newValue);

                setPropertyValue(definePropertyName() + SUFFIX_ANCHOR, anchor);
                setPropertyValue(definePropertyName() + SUFFIX_QUERY, query);
                setPropertyValue(definePropertyName() + SUFFIX_SELECTOR, selector);

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
        String base = getPropertyValue(definePropertyName());
        final String anchor = getPropertyValue(definePropertyName() + SUFFIX_ANCHOR);
        final String query = getPropertyValue(definePropertyName() + SUFFIX_QUERY);
        final String selector = getPropertyValue(definePropertyName() + SUFFIX_SELECTOR);

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
        relatedFormItem.addItemProperty(id, isNotBlank(value) ? new ObjectProperty<>(value) : new ObjectProperty<>(EMPTY, String.class));
    }

    @Inject
    public void setExtendedLinkFieldHelper(final ExtendedLinkFieldHelper extendedLinkFieldHelper) {
        _extendedLinkFieldHelper = extendedLinkFieldHelper;
    }
}
