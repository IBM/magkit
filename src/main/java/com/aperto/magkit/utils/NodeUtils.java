package com.aperto.magkit.utils;

import info.magnolia.context.MgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

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

    private NodeUtils() {
    }
}
