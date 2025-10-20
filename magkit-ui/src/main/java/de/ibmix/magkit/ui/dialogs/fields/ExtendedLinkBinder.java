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
import javax.inject.Provider;
import java.util.Optional;

/**
 * Custom {@link FieldBinder} ensuring default converters for link fields when Magnolia would not configure one.
 * <p>
 * Works together with {@link ExtendedLinkConverter} to provide flexible link editing without core modifications.
 * Falls back to {@link SelectFieldSupport#defaultConverter()} when super implementation yields none.
 * </p>
 * <p>Thread-safety: Not thread-safe; used during field binding in UI initialization.</p>
 *
 * @param <T> field value type
 * @author sebastian.bauch
 * @since 2022-03-04
 */
public class ExtendedLinkBinder<T> extends FieldBinder.Default<T> {

    private final SelectFieldSupport<T> _selectFieldSupport;

    @Inject
    ExtendedLinkBinder(ComponentProvider componentProvider, SelectFieldSupport<T> selectFieldSupport, Provider<UiFrameworkModule> uiFrameworkModuleProvider) {
        super(componentProvider, uiFrameworkModuleProvider);
        _selectFieldSupport = selectFieldSupport;
    }

    /**
     * Provide configured or fallback converter for the field definition.
     * @param definition field definition
     * @param field Vaadin field component
     * @return optional converter (never empty)
     */
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
