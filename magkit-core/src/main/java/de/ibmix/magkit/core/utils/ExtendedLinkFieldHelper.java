package de.ibmix.magkit.core.utils;

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

import jakarta.inject.Singleton;

import javax.jcr.Node;
import java.net.URI;

import static info.magnolia.cms.util.SelectorUtil.SELECTOR_DELIMITER;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.lastIndexOf;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBefore;

/**
 * Utility to extract and re-compose extended link components (base path/UUID, selectors, query string, anchor) for Magnolia link fields.
 * <p>
 * Main functionality:
 * <ul>
 *   <li>Parse a raw link/UUID value and obtain its base portion independently of extended components.</li>
 *   <li>Extract selector chain, query string and anchor fragment from a value.</li>
 *   <li>Merge individual components back into a single extended link string in Magnolia selector format.</li>
 *   <li>Create an extended link for a JCR {@link Node} property including optional selector, query and anchor suffix properties.</li>
 * </ul>
 * Key details:
 * <ul>
 *   <li>Selectors are injected directly before an eventual file extension (last dot) or appended at end if none exists.</li>
 *   <li>A UUID (36 chars) at start is treated as base if detected even when represented internally as a path.</li>
 *   <li>Values that are not paths, anchors or UUIDs yield {@code null} for URI creation.</li>
 * </ul>
 * Usage preconditions: Methods expect either a Magnolia path, anchor string, or a raw UUID (optionally followed by extended parts).
 * Side effects: The helper is stateless and does not modify passed {@link Node} instances; it only reads properties.
 * Null and error handling: Invalid or unsupported input strings produce {@code null} results for component getters; merge operations skip null/blank parts gracefully.
 * Thread-safety: The class is stateless and annotated {@link Singleton}; all methods are thread-safe.
 * Usage example:
 * <pre>
 *   ExtendedLinkFieldHelper helper = new ExtendedLinkFieldHelper();
 *   String extended = helper.mergeComponents("/my/page", "print.detail", "a=1&amp;b=2", "section");
 *   // Result: /my/page.print.detail.?a=1&amp;b=2#section
 * </pre>
 *
 * @author Philipp Güttler (IBM iX)
 * @since 2015-06-03
 */
@Singleton
public class ExtendedLinkFieldHelper {

    public static final String SUFFIX_ANCHOR = "_anchor";
    public static final String SUFFIX_QUERY = "_query";
    public static final String SUFFIX_SELECTOR = "_selector";

    private static final String TAG_FILE_EXTENSION = ".";
    private static final int DEFAULT_UUID_LENGTH = 36;

    /**
     * Returns the base portion (path or UUID) of the given value, omitting selectors, query and anchor.
     * If the path begins with a UUID it returns that UUID instead of the path representation.
     *
     * @param value raw value possibly containing selectors/query/anchor
     * @return base path or UUID; {@code null} if value cannot be interpreted as URI/UUID
     */
    public String getBase(final String value) {
        final URI uri = createUri(value);
        String result = null;
        if (uri != null) {
            result = substringBefore(uri.getPath(), SELECTOR_DELIMITER) + substringAfterLast(uri.getPath(), SELECTOR_DELIMITER);
            final String identifier = removeStart(result, "/");
            if (LinkTool.isUuid(identifier)) {
                result = identifier;
            }
        }
        return result;
    }

    /**
     * Removes the base (path/UUID) portion and returns only extended components concatenated.
     *
     * @param value raw value containing path/UUID and optional components
     * @return merged selector/query/anchor without base (may be empty string if none present)
     */
    public String stripBase(final String value) {
        return mergeComponents(null, getSelectors(value), getQuery(value), getAnchor(value));
    }

    /**
     * Extracts the selector chain from the given URI string.
     * Selectors are Magnolia-specific fragments between dots preceding the final path segment or extension.
     *
     * @param uri raw link value
     * @return selector chain joined by '.' or {@code null} if none present
     */
    public String getSelectors(final String uri) {
        String nodePath = uri;
        final StringBuilder selectors = new StringBuilder();
        while (containsMoreSelectors(nodePath)) {
            nodePath = substringAfter(nodePath, SELECTOR_DELIMITER);
            if (containsMoreSelectors(nodePath)) {
                selectors.append(substringBefore(nodePath, SELECTOR_DELIMITER)).append(SELECTOR_DELIMITER);
            }
        }
        return selectors.length() == 0 ? null : removeEnd(selectors.toString(), SELECTOR_DELIMITER);
    }

    /**
     * Extracts the query string (without leading '?').
     *
     * @param uri raw link value
     * @return query string or {@code null} if absent or value invalid
     */
    public String getQuery(final String uri) {
        final URI obj = createUri(uri);
        return obj == null ? null : obj.getQuery();
    }

    /**
     * Extracts the anchor fragment (without leading '#').
     *
     * @param uri raw link value
     * @return anchor fragment or {@code null} if absent or value invalid
     */
    public String getAnchor(final String uri) {
        final URI obj = createUri(uri);
        return obj == null ? null : obj.getFragment();
    }

    /**
     * Merges the given components into a Magnolia extended link string.
     * Selector chain is inserted before the last file extension dot if present, or appended at end otherwise.
     * Null or blank components are ignored.
     *
     * @param base base path or UUID (may be {@code null})
     * @param selector selector chain (dot separated) or {@code null}
     * @param query query string without leading '?' or {@code null}
     * @param anchor anchor fragment without leading '#' or {@code null}
     * @return composed extended link string (never {@code null})
     */
    public String mergeComponents(final String base, final String selector, final String query, final String anchor) {
        StringBuilder result = new StringBuilder(defaultString(base));
        if (isNotBlank(selector)) {
            int extIndex = lastIndexOf(result, TAG_FILE_EXTENSION);
            if (extIndex == -1) {
                extIndex = result.length();
            }
            result.insert(extIndex, SELECTOR_DELIMITER + selector + SELECTOR_DELIMITER);
        }
        if (isNotBlank(query)) {
            result.append('?').append(query);
        }
        if (isNotBlank(anchor)) {
            result.append('#').append(anchor);
        }
        return result.toString();
    }

    /**
     * Creates an extended link for a JCR node reference, augmenting a base link with optional anchor, query and selectors stored in suffixed properties.
     *
     * @param source node containing the link reference and optional suffixed properties
     * @param linkPropertyName base property name holding the reference target
     * @param workspace target workspace name for internal link resolution
     * @param linkType type of link to create (internal/external/redirect)
     * @return composed URL or {@code null} if base link cannot be resolved or is empty
     */
    public String createExtendedLink(Node source, String linkPropertyName, String workspace, LinkTool.LinkType linkType) {
        String link = LinkTool.createLinkForReference(source, linkPropertyName, workspace, linkType);
        if (isNotEmpty(link)) {
            String anchor = getString(source, linkPropertyName + SUFFIX_ANCHOR);
            String query = getString(source, linkPropertyName + SUFFIX_QUERY);
            String selectors = getString(source, linkPropertyName + SUFFIX_SELECTOR);
            link = mergeComponents(link, selectors, query, anchor);
        }
        return link;
    }

    /**
     * Creates a {@link URI} from the provided value if it resembles a Magnolia path, anchor or UUID (followed by components).
     * For raw UUID values a synthetic leading '/' is added to satisfy {@link URI} path requirements.
     *
     * @param value raw path, anchor or UUID (+ optional components)
     * @return URI instance or {@code null} if not interpretable
     */
    protected URI createUri(final String value) {
        return LinkTool.isPath(value) || LinkTool.isAnchor(value) ? URI.create(value) : createUuidUri(value);
    }

    /**
     * Creates a {@link URI} from the provided value if it resembles a Magnolia UUID (followed by components).
     * For raw UUID values a synthetic leading '/' is added to satisfy {@link URI} path requirements.
     *
     * @param value raw path, anchor or UUID (+ optional components)
     * @return URI instance or {@code null} if not an UUID
     */
    protected URI createUuidUri(final String value) {
        return LinkTool.isUuid(substring(value, 0, DEFAULT_UUID_LENGTH)) ? URI.create("/" + defaultString(value)) : null;
    }

    /**
     * Determines if more selector segments are present in the remaining URI string portion.
     * A segment is considered present if the string contains a '.' and does not start with a '.' (file extension boundary).
     *
     * @param uri remaining URI fragment
     * @return {@code true} if another selector segment can be parsed; otherwise {@code false}
     */
    protected boolean containsMoreSelectors(final String uri) {
        return !startsWith(uri, TAG_FILE_EXTENSION) && contains(uri, SELECTOR_DELIMITER);
    }
}
