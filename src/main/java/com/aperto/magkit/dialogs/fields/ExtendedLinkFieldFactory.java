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
import com.aperto.magkit.utils.LinkTool;
import com.vaadin.v7.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.Field;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.LinkField;
import info.magnolia.ui.form.field.factory.LinkFieldFactory;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collection;

/**
 * The factory creates a {@link LinkField}. The callback mechanism is overwritten to allow additional link components like fragments, queries and selectors.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 03.06.2015
 */
public class ExtendedLinkFieldFactory extends LinkFieldFactory<ExtendedLinkFieldDefinition> {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExtendedLinkFieldFactory.class);

    private final AppController _appController;
    private final UiContext _uiContext;

    private final ExtendedLinkFieldHelper _extendedLinkFieldHelper = new ExtendedLinkFieldHelper();
    private LinkField _linkField;

    @Inject
    public ExtendedLinkFieldFactory(final ExtendedLinkFieldDefinition definition, final Item relatedFieldItem, final UiContext uiContext,
                                    final I18NAuthoringSupport i18nAuthoringSupport, final AppController appController, final ComponentProvider componentProvider) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport, appController, componentProvider);
        _appController = appController;
        _uiContext = uiContext;
    }

    @Override
    protected Field<String> createFieldComponent() {
        _linkField = (LinkField) super.createFieldComponent();
        // Change the callback listener on the select button
        Collection<?> listeners = _linkField.getSelectButton().getListeners(Button.ClickEvent.class);
        for (Object listener : listeners) {
            if (listener instanceof Button.ClickListener) {
                _linkField.getSelectButton().removeClickListener((Button.ClickListener) listener);
            }
        }
        _linkField.getSelectButton().addClickListener(createButtonClickListener());
        return _linkField;
    }

    private Button.ClickListener createButtonClickListener() {
        return new ExtendedLinkFieldClickListener();
    }

    protected ChooseDialogCallback createChooseDialogCallback() {
        return new ExtendedLinkFieldChooseDialogCallback();
    }

    private class ExtendedLinkFieldChooseDialogCallback implements ChooseDialogCallback {
        /**
         * Returns the components for internal uri.
         *
         * @param value old field value
         * @return the uri components other than path
         */
        private String unstripUriExtension(final String value) {
            String unStripped = StringUtils.EMPTY;
            if (!LinkTool.isExternalLink(value) && StringUtils.isNotBlank(value)) {
                unStripped = StringUtils.removeStart(value, _extendedLinkFieldHelper.getBase(value));
            }
            return unStripped;
        }

        @Override
        public void onCancel() {
            _linkField.getSelectButton().setEnabled(true);
        }

        @Override
        public void onItemChosen(final String actionName, final Object chosenValue) {
            String propertyName = getFieldDefinition().getTargetPropertyToPopulate();
            String newValue = null;
            if (chosenValue instanceof JcrItemId) {
                try {
                    javax.jcr.Item jcrItem = JcrItemUtil.getJcrItem((JcrItemId) chosenValue);
                    if (jcrItem.isNode()) {
                        final Node selected = (Node) jcrItem;
                        boolean isPropertyExisting = StringUtils.isNotBlank(propertyName) && selected.hasProperty(propertyName);
                        newValue = (isPropertyExisting ? selected.getProperty(propertyName).getString() : selected.getPath()) + unstripUriExtension(_linkField.getTextField().getValue());
                    }
                } catch (RepositoryException e) {
                    LOGGER.error("Not able to access the configured property. Value will not be set.", e);
                }
            }
            _linkField.setValue(newValue);
            _linkField.getSelectButton().setEnabled(true);
        }
    }

    private class ExtendedLinkFieldClickListener implements Button.ClickListener {
        @Override
        public void buttonClick(final Button.ClickEvent event) {
            ChooseDialogCallback callback = createChooseDialogCallback();
            ExtendedLinkFieldDefinition def = getFieldDefinition();
            String value = _linkField.getValue();
            if (StringUtils.isNotBlank(def.getTargetTreeRootPath())) {
                _appController.openChooseDialog(def.getAppName(), _uiContext, def.getTargetTreeRootPath(), stripUriExtension(value), callback);
            } else {
                _appController.openChooseDialog(def.getAppName(), _uiContext, stripUriExtension(value), callback);
            }
        }

        /**
         * Removes components other than path from an internal uri.
         *
         * @param value the uri or identifier with additional components
         * @return the base path or identifier
         */
        private String stripUriExtension(final String value) {
            String stripped = StringUtils.EMPTY;
            if (!LinkTool.isExternalLink(value) && StringUtils.isNotBlank(value)) {
                stripped = _extendedLinkFieldHelper.getBase(value);
            }
            return stripped;
        }
    }
}
