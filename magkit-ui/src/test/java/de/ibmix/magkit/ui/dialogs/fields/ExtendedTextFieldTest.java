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

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Unit tests for {@link ExtendedTextField} remaining-length calculation.
 *
 * @author frank.sommer
 * @since 2021-02-17
 */
public class ExtendedTextFieldTest {

    @Test
    public void testJustRecommendedLength() {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(-1, 10), null);
        assertThat(extendedTextField.determineLabelMaxLength(), is(10));
    }

    @Test
    public void testJustMaxLength() {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(10, -1), null);
        assertThat(extendedTextField.determineLabelMaxLength(), is(10));
    }

    @Test
    public void testMaxGtRecommendedLength() {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(10, 20), null);
        assertThat(extendedTextField.determineLabelMaxLength(), is(10));
    }

    @Test
    public void testMaxLtRecommendedLength() {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(20, 10), null);
        assertThat(extendedTextField.determineLabelMaxLength(), is(10));
    }

    private ExtendedTextFieldDefinition createDefinition(int max, int recommend) {
        final ExtendedTextFieldDefinition definition = new ExtendedTextFieldDefinition();
        definition.setMaxLength(max);
        definition.setRecommendedLength(recommend);
        return definition;
    }
}
