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
import info.magnolia.cms.util.SelectorUtil;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import static de.ibmix.magkit.core.utils.LinkTool.isAnchor;
import static de.ibmix.magkit.core.utils.LinkTool.isExternalLink;
import static de.ibmix.magkit.core.utils.LinkTool.isPath;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substringBefore;

/**
 * Advanced link converter supporting Magnolia page paths with selectors, query parameters and anchors.
 * <p>
 * Extends {@link LinkConverter} by allowing compound suffixes (selector, query, anchor) to be preserved while converting
 * the path portion to a node identifier. Reverse conversion reconstructs the path with original suffix.
 * </p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Supports selector (e.g. <code>.detail</code>) via Magnolia's {@link SelectorUtil} delimiter.</li>
 *   <li>Preserves query strings and anchors while resolving the underlying node.</li>
 *   <li>Graceful handling of external URLs/anchors by delegating to super converter.</li>
 * </ul>
 *
 * <p>Usage example (dialog definition):</p>
 * <pre>
 *  $type: pageLinkField
 *  textInputAllowed: true
 *  converterClass: de.ibmix.magkit.ui.dialogs.fields.ExtendedLinkConverter
 *  fieldBinderClass: de.ibmix.magkit.ui.dialogs.fields.ExtendedLinkBinder
 * </pre>
 *
 * <p>Thread-safety: Not thread-safe; relies on injected datasource and JCR session.</p>
 *
 * @author frank.sommer
 * @since 2023-11-28
 */
public class ExtendedLinkConverter extends LinkConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedLinkConverter.class);
    private static final long serialVersionUID = 4484406102548210913L;

    public static final String TAG_ANCHOR = "#";
    public static final String TAG_QUERY = "?";
    public static final String TAG_SELECTOR = SelectorUtil.SELECTOR_DELIMITER;

    @Inject
    public ExtendedLinkConverter(JcrDatasource datasource) {
        super(datasource);
    }

    /**
     * Convert path with optional suffix to node identifier plus preserved suffix.
     * Delegates to super if external/anchor or simple path.
     * @param path user input (may be null)
     * @param context Vaadin value context
     * @return result wrapping identifier+suffix or delegated value
     */
    @Override
    public Result<String> convertToModel(String path, ValueContext context) {
        Result<String> result;

        if (isCoveredBySuperConverter(path)) {
            result = super.convertToModel(path, context);
        } else if (isPath(path)) {
            try {
                String converted = convertToIdentifier(path);
                result = isBlank(converted) ? Result.error("Path not found: " + path) : Result.ok(converted);
            } catch (RepositoryException e) {
                result = Result.error(e.getMessage() != null ? e.getMessage() : "Path not found: " + path);
            }
        } else {
            result = Result.error("Unsupported path format: " + path);
        }

        return result;
    }

    /**
     * Convert identifier (+suffix) back to path (+suffix) with JCR resolution; delegate when external/anchor/simple.
     * @param uuid identifier value possibly with suffix
     * @param context Vaadin value context
     * @return resolved path (+suffix) or delegated value
     */
    @Override
    public String convertToPresentation(String uuid, ValueContext context) {
        String result;

        if (isCoveredBySuperConverter(uuid)) {
            result = super.convertToPresentation(uuid, context);
        } else {
            result = convertToPath(uuid);
        }

        return result;
    }

    /**
     * Internal: Convert composite path (with suffix) to identifier plus suffix.
     * @param pathWithSuffix full path including suffix
     * @return identifier+suffix or empty string
     * @throws RepositoryException if JCR lookup fails
     */
    protected String convertToIdentifier(final String pathWithSuffix) throws RepositoryException {
        String path = getNodePart(pathWithSuffix);
        String query = pathWithSuffix.replace(path, EMPTY);
        Node node = getNodeByPath(path);
        return node != null ? node.getIdentifier() + query : EMPTY;
    }

    /**
     * Convert identifier (+suffix) back to full path (+suffix).
     * @param value identifier with suffix
     * @return path with suffix or null on failure
     */
    protected String convertToPath(final String value) {
        String result = null;
        try {
            String identifier = getNodePart(value);
            String query = value.replace(identifier, EMPTY);
            Node node = getNodeByIdentifier(identifier);
            result = node != null ? node.getPath() + query : null;
        } catch (RepositoryException e) {
            LOGGER.error("Could not convert entry {} to path.", value, e);
        }
        return result;
    }

    /**
     * Extract pure node part (strip selector/query/anchor) from value.
     * @param value raw value
     * @return base node path or identifier segment
     */
    protected static String getNodePart(String value) {
        return substringBefore(substringBefore(substringBefore(value, TAG_SELECTOR), TAG_QUERY), TAG_ANCHOR);
    }

    /**
     * Determine if super converter should handle this value (external, anchor, blank, simple path without suffix).
     * @param path raw input
     * @return true if super converter covers the case
     */
    protected static boolean isCoveredBySuperConverter(String path) {
        return isBlank(path) || isExternalLink(path) || isAnchor(path) || !containsAny(path, TAG_ANCHOR, TAG_QUERY, TAG_SELECTOR);
    }

}
