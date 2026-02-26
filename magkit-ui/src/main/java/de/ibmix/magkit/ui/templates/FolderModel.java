package de.ibmix.magkit.ui.templates;

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

import de.ibmix.magkit.core.utils.LinkTool;
import de.ibmix.magkit.core.utils.NodeUtils;
import info.magnolia.context.WebContext;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.templating.functions.TemplatingFunctions;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static info.magnolia.cms.util.RequestDispatchUtil.PERMANENT_PREFIX;
import static info.magnolia.cms.util.RequestDispatchUtil.dispatch;
import static info.magnolia.context.MgnlContext.getWebContext;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Rendering model for the "folder" template type. The model handles two main responsibilities:
 * 1) In edit mode it ensures the navigation hiding flag is present by setting {@link #PN_HIDE_IN_NAV} to true if missing.
 * 2) Outside edit mode it performs a permanent redirect. The redirect target is resolved from the {@link #PN_REDIRECT} property
 *    which may hold an external URL, a UUID of a target page node, or be empty. If no valid redirect target can be resolved,
 *    the parent page node is used as fallback. Dispatching uses Magnolia's permanent redirect prefix to produce an HTTP 301.
 *
 * Side effects: In edit mode the content node may be modified and saved; in view mode a redirect is dispatched which short-circuits normal rendering.
 * Null/Error handling: Repository access errors are caught and logged; on errors the model falls back to default behavior (no redirect or property set).
 * Thread-safety: Instances are request-scoped (non thread-safe); the static helper methods are stateless and thread-safe.
 * Usage example:
 * <pre>
 * FolderModel model = new FolderModel(node, definition, parentModel, templatingFunctions);
 * String outcome = model.execute();
 * </pre>
 *
 * @author diana.racho@ibmix.de
 * @since 2009-11-06
 */
public class FolderModel extends RenderingModelImpl<ConfiguredTemplateDefinition> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FolderModel.class);

    public static final String PN_HIDE_IN_NAV = "hideInNav";
    public static final String TPL_NAME = "folder";
    public static final String TPL_NAME_SPACE = "magkit";
    public static final String TPL_REF = TPL_NAME_SPACE + ":pages/" + TPL_NAME;
    public static final String PN_REDIRECT = "redirect";

    private final TemplatingFunctions _templatingFunctions;

    /**
     * Creates a new folder template model instance.
     *
     * @param content the current page node representing the folder (must not be null)
     * @param definition the template definition
     * @param parent the parent rendering model (may be null)
     * @param templatingFunctions Magnolia templating functions helper
     */
    @Inject
    public FolderModel(Node content, ConfiguredTemplateDefinition definition, RenderingModel<?> parent, TemplatingFunctions templatingFunctions) {
        super(content, definition, parent);

        _templatingFunctions = templatingFunctions;
    }

    /**
     * Executes the model: In edit mode sets the hideInNav property if absent; otherwise performs a permanent redirect
     * to the resolved target. Falls back to parent page when no redirect target set. Returns the outcome of the parent implementation
     * after running side effects.
     *
     * @return the outcome string from the super implementation
     */
    @Override
    public String execute() {
        if (_templatingFunctions.isEditMode()) {
            setHideInNav();
        } else {
            sendRedirect();
        }
        return super.execute();
    }

    /**
     * Resolve redirect target and dispatch a permanent redirect. Logs and ignores repository errors.
     */
    private void sendRedirect() {
        try {
            String path = retrieveRedirectUri(getNode());

            WebContext webContext = getWebContext();
            dispatch(PERMANENT_PREFIX + path, webContext.getRequest(), webContext.getResponse());
        } catch (RepositoryException e) {
            LOGGER.info("Error on check current page node.", e);
        }
    }

    /**
     * Ensure the folder is hidden in navigation by setting {@link #PN_HIDE_IN_NAV} to true if not already present.
     * Logs but suppresses repository errors.
     */
    private void setHideInNav() {
        try {
            if (!content.hasProperty(PN_HIDE_IN_NAV)) {
                content.setProperty(PN_HIDE_IN_NAV, true);
                content.getSession().save();
            }
        } catch (RepositoryException e) {
            LOGGER.info("Can't set hideInNav property for folder.", e);
        }
    }

    /**
     * Get the redirect URI from a page node. Delegates to {@link #retrieveRedirectUri(Node, boolean)} with external flag false.
     *
     * @param node page node (must not be null)
     * @return redirect URL, never null; falls back to parent link when no target provided
     * @throws RepositoryException on repository access issues during resolution
     */
    public static String retrieveRedirectUri(Node node) throws RepositoryException {
        return retrieveRedirectUri(node, false);
    }

    /**
     * Get the redirect URI from a page node. The redirect target is read from {@link #PN_REDIRECT}. Supported formats:
     * external URL, UUID of a target page, or empty. If unresolved the parent page link is returned. When {@code asExternal}
     * is true, an external link representation is produced.
     *
     * @param node page node (must not be null)
     * @param asExternal produce external link instead of internal redirect link
     * @return resolved redirect URL (never null)
     * @throws RepositoryException on repository access issues during resolution
     */
    public static String retrieveRedirectUri(Node node, boolean asExternal) throws RepositoryException {
        LinkTool.LinkType linkType = asExternal ? LinkTool.LinkType.EXTERNAL : LinkTool.LinkType.REDIRECT;

        String path = getRedirectTarget(node, linkType);
        if (path == null) {
            path = linkType.toLink(node.getParent());
        }
        return path;
    }

    /**
     * Internal helper that attempts to resolve a redirect target based on {@link #PN_REDIRECT} property value.
     * Returns null when no valid target can be derived.
     *
     * @param node current page node
     * @param linkType target link type transformation
     * @return redirect target or null when unresolved
     */
    private static String getRedirectTarget(Node node, LinkTool.LinkType linkType) {
        String redirectTarget = null;

        final String redirectValue = getString(node, PN_REDIRECT, EMPTY);
        if (LinkTool.isExternalLink(redirectValue)) {
            redirectTarget = redirectValue;
        } else if (LinkTool.isUuid(redirectValue)) {
            final Node redirectNode = NodeUtils.getNodeByIdentifier(WEBSITE, redirectValue);
            if (redirectNode != null) {
                redirectTarget = linkType.toLink(redirectNode);
            }
        }

        return redirectTarget;
    }
}
