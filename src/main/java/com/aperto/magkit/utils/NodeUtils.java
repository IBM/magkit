package com.aperto.magkit.utils;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collections;
import java.util.List;

import static info.magnolia.jcr.util.NodeTypes.Component;
import static info.magnolia.jcr.util.NodeTypes.Page;
import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang.StringUtils.*;

/**
 * Util class for handling nodes ({@link Node}).
 *
 * @author frank.sommer
 * @since 26.03.13
 */
public final class NodeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeUtils.class);

    /**
     * Determines the path to given workspace and node identifier. Uses the jcr session from {@link MgnlContext}.
     *
     * @param workspace Workspace for node identifier lookup
     * @param identifier Node identifier fka. uuid
     * @return Node path
     */
    public static String getPathForIdentifier(String workspace, String identifier) {
        String path = null;
        if (isNotEmpty(workspace) && isNotEmpty(identifier)) {
            try {
                Session jcrSession = MgnlContext.getJCRSession(workspace);
                if (jcrSession != null) {
                    Node node = jcrSession.getNodeByIdentifier(identifier);
                    path = node.getPath();
                }
            } catch (RepositoryException e) {
                LOGGER.info("Can't get path to node. Error message was {}.", e.getLocalizedMessage());
            }
        }
        return path;
    }

    /**
     * Gets a node by identifier from website workspace.
     * @see #getNodeByIdentifier(String, String)
     */
    public static Node getNodeByIdentifier(String identifier) {
        return getNodeByIdentifier(WEBSITE, identifier);
    }

    /**
     * Gets a node by identifier from given workspace.
     * Catch the exception from magnolia NodeUtil.
     *
     * @see NodeUtil#getNodeByIdentifier(String, String)
     */
    public static Node getNodeByIdentifier(String workspace, String identifier) {
        Node node = null;
        try {
            node = NodeUtil.getNodeByIdentifier(workspace, identifier);
        } catch (RepositoryException e) {
            LOGGER.info("Error getting node with id {} in {}.", identifier, workspace);
            LOGGER.debug(e.getLocalizedMessage(), e);
        }
        return node;
    }

    /**
     * Retrieves from given node the child page nodes.
     *
     * @param pageNode page node
     * @return list of child pages, fallback empty list
     */
    public static List<Node> getChildPages(Node pageNode) {
        List<Node> childPages = Collections.emptyList();

        if (pageNode != null) {
            try {
                childPages = NodeUtil.asList(NodeUtil.getNodes(pageNode, Page.NAME));
            } catch (RepositoryException e) {
                LOGGER.info("Error getting child page nodes of page: {}.", getPathIfPossible(pageNode));
                LOGGER.debug(e.getLocalizedMessage(), e);
            }
        }

        return childPages;
    }

    /**
     * Checks an area for sub components.
     * @param node a page or component node to check
     * @param areaName name of the area node
     * @return true, if the area contains components.
     */
    public static boolean hasSubComponents(Node node, String areaName) {
        boolean hasSubComponents = false;
        if (node != null && isNotBlank(areaName)) {
            try {
                if (node.hasNode(areaName)) {
                    Node areaNode = node.getNode(areaName);
                    Iterable<Node> nodes = NodeUtil.getNodes(areaNode, Component.NAME);
                    hasSubComponents = nodes.iterator().hasNext();
                }
            } catch (RepositoryException e) {
                LOGGER.warn("Error on checking for sub components.", e);
            }
        }
        return hasSubComponents;
    }

    /**
     * Delivers the template of the given node.
     *
     * @param node node to check
     * @return template name or empty string
     */
    public static String getTemplate(@Nullable Node node) {
        String template = EMPTY;
        try {
            if (node != null) {
                template = defaultString(NodeTypes.Renderable.getTemplate(node));
            }
        } catch (RepositoryException e) {
            LOGGER.info("Unable to get template id from node {}.", getPathIfPossible(node), e);
        }
        return template;
    }

    private NodeUtils() {
    }
}
