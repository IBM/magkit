package de.ibmix.magkit.ui.dialogs.fields;

/*-
 * #%L
 * IBM iX Magnolia Kit UI
 * %%
 * Copyright (C) 2025 IBM iX
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
import info.magnolia.ui.field.FieldDefinition;
import info.magnolia.ui.field.SelectFieldSupport;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ExtendedLinkBinder} ensuring both branches of {@link ExtendedLinkBinder#createConfiguredConverter(FieldDefinition, HasValue)}.
 * Verifies behavior when a configured converter is provided by Magnolia and when a fallback is used. No state is retained.
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-31
 */
public class ExtendedLinkBinderTest {

    /**
     * Ensures that when the wrapped superclass method provides a converter, that converter is returned and the fallback is not used.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void returnsConfiguredConverterWhenSuperProvidesOne() {
        ComponentProvider componentProvider = mock(ComponentProvider.class);
        SelectFieldSupport<String> selectFieldSupport = mock(SelectFieldSupport.class);
        Converter<String, String> fallbackConverter = mock(Converter.class);
        when(selectFieldSupport.defaultConverter()).thenReturn(fallbackConverter);
        Converter<String, String> configuredConverter = mock(Converter.class);
        ExtendedLinkBinder<String> binder = new TestExtendedLinkBinder(configuredConverter, componentProvider, selectFieldSupport, null);
        FieldDefinition<String> definition = mock(FieldDefinition.class);
        HasValue<?> field = mock(HasValue.class);
        Optional<Converter<String, ?>> result = binder.createConfiguredConverter(definition, field);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertSame(configuredConverter, result.orElse(null));
        verify(selectFieldSupport, never()).defaultConverter();
    }

    /**
     * Ensures that when no configured converter is provided, the fallback from {@link SelectFieldSupport#defaultConverter()} is used.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void returnsFallbackConverterWhenSuperProvidesNone() {
        ComponentProvider componentProvider = mock(ComponentProvider.class);
        SelectFieldSupport<String> selectFieldSupport = mock(SelectFieldSupport.class);
        Converter<String, String> fallbackConverter = mock(Converter.class);
        when(selectFieldSupport.defaultConverter()).thenReturn(fallbackConverter);
        ExtendedLinkBinder<String> binder = new TestExtendedLinkBinder(null, componentProvider, selectFieldSupport, null);
        FieldDefinition<String> definition = mock(FieldDefinition.class);
        HasValue<?> field = mock(HasValue.class);
        Optional<Converter<String, ?>> result = binder.createConfiguredConverter(definition, field);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertSame(fallbackConverter, result.orElse(null));
        verify(selectFieldSupport).defaultConverter();
    }

    /**
     * Test subclass to override the wrapped super call deterministically for both test scenarios.
     */
    private static class TestExtendedLinkBinder extends ExtendedLinkBinder<String> {

        private final Converter<String, ?> _superConverter;

        TestExtendedLinkBinder(Converter<String, ?> superConverter, ComponentProvider componentProvider, SelectFieldSupport<String> selectFieldSupport, UiFrameworkModule uiFrameworkModuleProvider) {
            super(componentProvider, selectFieldSupport, uiFrameworkModuleProvider);
            _superConverter = superConverter;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected <PT> Optional<Converter<PT, ?>> superCreateConfiguredConverter(FieldDefinition<PT> definition, HasValue<?> field) {
            return Optional.ofNullable((Converter<PT, ?>) _superConverter);
        }
    }
}
