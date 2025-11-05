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
 * Converter for link input supporting both internal repository paths and external URLs/anchors.
 * <p>
 * Extends {@link JcrPathToIdentifierConverter} by a guard that bypasses JCR resolution for external URLs (http/https),
 * anchor references ("#...") and empty values, returning them unchanged. Internal absolute paths are converted to the
 * corresponding node identifiers for storage and vice versa.
 * </p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Transparent handling of external links and anchors without repository lookup.</li>
 *   <li>Delegates internal path resolution to Magnolia's JCR converter logic.</li>
 *   <li>Graceful {@code null} input handling (returns {@code null}).</li>
 * </ul>
 *
 * <p>Usage example (dialog definition snippet):</p>
 * <pre>
 *  textInputAllowed: true
 *  converterClass: de.ibmix.magkit.ui.dialogs.fields.LinkConverter
 * </pre>
 *
 * <p>Thread-safety: Stateless apart from injected datasource; not thread-safe for concurrent mutation but safe to reuse per UI thread.</p>
 *
 * @author frank.sommer
 * @since 2023-11-28
 */
public class LinkConverter extends JcrPathToIdentifierConverter {
    private static final long serialVersionUID = 4484406162548230911L;

    /**
     * Create converter instance with injected datasource.
     * @param datasource JCR datasource for path resolution
     */
    @Inject
    public LinkConverter(JcrDatasource datasource) {
        super(datasource);
    }

    /**
     * Convert a user entered path or URL to a node identifier if internal; otherwise pass through.
     * @param path raw user input (may be null)
     * @param context Vaadin value context (ignored here)
     * @return Result wrapping identifier, unchanged external string or null
     */
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

    /**
     * Convert a stored identifier back to its JCR path if internal; otherwise return unchanged
     * external URL / anchor.
     * @param uuid identifier or external/anchor input (may be null)
     * @param context Vaadin value context (ignored here)
     * @return JCR path, original external URL/anchor or null
     */
    @Override
    public String convertToPresentation(String uuid, ValueContext context) {
        String result = uuid;
        if (!isExternalLink(uuid) && !isAnchor(uuid)) {
            result = super.convertToPresentation(uuid, context);
        }
        return result;
    }

}
