package de.ibmix.magkit.ui.dialogs.fields;

/*-
 * #%L
 * IBM iX Magnolia Kit UI
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

import com.vaadin.data.Converter;
import com.vaadin.data.HasValue;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.UiFrameworkModule;
import info.magnolia.ui.field.FieldBinder;
import info.magnolia.ui.field.FieldDefinition;
import info.magnolia.ui.field.SelectFieldSupport;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Custom binder for avoiding mgnl changes. Used with {@link ExtendedLinkConverter} in page link fields.
 *
 * @param <T> field value type
 * @author sebastian.bauch
 * @since 04.03.2022
 */
public class ExtendedLinkBinder<T> extends FieldBinder.Default<T> {

    private final SelectFieldSupport<T> _selectFieldSupport;

    @Inject
    ExtendedLinkBinder(ComponentProvider componentProvider, SelectFieldSupport<T> selectFieldSupport, UiFrameworkModule uiFrameworkModule) {
        super(componentProvider, uiFrameworkModule);
        _selectFieldSupport = selectFieldSupport;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected <PT> Optional<Converter<PT, ?>> createConfiguredConverter(FieldDefinition<PT> definition, HasValue<?> field) {
        Converter configuredConverter = super.createConfiguredConverter(definition, field).orElse(null);
        if (configuredConverter == null) {
            configuredConverter = _selectFieldSupport.defaultConverter();
        }
        return Optional.of(configuredConverter);
    }

}
