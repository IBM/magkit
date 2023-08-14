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
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * Helper class for links.
 *
 * @author Frank Sommer (25.10.2007)
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
     * Checks if the given link is a uuid.
     *
     * @param link to check
     * @return true or false
     * @see #UUID_PATTERN
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
     * Null-safe check, if the link starts with a web protocol.
     *
     * @param linkValue to check
     * @return true for external link
     */
    public static boolean isExternalLink(@Nullable final String linkValue) {
        return startsWithIgnoreCase(linkValue, HTTPS_PREFIX) || startsWithIgnoreCase(linkValue, HTTP_PREFIX);
    }

    /**
     * Null-safe check, if the link starts with a slash.
     *
     * @param value to check
     * @return true for path links
     */
    public static boolean isPath(@Nullable final String value) {
        return startsWith(value, "/");
    }

    /**
     * Null-safe check if value starts with #.
     *
     * @param value to check
     * @return true for url fragment with starting #
     */
    public static boolean isAnchor(@Nullable final String value) {
        return startsWith(value, "#");
    }

    /**
     * Creates a link for the reference provided by the source node as property value.
     * Handles internal and external links.
     *
     * @param source           the source node that contains the reference
     * @param linkPropertyName the name of the link property at source node
     * @param workspace        the workspace name of the target node
     * @param linkType         the LinkTool.LinkType that determines weather an internal, external or redirect URL should be created.
     * @return the URL for the reference or NULL
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
     * Creates an external link for a given path. Context path will be added.
     *
     * <pre>
     * LinkTool.createExternalLinkForPath(node, "/resources/test.jpg") = https://www.aperto.de/author/resources/test.jpg
     * </pre>
     *
     * @param node node of the current site
     * @param path path to a resource
     * @return external link or empty string
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
     * An enumeration of link types to signal whether an internal, external or redirect link should be created.
     * Serves as a strategy for link creation methods.
     */
    public enum LinkType {
        INTERNAL(LinkUtil::createAbsoluteLink),
        EXTERNAL(LinkUtil::createExternalLink),
        REDIRECT(n -> removeStart(LinkUtil.createAbsoluteLink(n), MgnlContext.getContextPath()));

        private final Function<Node, String> _toLinkFunction;

        LinkType(Function<Node, String> toLinkFunction) {
            _toLinkFunction = toLinkFunction;
        }

        public String toLink(Node node) {
            return _toLinkFunction.apply(node);
        }
    }

    private LinkTool() {
    }
}
