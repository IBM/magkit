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

import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.editor.converter.JcrPathToIdentifierConverter;

import javax.inject.Inject;

import static de.ibmix.magkit.core.utils.LinkTool.isAnchor;
import static de.ibmix.magkit.core.utils.LinkTool.isExternalLink;

/**
 * Handles input of links by urls (external) or absolute paths (internal).
 *
 * @author frank.sommer
 * @since 28.11.2023
 */
public class LinkConverter extends JcrPathToIdentifierConverter {
    private static final long serialVersionUID = 4484406162548230911L;

    @Inject
    public LinkConverter(JcrDatasource datasource) {
        super(datasource);
    }

    @Override
    public Result<String> convertToModel(String path, ValueContext context) {
        Result<String> result;
        if (!isExternalLink(path) && !isAnchor(path)) {
            result = super.convertToModel(path, context);
        } else {
            result = Result.of(() -> path, Throwable::getMessage);
        }
        return result;
    }

    @Override
    public String convertToPresentation(String uuid, ValueContext context) {
        String result = uuid;
        if (!isExternalLink(uuid) && !isAnchor(uuid)) {
            result = super.convertToPresentation(uuid, context);
        }
        return result;
    }

}
