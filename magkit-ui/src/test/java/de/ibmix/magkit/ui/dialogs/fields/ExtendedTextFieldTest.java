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

import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ExtendedTextField} remaining-length calculation and UI behavior.
 *
 * @author frank.sommer
 * @since 2021-02-17
 */
public class ExtendedTextFieldTest {

    /**
     * Verifies recommended length is used when max length is not set.
     */
    @Test
    public void testJustRecommendedLength() {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(-1, 10), null);
        assertEquals(10, extendedTextField.determineLabelMaxLength());
    }

    /**
     * Verifies max length is used when recommended length is not set.
     */
    @Test
    public void testJustMaxLength() {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(10, -1), null);
        assertEquals(10, extendedTextField.determineLabelMaxLength());
    }

    /**
     * Verifies the smaller of max and recommended length (max smaller) is taken.
     */
    @Test
    public void testMaxGtRecommendedLength() {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(10, 20), null);
        assertEquals(10, extendedTextField.determineLabelMaxLength());
    }

    /**
     * Verifies the smaller of max and recommended length (recommended smaller) is taken.
     */
    @Test
    public void testMaxLtRecommendedLength() {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(20, 10), null);
        assertEquals(10, extendedTextField.determineLabelMaxLength());
    }

    /**
     * Verifies when neither length is configured the result is -1.
     */
    @Test
    public void testNoLengthsConfigured() {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(-1, -1), null);
        assertEquals(-1, extendedTextField.determineLabelMaxLength());
    }

    /**
     * Verifies initContent creates label and calculates remaining length for initial value.
     */
    @Test
    public void testInitContentCreatesLabelWithRecommendedLength() {
        ExtendedTextFieldDefinition definition = createDefinition(-1, 10);
        TextField inner = new TextField();
        inner.setValue("abc");
        ExtendedTextField extendedTextField = new ExtendedTextField(definition, inner);
        Component root = extendedTextField.initContent();
        assertTrue(root instanceof VerticalLayout);
        assertEquals("7/10", extendedTextField.getRemainingLength().getValue());
        assertEquals(10, extendedTextField.determineLabelMaxLength());
        assertEquals(inner.getValue(), extendedTextField.getValue());
    }

    /**
     * Verifies no label is added when no length configuration exists.
     */
    @Test
    public void testInitContentWithoutLengthsOmitsLabel() {
        ExtendedTextFieldDefinition definition = createDefinition(-1, -1);
        TextField inner = new TextField();
        inner.setValue("abc");
        ExtendedTextField extendedTextField = new ExtendedTextField(definition, inner);
        Component root = extendedTextField.initContent();
        assertTrue(root instanceof VerticalLayout);
        VerticalLayout layout = (VerticalLayout) root;
        assertEquals(1, layout.getComponentCount());
        assertEquals(inner, layout.getComponent(0));
        assertEquals("", extendedTextField.getRemainingLength().getValue());
        assertEquals(-1, extendedTextField.determineLabelMaxLength());
    }

    /**
     * Verifies manual update of remaining length adjusts the label value.
     */
    @Test
    public void testUpdateRemainingLengthChangesLabelValue() {
        ExtendedTextFieldDefinition definition = createDefinition(-1, 10);
        TextField inner = new TextField();
        inner.setValue("");
        ExtendedTextField extendedTextField = new ExtendedTextField(definition, inner);
        extendedTextField.initContent();
        assertEquals("10/10", extendedTextField.getRemainingLength().getValue());
        extendedTextField.updateRemainingLength(5, 10);
        assertEquals("5/10", extendedTextField.getRemainingLength().getValue());
    }

    /**
     * Verifies setValue delegates to inner field and updates remaining length label accordingly.
     */
    @Test
    public void testSetValueDelegatesToInnerFieldAndUpdatesLabel() {
        ExtendedTextFieldDefinition definition = createDefinition(-1, 10);
        TextField inner = new TextField();
        inner.setValue("");
        ExtendedTextField extendedTextField = new ExtendedTextField(definition, inner);
        extendedTextField.initContent();
        assertEquals("10/10", extendedTextField.getRemainingLength().getValue());
        extendedTextField.setValue("abcdefghi");
        assertEquals("abcdefghi", inner.getValue());
        assertEquals("abcdefghi", extendedTextField.getValue());
        assertEquals("1/10", extendedTextField.getRemainingLength().getValue());
        extendedTextField.setValue("abcdefghij");
        assertEquals("abcdefghij", inner.getValue());
        assertEquals("0/10", extendedTextField.getRemainingLength().getValue());
        assertFalse(extendedTextField.isEmpty());
    }

    /**
     * Verifies listener on inner field triggers remaining length recalculation.
     */
    @Test
    public void testInnerFieldValueChangeUpdatesLabel() {
        ExtendedTextFieldDefinition definition = createDefinition(-1, 10);
        TextField inner = new TextField();
        inner.setValue("");
        ExtendedTextField extendedTextField = new ExtendedTextField(definition, inner);
        extendedTextField.initContent();
        assertEquals("10/10", extendedTextField.getRemainingLength().getValue());
        inner.setValue("abcd");
        assertEquals("6/10", extendedTextField.getRemainingLength().getValue());
    }

    /**
     * Verifies negative remaining length when initial value exceeds available length.
     */
    @Test
    public void testInitialValueExceedsAvailableLength() {
        ExtendedTextFieldDefinition definition = createDefinition(-1, 10);
        TextField inner = new TextField();
        inner.setValue("abcdefghijkl");
        ExtendedTextField extendedTextField = new ExtendedTextField(definition, inner);
        extendedTextField.initContent();
        assertEquals("-2/10", extendedTextField.getRemainingLength().getValue());
    }

    /**
     * Verifies getEmptyValue delegates to inner field.
     */
    @Test
    public void testGetEmptyValueDelegates() {
        ExtendedTextFieldDefinition definition = createDefinition(-1, 10);
        TextField inner = new TextField();
        ExtendedTextField extendedTextField = new ExtendedTextField(definition, inner);
        extendedTextField.initContent();
        assertEquals("", extendedTextField.getEmptyValue());
    }

    /**
     * Verifies addValueChangeListener delegation and callback invocation.
     */
    @Test
    public void testAddValueChangeListenerDelegates() {
        ExtendedTextFieldDefinition definition = createDefinition(-1, 10);
        TextField inner = new TextField();
        ExtendedTextField extendedTextField = new ExtendedTextField(definition, inner);
        extendedTextField.initContent();
        AtomicReference<String> captured = new AtomicReference<>();
        extendedTextField.addValueChangeListener(event -> captured.set(event.getValue()));
        extendedTextField.setValue("xyz");
        assertEquals("xyz", captured.get());
    }

    /**
     * Verifies isEmpty delegation reflecting inner field state.
     */
    @Test
    public void testIsEmptyDelegates() {
        ExtendedTextFieldDefinition definition = createDefinition(-1, 10);
        TextField inner = new TextField();
        ExtendedTextField extendedTextField = new ExtendedTextField(definition, inner);
        extendedTextField.initContent();
        assertTrue(extendedTextField.isEmpty());
        extendedTextField.setValue("a");
        assertFalse(extendedTextField.isEmpty());
    }

    /**
     * Verifies getRemainingLength returns the label instance (always present, but empty when no lengths configured).
     */
    @Test
    public void testGetRemainingLengthReturnsLabel() {
        ExtendedTextFieldDefinition definition = createDefinition(-1, -1);
        TextField inner = new TextField();
        ExtendedTextField extendedTextField = new ExtendedTextField(definition, inner);
        extendedTextField.initContent();
        assertEquals("", extendedTextField.getRemainingLength().getValue());
        assertEquals(-1, extendedTextField.determineLabelMaxLength());
    }

    private ExtendedTextFieldDefinition createDefinition(int max, int recommend) {
        final ExtendedTextFieldDefinition definition = new ExtendedTextFieldDefinition();
        definition.setMaxLength(max);
        definition.setRecommendedLength(recommend);
        return definition;
    }
}
