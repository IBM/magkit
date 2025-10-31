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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link SpecificMultiFieldDefinition} verifying default values and mutator behavior.
 *
 * Covered aspects:
 * <ul>
 *   <li>Default maximum component value equals {@link SpecificMultiDefinition#DEFAULT_MAX}.</li>
 *   <li>Setter updates maximum component value.</li>
 *   <li>Setter allows null (current behavior returns null though Javadoc suggests fallback).</li>
 *   <li>Parent component property accessor methods.</li>
 *   <li>Constructor assigns implementation class {@link SpecificMultiFormView}.</li>
 *   <li>Multiple updates of max components retain latest value.</li>
 *   <li>Null and empty parent property handling.</li>
 * </ul>
 *
 * NOTE: The Javadoc of {@link SpecificMultiFieldDefinition#setMaxComponents(Long)} states null should fall back to default,
 * but the current implementation stores null. Test documents existing behavior; adjust if implementation changes.
 *
 * @author wolf.bubenik
 * @since 2025-10-31
 */
public class SpecificMultiFieldDefinitionTest {

    /**
     * Verifies constructor sets default max components and implementation class.
     */
    @Test
    public void testDefaultState() {
        SpecificMultiFieldDefinition def = new SpecificMultiFieldDefinition();
        assertEquals(SpecificMultiDefinition.DEFAULT_MAX, def.getMaxComponents());
        assertSame(SpecificMultiFormView.class, def.getImplementationClass());
    }

    /**
     * Verifies max components can be updated to a custom value.
     */
    @Test
    public void testSetMaxComponents() {
        SpecificMultiFieldDefinition def = new SpecificMultiFieldDefinition();
        def.setMaxComponents(10L);
        assertEquals(10L, def.getMaxComponents());
    }

    /**
     * Verifies setting null currently results in null (no automatic fallback implemented).
     */
    @Test
    public void testSetNullMaxComponents() {
        SpecificMultiFieldDefinition def = new SpecificMultiFieldDefinition();
        def.setMaxComponents(null);
        assertNull(def.getMaxComponents());
    }

    /**
     * Verifies parent component property accessor methods.
     */
    @Test
    public void testParentComponentPropertyAccessors() {
        SpecificMultiFieldDefinition def = new SpecificMultiFieldDefinition();
        def.setParentCountProperty("parentProp");
        assertEquals("parentProp", def.getParentComponentProperty());
    }

    /**
     * Verifies multiple sequential updates of maxComponents retain the last explicit value including null.
     */
    @Test
    public void testMultipleMaxComponentUpdates() {
        SpecificMultiFieldDefinition def = new SpecificMultiFieldDefinition();
        def.setMaxComponents(5L);
        assertEquals(5L, def.getMaxComponents());
        def.setMaxComponents(7L);
        assertEquals(7L, def.getMaxComponents());
        def.setMaxComponents(null);
        assertNull(def.getMaxComponents());
        def.setMaxComponents(2L);
        assertEquals(2L, def.getMaxComponents());
    }

    /**
     * Verifies null and empty string handling for parent component property.
     */
    @Test
    public void testParentComponentPropertyNullAndEmpty() {
        SpecificMultiFieldDefinition def = new SpecificMultiFieldDefinition();
        assertNull(def.getParentComponentProperty());
        def.setParentCountProperty("");
        assertEquals("", def.getParentComponentProperty());
        def.setParentCountProperty(null);
        assertNull(def.getParentComponentProperty());
    }
}
