package com.aperto.magkit.utils;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static info.magnolia.jcr.util.NodeTypes.Component;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

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
                Node node = jcrSession.getNodeByIdentifier(identifier);
                path = node.getPath();
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

    private NodeUtils() {
    }
}
