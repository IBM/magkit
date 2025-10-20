package de.ibmix.magkit.ui.dialogs.validators;

/*-
 * #%L
 * IBM iX Magnolia Kit UI
 * %%
 * Copyright (C) 2023 - 2024 IBM iX
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

import info.magnolia.dam.jcr.JcrAsset;
import info.magnolia.dam.jcr.JcrFolder;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MimeTypeValidator}.
 * @author frank.sommer
 * @since 2024-01-24
 */
public class MimeTypeValidatorTest {

    private MimeTypeValidator _mimeTypeValidator;

    @Before
    public void setUp() {
        final MimeTypeValidatorDefinition validatorDefinition = new MimeTypeValidatorDefinition();
        validatorDefinition.setErrorMessage("error message");
        validatorDefinition.setAcceptedMimeTypes(List.of("image/*"));
        _mimeTypeValidator = new MimeTypeValidator(validatorDefinition);
    }

    @Test
    public void nullValue() {
        assertThat(_mimeTypeValidator.isValidValue(null), is(true));
    }

    @Test
    public void folder() {
        final JcrFolder jcrFolder = mock(JcrFolder.class);
        assertThat(_mimeTypeValidator.isValidValue(jcrFolder), is(false));
    }

    @Test
    public void document() {
        final JcrAsset jcrAsset = mock(JcrAsset.class);
        when(jcrAsset.isAsset()).thenReturn(true);
        when(jcrAsset.getMimeType()).thenReturn("application/pdf");
        assertThat(_mimeTypeValidator.isValidValue(jcrAsset), is(false));
    }

    @Test
    public void image() {
        final JcrAsset jcrAsset = mock(JcrAsset.class);
        when(jcrAsset.isAsset()).thenReturn(true);
        when(jcrAsset.getMimeType()).thenReturn("image/jpeg");
        assertThat(_mimeTypeValidator.isValidValue(jcrAsset), is(true));
    }

    @Test
    public void svg() {
        final JcrAsset jcrAsset = mock(JcrAsset.class);
        when(jcrAsset.isAsset()).thenReturn(true);
        when(jcrAsset.getMimeType()).thenReturn("image/svg+xml");
        assertThat(_mimeTypeValidator.isValidValue(jcrAsset), is(true));
    }
}
