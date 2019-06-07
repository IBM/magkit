package com.aperto.magkit.utils;

import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;
import info.magnolia.config.registry.Registry;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;

/**
 * Util class for handling nodes ({@link Node}).
 *
 * @author frank.sommer
 * @since 26.03.13
 */
public final class NodeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeUtils.class);

    public static final Predicate<Node> IS_FOLDER = n -> isNodeType(n, NodeTypes.Folder.NAME);
    public static final Predicate<Node> IS_PAGE = n -> isNodeType(n, NodeTypes.Page.NAME);
    public static final Predicate<Node> IS_AREA = n -> isNodeType(n, NodeTypes.Area.NAME);
    public static final Predicate<Node> IS_COMPONENT = n -> isNodeType(n, NodeTypes.Component.NAME);
    public static final Predicate<Node> IS_CONTENT = n -> isNodeType(n, NodeTypes.Content.NAME);
    public static final Predicate<Node> IS_CONTENT_NODE = n -> isNodeType(n, NodeTypes.ContentNode.NAME);

    public static final Predicate<Node> HAS_HOME_TEMPLATE = node -> StringUtils.equals("home", getTemplateType(node));
    public static final Predicate<Node> HAS_FEATURE_TEMPLATE = node -> StringUtils.equals("feature", getTemplateType(node));
    public static final Predicate<Node> HAS_CONTENT_TEMPLATE = node -> StringUtils.equals("content", getTemplateType(node));

    /**
     * Determines the path to given workspace and node identifier.
     *
     * @param workspace  Workspace for node identifier lookup
     * @param identifier Node identifier fka. uuid
     * @return Node path
     */
    public static String getPathForIdentifier(String workspace, String identifier) {
        Node node = getNodeByIdentifier(workspace, identifier);
        return node != null ? NodeUtil.getPathIfPossible(node) : null;
    }

    /**
     * Gets a node by identifier from website workspace.
     *
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
        return NodeUtil.asList(getChildren(pageNode, IS_PAGE));
    }

    /**
     * Checks an area for sub components.
     *
     * @param node     a page or component node to check
     * @param areaName name of the area node
     * @return true, if the area contains components.
     */
    public static boolean hasSubComponents(Node node, String areaName) {
        Node child = getChildNode(node, areaName);
        return getChildren(child, IS_COMPONENT).iterator().hasNext();
    }

    public static Node getChildNode(@Nullable final Node node, @Nullable final String path) {
        Node result = null;
        if (node != null && isNotBlank(path)) {
            try {
                result = node.getNode(path);
            } catch (RepositoryException e) {
                LOGGER.warn("Unable to get child of node [{}]", getPathIfPossible(node));
                LOGGER.debug(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Delivers the template of the given node.
     *
     * @param node node to check
     * @return template name or empty string
     */
    public static String getTemplate(@Nullable final Node node) {
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

    public static String getTemplateType(@Nullable final Node node) {
        TemplateDefinition def = getTemplateDefinition(node);
        return def != null ? def.getType() : null;
    }

    public static TemplateDefinition getTemplateDefinition(@Nullable final Node node) {
        return getTemplateDefinition(getTemplate(node));
    }

    public static TemplateDefinition getTemplateDefinition(@Nullable final String templateId) {
        TemplateDefinition result = null;
        try {
            if (isNotBlank(templateId)) {
                TemplateDefinitionRegistry registry = Components.getComponent(TemplateDefinitionRegistry.class);
                result = registry.getProvider(templateId).get();
            }
        } catch (Registry.NoSuchDefinitionException e) {
            LOGGER.debug("No definition found for template id {}.", templateId, e);
        }
        return result;
    }

    public static Node getParent(@Nullable final Node node) {
        Node parent = null;
        try {
            if (node != null && node.getDepth() > 0) {
                parent = node.getParent();
            }
        } catch (RepositoryException e) {
            LOGGER.info("Unable to get parent node from [{}]", getPathIfPossible(node));
            LOGGER.debug(e.getLocalizedMessage(), e);
        }

        return parent;
    }

    /**
     * Allows {@link Node#isNodeType(String)} in a null-safe manner and catches the {@link RepositoryException}.
     * @param node node to check
     * @param nodeType the type to check
     * @return true if node has the given type
     */
    public static boolean isNodeType(@Nullable final Node node, @Nullable final String nodeType) {
        boolean isNodeType = false;
        try {
            // TODO: Use NodeUtils.isNodeType(..)? -> checks original type of frozen nodes?
            isNodeType = node != null && node.isNodeType(defaultString(nodeType));
        } catch (RepositoryException e) {
            LOGGER.info("Unable to check node type [{}] for node [{}]", nodeType, getPathIfPossible(node));
            LOGGER.debug(e.getLocalizedMessage(), e);
        }
        return isNodeType;
    }

    // Ancestors:

    /**
     * Finds the first ancestor of the given Node that matches the Predicate.
     * The input node is excluded from search.
     *
     * @param child the Node to get the ancestor for. May be NULL.
     * @param nodePredicate the Predicate to be matched. Never NULL.
     * @return the first matching ancestor node or NULL if the input node is NULL or no such ancestor exists.
     */
    public static Node getAncestor(@Nullable final Node child, @NotNull final Predicate<Node> nodePredicate) {
        return getAncestorOrSelf(getParent(child), nodePredicate);
    }

    /**
     * Finds the first ancestor of the given Node that matches the Predicate.
     * The input node is included into the search.
     *
     * @param child the Node to get the ancestor for. May be NULL.
     * @param nodePredicate the Predicate to be matched. Never NULL.
     * @return the first matching ancestor node or NULL if the input node is NULL or no such ancestor exists.
     */
    public static Node getAncestorOrSelf(@Nullable final Node child, @NotNull final Predicate<Node> nodePredicate) {
        Node result = null;
        if (child != null) {
            result = nodePredicate.test(child) ? child : getAncestorOrSelf(getParent(child), nodePredicate);
        }
        return result;
    }

    /**
     * Finds the first ancestor of the given Node with the provided template id.
     * The input node is included into the search.
     *
     * @param content the Node to get the ancestor for. May be NULL.
     * @param templateId the template id required for the ancestor. May be NULL.
     * @return the first ancestor node with the provided template id or NULL if the input node is NULL or no such ancestor exists.
     */
    public static Node getAncestorOrSelfWithTemplate(@Nullable final Node content, @Nullable final String templateId) {
        return getAncestorOrSelf(content, node -> StringUtils.equals(getTemplate(node), templateId));
    }

    /**
     * Finds the first ancestor of the given Node with the provided primary node type.
     * The input node is excluded from search.
     *
     * @param node the Node to get the ancestor for. May be NULL.
     * @param nodeType the primary node type required for the ancestor. May be NULL.
     * @return the first ancestor node having the provided primary node type or NULL if the input node is NULL or no such ancestor exists.
     */
    public static Node getAncestorWithPrimaryType(@Nullable final Node node, @Nullable final String nodeType) {
        return getAncestor(node, n -> isNodeType(n, nodeType));
    }

    /**
     * A save method to get the depth (level) of a node.
     *
     * @param node the node to get the depth for
     * @return the node level as int or -1 if node is NULL or an exception occurred.
     */
    public static int getDepth(final Node node) {
        int result = -1;
        if (node != null) {
            try {
                result = node.getDepth();
            } catch (RepositoryException e) {
                LOGGER.info("Error getting the node depth.");
                LOGGER.debug(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Collects all direct children of the given Node that match the provided predicate.
     *
     * @param node the Node to get the children for. May be NULL.
     * @param predicate the filter predicate. Never NULL.
     * @return an Iterable with all matching child nodes. May be empty but never NULL.
     */
    public static Iterable<Node> getChildren(@Nullable final Node node, @NotNull final Predicate<Node> predicate) {
        Iterable<Node> result = Collections.emptyList();
        if (node != null) {
            try {
                result = NodeUtil.getNodes(node, toJackRabbitPredicate(predicate));
            } catch (RepositoryException e) {
                LOGGER.info("Unable to get children for node [{}]", getPathIfPossible(node));
                LOGGER.debug(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Wraps a java.util.function.Predicate into an org.apache.jackrabbit.commons.predicate.Predicate.
     *
     * @param nodePredicate the java.util.function.Predicate to be wrapped. Never NULL.
     * @return a org.apache.jackrabbit.commons.predicate.Predicate that executes the input Predicate, never NULL.
     */
    public static org.apache.jackrabbit.commons.predicate.Predicate toJackRabbitPredicate(@NotNull final Predicate<Node> nodePredicate) {
        return object -> nodePredicate.test((Node) object);
    }

    private NodeUtils() {
    }
}
