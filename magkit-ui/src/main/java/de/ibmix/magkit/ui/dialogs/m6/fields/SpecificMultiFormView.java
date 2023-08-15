package de.ibmix.magkit.ui.dialogs.m6.fields;

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

import com.vaadin.server.ClientConnector;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.editor.LocaleContext;
import info.magnolia.ui.editor.MultiFormDefinition;
import info.magnolia.ui.editor.MultiFormView;

import javax.inject.Inject;

/**
 * Editor view which hosts a number of similar child editors.
 *
 * @param <T> item type.
 * @author payam.tabrizi
 * @since 23.02.21
 */
public class SpecificMultiFormView<T> extends MultiFormView<T> implements ClientConnector.AttachListener, ClientConnector.DetachListener, HasComponents.ComponentAttachListener, HasComponents.ComponentDetachListener {

    private static final long DEFAULT_MAX = 3;

    private final MultiFormDefinition<T> _definition;
    private final SimpleTranslator _i18n;

    private VerticalLayout _rootLayout;
    private Long _maxComponents;

    @Inject
    public SpecificMultiFormView(MultiFormDefinition<T> definition, SimpleTranslator i18n, LocaleContext localeContext, Datasource<T> datasource) {
        super(definition, i18n, localeContext, datasource);
        _definition = definition;
        _i18n = i18n;
    }

    @Override
    public void layout() {
        super.layout();
        if (_definition != null) {
            initMaxComponents();
            initContent();
        }
    }

    private void initMaxComponents() {
        _maxComponents = ((SpecificMultiFieldDefinition) _definition).getMaxComponents();
        if (_maxComponents == null) {
            _maxComponents = DEFAULT_MAX;
        }
    }

    private void initContent() {
        _rootLayout = (VerticalLayout) asVaadinComponent();
        int componentCount = _rootLayout.getComponentCount();
        _rootLayout.addComponentAttachListener(this);
        if (componentCount > 1) {
            _rootLayout.addComponentDetachListener(this);
        }
        _rootLayout.addAttachListener(this);
    }

    @Override
    public void attach(final ClientConnector.AttachEvent event) {
        addButtonDisabled(false);
    }

    @Override
    public void detach(final ClientConnector.DetachEvent event) {
        addButtonEnabled();
    }

    @Override
    public void componentAttachedToContainer(final HasComponents.ComponentAttachEvent event) {
        addButtonDisabled(true);
    }

    @Override
    public void componentDetachedFromContainer(final HasComponents.ComponentDetachEvent event) {
        addButtonEnabled();
    }

    private void addButtonEnabled() {
        Component component = _rootLayout.getComponent(_rootLayout.getComponentCount() - 1);
        if (_rootLayout.getComponentCount() <= _maxComponents) {
            component.setEnabled(true);
        }
    }

    private void addButtonDisabled(boolean showNotification) {
        Component component = _rootLayout.getComponent(_rootLayout.getComponentCount() - 1);
        if (_rootLayout.getComponentCount() > _maxComponents) {
            component.setEnabled(false);
            if (showNotification) {
                Notification.show(_i18n.translate("specificMultiField.maxNumber.reached", _maxComponents));
            }
        }
    }

}
