package de.ibmix.magkit.ui.dialogs.fields;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2025 IBM iX
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
import com.vaadin.ui.Button;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.i18nsystem.SimpleTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lightweight unit tests for {@link SpecificMultiFormView} exercising button enable/disable logic and event handlers
 * without invoking Magnolia's UI initialization (constructor side effects) by using Objenesis for instantiation.
 *
 * Covered cases:
 * <ul>
 *   <li>Enable when component count is within max.</li>
 *   <li>Disable with notification when count exceeds max.</li>
 *   <li>Disable without notification when count exceeds max.</li>
 *   <li>Disable call under limit leaves button enabled.</li>
 *   <li>Attach event uses disable path (showNotification=false).</li>
 *   <li>Detach event re-enables when back under limit.</li>
 *   <li>Component attach event disables with notification.</li>
 *   <li>Component detach event enables when count reduced.</li>
 *   <li>initMaxComponents fallback to default when null configured.</li>
 *   <li>initMaxComponents retains custom configured value.</li>
 *   <li>Calling addButtonEnabled when over limit does not re-enable.</li>
 * </ul>
 *
 * Testing approach: Instantiate the class without constructor execution, then inject private state (_rootLayout,
 * _maxComponents, _i18n, _definition) via reflection to isolate and test logic pathways.
 *
 * @author wolf.bubenik
 * @since 2025-10-31
 */
public class SpecificMultiFormViewTest {

    private SpecificMultiFormView<Object> _view;
    private SimpleTranslator _translator;
    private VerticalLayout _root;
    private Button _addButton;

    @BeforeEach
    public void setUp() throws Exception {
        @SuppressWarnings("unchecked") SpecificMultiFormView<Object> instance = new ObjenesisStd().newInstance(SpecificMultiFormView.class);
        _view = instance;
        _translator = mock(SimpleTranslator.class);
        _root = new VerticalLayout();
        _addButton = new Button("add");
        _root.addComponent(_addButton);
        inject("_rootLayout", _root);
        inject("_maxComponents", 3L);
        inject("_i18n", _translator);
    }

    /**
     * Verifies enabling logic when count is at or below max.
     */
    @Test
    public void testEnableWithinLimit() {
        _view.addButtonEnabled();
        assertTrue(_addButton.isEnabled());
        addEntry(new Label("c1"));
        addEntry(new Label("c2"));
        _view.addButtonEnabled();
        assertTrue(_addButton.isEnabled());
    }

    /**
     * Verifies disabling with notification and translation invocation when count exceeds max.
     */
    @Test
    public void testDisableWithNotification() {
        when(_translator.translate("specificMultiField.maxNumber.reached", 3L)).thenReturn("reached");
        exceedLimit();
        _view.addButtonDisabled(true);
        assertFalse(_addButton.isEnabled());
        verify(_translator, times(1)).translate("specificMultiField.maxNumber.reached", 3L);
    }

    /**
     * Verifies disabling without notification does not trigger translation.
     */
    @Test
    public void testDisableWithoutNotification() {
        exceedLimit();
        _view.addButtonDisabled(false);
        assertFalse(_addButton.isEnabled());
        verify(_translator, never()).translate("specificMultiField.maxNumber.reached", 3L);
    }

    /**
     * Verifies calling disable under limit keeps button enabled and does not translate.
     */
    @Test
    public void testDisableUnderLimitNoEffect() {
        _view.addButtonDisabled(true);
        assertTrue(_addButton.isEnabled());
        verify(_translator, never()).translate("specificMultiField.maxNumber.reached", 3L);
    }

    /**
     * Verifies attach event applies disable path without notification.
     */
    @Test
    public void testAttachEventDisablesWithoutNotification() {
        exceedLimit();
        ClientConnector.AttachEvent event = mock(ClientConnector.AttachEvent.class);
        _view.attach(event);
        assertFalse(_addButton.isEnabled());
        verify(_translator, never()).translate("specificMultiField.maxNumber.reached", 3L);
    }

    /**
     * Verifies detach event re-enables when back under or equal max.
     */
    @Test
    public void testDetachEventReEnables() {
        exceedLimit();
        _view.addButtonDisabled(false);
        assertFalse(_addButton.isEnabled());
        // Reduce count to max
        removeNonAddComponent();
        ClientConnector.DetachEvent event = mock(ClientConnector.DetachEvent.class);
        _view.detach(event);
        assertTrue(_addButton.isEnabled());
    }

    /**
     * Verifies component attach event disables with notification.
     */
    @Test
    public void testComponentAttachEventDisablesWithNotification() {
        when(_translator.translate("specificMultiField.maxNumber.reached", 3L)).thenReturn("reached");
        exceedLimit();
        HasComponents.ComponentAttachEvent event = mock(HasComponents.ComponentAttachEvent.class);
        _view.componentAttachedToContainer(event);
        assertFalse(_addButton.isEnabled());
        verify(_translator, times(1)).translate("specificMultiField.maxNumber.reached", 3L);
    }

    /**
     * Verifies component detach event enables after reduction.
     */
    @Test
    public void testComponentDetachEventEnablesAfterReduction() {
        exceedLimit();
        _view.addButtonDisabled(false);
        assertFalse(_addButton.isEnabled());
        removeNonAddComponent();
        HasComponents.ComponentDetachEvent event = mock(HasComponents.ComponentDetachEvent.class);
        _view.componentDetachedFromContainer(event);
        assertTrue(_addButton.isEnabled());
    }

    /**
     * Verifies calling addButtonEnabled when over limit keeps button disabled.
     */
    @Test
    public void testEnableOverLimitNoChange() {
        exceedLimit();
        _view.addButtonDisabled(false);
        assertFalse(_addButton.isEnabled());
        _view.addButtonEnabled();
        assertFalse(_addButton.isEnabled());
    }

    /**
     * Verifies initMaxComponents fallback to default when definition returns null.
     */
    @Test
    public void testInitMaxComponentsFallback() throws Exception {
        SpecificMultiFieldDefinition def = new SpecificMultiFieldDefinition();
        def.setMaxComponents(null);
        inject("_definition", def);
        _view.initMaxComponents();
        assertEquals(3L, readMaxComponents());
        // Change current value to verify fallback replacement
        inject("_maxComponents", 99L);
        _view.initMaxComponents();
        assertEquals(3L, readMaxComponents());
    }

    /**
     * Verifies initMaxComponents uses custom configured value.
     */
    @Test
    public void testInitMaxComponentsCustomValue() throws Exception {
        SpecificMultiFieldDefinition def = new SpecificMultiFieldDefinition();
        def.setMaxComponents(5L);
        inject("_definition", def);
        inject("_maxComponents", 1L);
        _view.initMaxComponents();
        assertEquals(5L, readMaxComponents());
    }

    private void exceedLimit() {
        addEntry(new Label("c1"));
        addEntry(new Label("c2"));
        addEntry(new Label("c3"));
    }

    private void addEntry(Label component) {
        _root.removeComponent(_addButton);
        _root.addComponent(component);
        _root.addComponent(_addButton);
    }

    private void removeNonAddComponent() {
        if (_root.getComponentCount() > 1) {
            _root.removeComponent(_root.getComponent(0));
        }
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field f = SpecificMultiFormView.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(_view, value);
    }

    private long readMaxComponents() throws Exception {
        Field f = SpecificMultiFormView.class.getDeclaredField("_maxComponents");
        f.setAccessible(true);
        return (long) f.get(_view);
    }
}
