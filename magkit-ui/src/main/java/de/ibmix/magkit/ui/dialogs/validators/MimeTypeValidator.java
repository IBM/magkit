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

import com.vaadin.data.ValidationResult;
import com.vaadin.data.ValueContext;
import com.vaadin.data.validator.AbstractValidator;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.Item;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Validator for asset mimetypes.
 *
 * @author frank.sommer
 */
public class MimeTypeValidator extends AbstractValidator<Item> {

    private final MimeTypeValidatorDefinition _definition;

    public MimeTypeValidator(MimeTypeValidatorDefinition definition) {
        super(definition.getErrorMessage());
        _definition = definition;
    }

    public boolean isValidValue(Item item) {
        boolean valid = true;
        if (item != null) {
            valid = isEmpty(_definition.getAcceptedMimeTypes()) || isAcceptedMimeType(item);
        }
        return valid;
    }

    private boolean isAcceptedMimeType(Item item) {
        if (item.isAsset()) {
            for (String acceptedMimeType : _definition.getAcceptedMimeTypes()) {
                String regex = acceptedMimeType.replace(".", "\\.").replace("+", "\\+").replace("*", ".*");
                if (((Asset) item).getMimeType().matches(regex)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ValidationResult apply(Item value, ValueContext context) {
        return toResult(value, isValidValue(value));
    }
}
