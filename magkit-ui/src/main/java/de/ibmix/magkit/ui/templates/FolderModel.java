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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static info.magnolia.cms.util.RequestDispatchUtil.PERMANENT_PREFIX;
import static info.magnolia.cms.util.RequestDispatchUtil.dispatch;
import static info.magnolia.context.MgnlContext.getWebContext;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Model of folder template.
 *
 * @author diana.racho (06.11.2009)
 */
public class FolderModel extends RenderingModelImpl<ConfiguredTemplateDefinition> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FolderModel.class);

    public static final String PN_HIDE_IN_NAV = "hideInNav";
    public static final String TPL_NAME = "folder";
    public static final String TPL_NAME_SPACE = "magkit";
    public static final String TPL_REF = TPL_NAME_SPACE + ":pages/" + TPL_NAME;
    public static final String PN_REDIRECT = "redirect";

    private final TemplatingFunctions _templatingFunctions;

    @Inject
    public FolderModel(Node content, ConfiguredTemplateDefinition definition, RenderingModel<?> parent, TemplatingFunctions templatingFunctions) {
        super(content, definition, parent);

        _templatingFunctions = templatingFunctions;
    }

    @Override
    public String execute() {
        if (_templatingFunctions.isEditMode()) {
            setHideInNav();
        } else {
            sendRedirect();
        }
        return super.execute();
    }

    private void sendRedirect() {
        try {
            String path = retrieveRedirectUri(getNode());

            WebContext webContext = getWebContext();
            dispatch(PERMANENT_PREFIX + path, webContext.getRequest(), webContext.getResponse());
        } catch (RepositoryException e) {
            LOGGER.info("Error on check current page node.", e);
        }
    }

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
     * Get the redirect uri from node.
     *
     * @param node page node
     * @return redirect url
     * @throws RepositoryException repository exception
     */
    public static String retrieveRedirectUri(Node node) throws RepositoryException {
        return retrieveRedirectUri(node, false);
    }

    /**
     * Get the redirect uri from node.
     *
     * @param node       page node
     * @param asExternal uri as external or not
     * @return redirect url
     * @throws RepositoryException repository exception
     */
    public static String retrieveRedirectUri(Node node, boolean asExternal) throws RepositoryException {
        LinkTool.LinkType linkType = asExternal ? LinkTool.LinkType.EXTERNAL : LinkTool.LinkType.REDIRECT;

        String path = getRedirectTarget(node, linkType);
        if (path == null) {
            path = linkType.toLink(node.getParent());
        }
        return path;
    }

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
