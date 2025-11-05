package de.ibmix.magkit.ui.dialogs.fields;

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
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.editor.LocaleContext;
import info.magnolia.ui.editor.MultiFormDefinition;
import info.magnolia.ui.editor.MultiFormView;
import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * View implementation enforcing a maximum number of child components for multi-form editors.
 * <p>
 * Enhances the standard {@link MultiFormView} by disabling the add component button once a configured limit is
 * reached and optionally showing an i18n notification. The limit is derived from the associated
 * {@link SpecificMultiFieldDefinition} / {@link SpecificMultiValueFieldDefinition}.
 * </p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Automatic enable/disable of add button based on component count.</li>
 *   <li>I18n notification when max reached (configurable via translator key).</li>
 *   <li>Attach/detach listeners to keep state consistent when components are dynamically added or removed.</li>
 * </ul>
 *
 * <p>Usage preconditions: Definition must be an instance implementing {@link SpecificMultiDefinition}. The associated
 * layout component must be a {@link VerticalLayout} produced by {@link #asVaadinComponent()}.</p>
 * <p>Thread-safety: Not thread-safe; use within Vaadin UI thread only.</p>
 *
 * @param <T> item type handled by the datasource/editor
 * @author payam.tabrizi
 * @since 2021-02-23
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

    /**
     * Apply layout and initialize max constraints and listeners.
     */
    @Override
    public void layout() {
        super.layout();
        if (_definition != null) {
            initMaxComponents();
            initContent();
        }
    }

    /**
     * Initialize maximum component count from definition or fallback.
     */
    void initMaxComponents() {
        _maxComponents = ((SpecificMultiFieldDefinition) _definition).getMaxComponents();
        if (_maxComponents == null) {
            _maxComponents = DEFAULT_MAX;
        }
    }

    /**
     * Initialize root layout and attach/detach listeners for dynamic component management.
     */
    void initContent() {
        _rootLayout = (VerticalLayout) asVaadinComponent();
        int componentCount = _rootLayout.getComponentCount();
        _rootLayout.addComponentAttachListener(this);
        if (componentCount > 1) {
            _rootLayout.addComponentDetachListener(this);
        }
        _rootLayout.addAttachListener(this);
    }

    /**
     * UI attach event: ensures add button state (enabled if under limit).
     * @param event attach event
     */
    @Override
    public void attach(final ClientConnector.AttachEvent event) {
        addButtonDisabled(false);
    }

    /**
     * UI detach event: re-evaluates enabling of add button for later re-attach cycles.
     * @param event detach event
     */
    @Override
    public void detach(final ClientConnector.DetachEvent event) {
        addButtonEnabled();
    }

    /**
     * Child component attached: potential disable when limit exceeded (with notification).
     * @param event component attach event
     */
    @Override
    public void componentAttachedToContainer(final HasComponents.ComponentAttachEvent event) {
        addButtonDisabled(true);
    }

    /**
     * Child component detached: try enabling add button again.
     * @param event component detach event
     */
    @Override
    public void componentDetachedFromContainer(final HasComponents.ComponentDetachEvent event) {
        addButtonEnabled();
    }

    /**
     * Enable add button if current number of components is within limit.
     */
    void addButtonEnabled() {
        Component component = _rootLayout.getComponent(_rootLayout.getComponentCount() - 1);
        if (_rootLayout.getComponentCount() <= _maxComponents) {
            component.setEnabled(true);
        }
    }

    /**
     * Disable add button when limit exceeded, optionally showing notification.
     * @param showNotification whether to show i18n notification about limit reached
     */
    void addButtonDisabled(boolean showNotification) {
        Component component = _rootLayout.getComponent(_rootLayout.getComponentCount() - 1);
        if (_rootLayout.getComponentCount() > _maxComponents) {
            component.setEnabled(false);
            if (showNotification) {
                String message = _i18n.translate("specificMultiField.maxNumber.reached", _maxComponents);
                if (isNotEmpty(message) && UI.getCurrent() != null) {
                    Notification.show(message);
                }
            }
        }
    }

}
