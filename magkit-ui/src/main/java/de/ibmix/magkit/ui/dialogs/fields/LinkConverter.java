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

import info.magnolia.ui.form.field.converter.BaseIdentifierToPathConverter;

import java.util.Locale;

import static de.ibmix.magkit.core.utils.LinkTool.isAnchor;
import static de.ibmix.magkit.core.utils.LinkTool.isExternalLink;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Handles input of links by urls (external) or absolute paths (internal).
 *
 * @author philipp.guettler
 * @since 06.03.14
 */
public class LinkConverter extends BaseIdentifierToPathConverter {

    @Override
    public String convertToModel(final String path, final Class<? extends String> targetType, final Locale locale) {
        String result = path;
        if (isNotBlank(path) && !isExternalLink(path) && !isAnchor(path)) {
            result = super.convertToModel(path, targetType, locale);
        }
        return result;
    }

    @Override
    public String convertToPresentation(final String uuid, final Class<? extends String> targetType, final Locale locale) {
        String result = uuid;
        if (isNotBlank(uuid) && !isExternalLink(uuid) && !isAnchor(uuid)) {
            result = super.convertToPresentation(uuid, targetType, locale);
        }
        return result;
    }

}
