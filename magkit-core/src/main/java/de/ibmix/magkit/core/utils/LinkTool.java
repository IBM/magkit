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

import info.magnolia.context.MgnlContext;
import info.magnolia.link.LinkUtil;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.jcr.Node;
import java.net.URISyntaxException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static info.magnolia.jcr.util.PropertyUtil.getString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * Utility class providing helper methods for working with different kinds of links inside Magnolia.
 * <p>
 * Main functionalities:
 * <ul>
 *   <li>Detection of UUID formatted link references.</li>
 *   <li>Classification of link values (external URLs, repository paths, anchors).</li>
 *   <li>Creation of internal, external or redirect links via {@link LinkType} strategies.</li>
 *   <li>Safe construction of external resource links including context path handling.</li>
 * </ul>
 * Key features & important details:
 * <ul>
 *   <li>All methods are static; the class is {@code final} and therefore stateless and thread-safe.</li>
 *   <li>Null-safe checks for link categorisation to avoid {@link NullPointerException}.</li>
 *   <li>Encapsulates Magnolia specific link creation logic behind simple semantic methods.</li>
 *   <li>Graceful error handling when building URIs (errors are logged; no exception propagation).</li>
 * </ul>
 * Usage example:
 * <pre>
 *   if (LinkTool.isExternalLink(linkValue)) {
 *       // handle external link
 *   }
 *   String url = LinkTool.createLinkForReference(sourceNode, "link", "website", LinkTool.LinkType.INTERNAL);
 * </pre>
 * Thread-safety: All operations are side-effect free and do not mutate shared state; safe for concurrent use.
 * Null & error handling: Null inputs are tolerated in classification helpers; URI syntax issues are logged only.
 *
 * @author Frank Sommer (25.10.2007)
 * @since 2007-10-25
 */
public final class LinkTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkTool.class);

    public static final Pattern UUID_PATTERN = Pattern.compile("^[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}$");

    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final int HTTP_PORT = 80;
    public static final int HTTPS_PORT = 443;
    public static final String PROTOCOL_DELIMITER = "://";
    public static final String HTTP_PREFIX = HTTP + PROTOCOL_DELIMITER;
    public static final String HTTPS_PREFIX = HTTPS + PROTOCOL_DELIMITER;

    /**
     * Checks if the given link value matches a UUID format (lowercase hexadecimal representation).
     *
     * @param link the string to check; may be null or empty
     * @return true if the value matches {@link #UUID_PATTERN}, otherwise false
     */
    public static boolean isUuid(String link) {
        boolean isUuid = false;
        if (isNotEmpty(link)) {
            Matcher matcher = UUID_PATTERN.matcher(link);
            isUuid = matcher.matches();
        }
        return isUuid;
    }

    /**
     * Null-safe check whether a link starts with an HTTP/HTTPS protocol prefix and is therefore considered external.
     *
     * @param linkValue value to check; may be null
     * @return true if the value starts with {@code http://} or {@code https://}
     */
    public static boolean isExternalLink(@Nullable final String linkValue) {
        return startsWithIgnoreCase(linkValue, HTTPS_PREFIX) || startsWithIgnoreCase(linkValue, HTTP_PREFIX);
    }

    /**
     * Null-safe check whether the provided value represents an absolute path (starts with '/').
     *
     * @param value value to check; may be null
     * @return true if the value starts with '/'
     */
    public static boolean isPath(@Nullable final String value) {
        return startsWith(value, "/");
    }

    /**
     * Null-safe check whether the provided value is an anchor fragment (starts with '#').
     *
     * @param value value to check; may be null
     * @return true if the value starts with '#'
     */
    public static boolean isAnchor(@Nullable final String value) {
        return startsWith(value, "#");
    }

    /**
     * Creates a link for a reference stored as property value on the source node. For non-external values the property
     * is treated as a node identifier (UUID or path) and resolved; the resulting node is converted using the chosen
     * {@link LinkType} strategy.
     *
     * @param source           the source node containing the reference property
     * @param linkPropertyName the property name holding the reference
     * @param workspace        the workspace name where the target node resides
     * @param linkType         strategy determining how to convert the target node into a URL; may be null
     * @return the resolved URL (internal/external/redirect) or null if resolution fails or input blank/external
     */
    public static String createLinkForReference(Node source, String linkPropertyName, String workspace, LinkType linkType) {
        String link = isNotBlank(linkPropertyName) ? trimToNull(getString(source, linkPropertyName)) : null;
        if (isNotBlank(link) && !isExternalLink(link)) {
            Node target = NodeUtils.getNodeByReference(workspace, link);
            link = linkType != null ? linkType.toLink(target) : null;
        }
        return link;
    }

    /**
     * Creates an external absolute URL for a given resource path beneath the site's context path.
     * The scheme, host and port are derived from the current site node via {@link LinkType#EXTERNAL} while the provided
     * {@code path} is appended (including context path).
     *
     * @param node the current site node used to derive host information
     * @param path the resource path beneath the context path (e.g. "/resources/image.jpg")
     * @return a fully qualified external URL string or an empty string if URI creation fails
     */
    public static String createExternalLinkForPath(final Node node, final String path) {
        String link = "";
        try {
            URIBuilder uriBuilder = new URIBuilder(LinkType.EXTERNAL.toLink(node)).setPath(MgnlContext.getContextPath() + path);
            link = uriBuilder.toString();
        } catch (URISyntaxException e) {
            LOGGER.error("Error creating link.", e);
        }
        return link;
    }

    /**
     * Enumeration signaling how to transform a Magnolia node into a URL. Functions cover internal absolute links,
     * external links (including domain) and redirect links (context path stripped).
     */
    public enum LinkType {
        INTERNAL(LinkUtil::createAbsoluteLink),
        EXTERNAL(LinkUtil::createExternalLink),
        REDIRECT(n -> removeStart(LinkUtil.createAbsoluteLink(n), MgnlContext.getContextPath()));

        private final Function<Node, String> _toLinkFunction;

        LinkType(Function<Node, String> toLinkFunction) {
            _toLinkFunction = toLinkFunction;
        }

        /**
         * Applies the strategy to produce a URL representation for the given node.
         *
         * @param node the Magnolia node to transform
         * @return resulting URL string (never null, may be empty depending on underlying Magnolia API)
         */
        public String toLink(Node node) {
            return _toLinkFunction.apply(node);
        }
    }

    private LinkTool() {
    }
}
