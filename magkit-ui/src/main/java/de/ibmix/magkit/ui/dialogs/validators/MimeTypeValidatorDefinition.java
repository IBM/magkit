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

import info.magnolia.ui.field.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.field.ValidatorType;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * Definition holding the accepted MIME type patterns for {@link MimeTypeValidator}.
 * <p>Patterns may include '*' wildcards. An empty collection treats all assets as valid.</p>
 * <p>Thread-safety: Configuration only; not thread-safe.</p>
 * @author frank.sommer
 * @since 2024-01-24
 */
@Getter
@Setter
@ValidatorType("mimeTypeValidator")
public class MimeTypeValidatorDefinition extends ConfiguredFieldValidatorDefinition {
    private Collection<String> _acceptedMimeTypes;

    public MimeTypeValidatorDefinition() {
        setFactoryClass(MimeTypeValidatorFactory.class);
    }
}
