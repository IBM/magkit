package com.aperto.magkit.utils;

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

import javax.inject.Singleton;
import javax.jcr.Node;
import java.net.URI;

import static com.aperto.magkit.utils.LinkTool.isAnchor;
import static com.aperto.magkit.utils.LinkTool.isPath;
import static com.aperto.magkit.utils.LinkTool.isUuid;
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
 * Helper class to get partitions of an uri.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 03.06.2015
 */
@Singleton
public class ExtendedLinkFieldHelper {

    public static final String SUFFIX_ANCHOR = "_anchor";
    public static final String SUFFIX_QUERY = "_query";
    public static final String SUFFIX_SELECTOR = "_selector";

    private static final String TAG_FILE_EXTENSION = ".";
    private static final int DEFAULT_UUID_LENGTH = 36;

    /**
     * Returns the path of an uri. If the uri scheme contains an identifier, the identifier is returned instead, even it is treaded as path internally.
     *
     * @param value the uri string
     * @return the path or the uuid
     */
    public String getBase(final String value) {
        final URI uri = createUri(value);
        String result = null;
        if (uri != null) {
            result = substringBefore(uri.getPath(), SELECTOR_DELIMITER) + substringAfterLast(uri.getPath(), SELECTOR_DELIMITER);
            final String identifier = removeStart(result, "/");
            if (isUuid(identifier)) {
                result = identifier;
            }
        }
        return result;
    }

    /**
     * Removes all components from the given uri.
     *
     * @param value the uri, which might contain additional components
     * @return everything except the path
     */
    public String stripBase(final String value) {
        return mergeComponents(null, getSelectors(value), getQuery(value), getAnchor(value));
    }

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

    public String getQuery(final String uri) {
        final URI obj = createUri(uri);
        return obj == null ? null : obj.getQuery();
    }

    public String getAnchor(final String uri) {
        final URI obj = createUri(uri);
        return obj == null ? null : obj.getFragment();
    }

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
     * Creates a link for the reference provided by the source node as property value.
     * Handles internal and external links and adds the extended link features (anchor, query string, selector).
     *
     * @param source the source node that contains the reference
     * @param linkPropertyName the name of the link property at source node
     * @param workspace the workspace name of the target node
     * @param linkType the LinkTool.LinkType that determines weather an internal, external or redirect URL should be created.
     * @return the URL for the reference or NULL
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
     * Creates an URI from given string.
     *
     * @param value a path or an uuid, and additional uri components
     * @return the uri or null
     */
    protected URI createUri(final String value) {
        return isPath(value) || isAnchor(value) ? URI.create(value) : isUuid(substring(value, 0, DEFAULT_UUID_LENGTH)) ? URI.create("/" + defaultString(value)) : null;
    }

    protected boolean containsMoreSelectors(final String uri) {
        return !startsWith(uri, TAG_FILE_EXTENSION) && contains(uri, SELECTOR_DELIMITER);
    }
}
