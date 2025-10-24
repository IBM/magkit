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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MimeTypeValidator}.
 * @author frank.sommer
 * @since 2024-01-24
 */
public class MimeTypeValidatorTest {

    private MimeTypeValidator _mimeTypeValidator;

    @BeforeEach
    public void setUp() {
        final MimeTypeValidatorDefinition validatorDefinition = new MimeTypeValidatorDefinition();
        validatorDefinition.setErrorMessage("error message");
        validatorDefinition.setAcceptedMimeTypes(List.of("image/*"));
        _mimeTypeValidator = new MimeTypeValidator(validatorDefinition);
    }

    @Test
    public void nullValue() {
        assertTrue(_mimeTypeValidator.isValidValue(null));
    }

    @Test
    public void folder() {
        final JcrFolder jcrFolder = mock(JcrFolder.class);
        assertFalse(_mimeTypeValidator.isValidValue(jcrFolder));
    }

    @Test
    public void document() {
        final JcrAsset jcrAsset = mock(JcrAsset.class);
        when(jcrAsset.isAsset()).thenReturn(true);
        when(jcrAsset.getMimeType()).thenReturn("application/pdf");
        assertFalse(_mimeTypeValidator.isValidValue(jcrAsset));
    }

    @Test
    public void image() {
        final JcrAsset jcrAsset = mock(JcrAsset.class);
        when(jcrAsset.isAsset()).thenReturn(true);
        when(jcrAsset.getMimeType()).thenReturn("image/jpeg");
        assertTrue(_mimeTypeValidator.isValidValue(jcrAsset));
    }

    @Test
    public void svg() {
        final JcrAsset jcrAsset = mock(JcrAsset.class);
        when(jcrAsset.isAsset()).thenReturn(true);
        when(jcrAsset.getMimeType()).thenReturn("image/svg+xml");
        assertTrue(_mimeTypeValidator.isValidValue(jcrAsset));
    }
}
