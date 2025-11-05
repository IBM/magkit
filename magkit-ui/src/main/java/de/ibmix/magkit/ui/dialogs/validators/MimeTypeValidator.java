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
 * Validator for accepted asset MIME types.
 * <p>
 * Determines validity by checking if the asset's MIME type matches any configured accepted pattern. Patterns support
 * simple wildcard replacement (<code>*</code>) translated to <code>.*</code> for regex matching; dots and plus signs
 * are escaped to ensure literal matching before wildcard expansion.
 * </p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Supports list of accepted MIME type patterns (e.g. <code>image/*</code>, <code>application/pdf</code>).</li>
 *   <li>Gracefully treats <code>null</code> value or empty accepted list as valid.</li>
 *   <li>Ignores non-asset items (folders) unless explicitly configured.</li>
 * </ul>
 *
 * <p>Usage preconditions: Provide accepted MIME types collection via {@link MimeTypeValidatorDefinition}; may be empty.</p>
 * <p>Null and error handling: <code>null</code> item considered valid; non-asset items return false to avoid false positives.</p>
 * <p>Thread-safety: Stateless apart from immutable definition reference; not thread-safe for concurrent modification.</p>
 *
 * @author frank.sommer
 * @since 2024-01-24
 */
public class MimeTypeValidator extends AbstractValidator<Item> {

    private final MimeTypeValidatorDefinition _definition;

    public MimeTypeValidator(MimeTypeValidatorDefinition definition) {
        super(definition.getErrorMessage());
        _definition = definition;
    }

    /**
     * Check validity of the given DAM item.
     * @param item DAM item (may be null)
     * @return true if valid under configured MIME type rules
     */
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

    /**
     * Apply validation according to Vaadin's validator contract.
     * @param value DAM item value
     * @param context value context
     * @return validation result
     */
    @Override
    public ValidationResult apply(Item value, ValueContext context) {
        return toResult(value, isValidValue(value));
    }
}
