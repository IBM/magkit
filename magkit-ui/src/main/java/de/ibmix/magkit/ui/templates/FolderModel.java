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
import static info.magnolia.context.MgnlContext.getAggregationState;
import static info.magnolia.context.MgnlContext.getWebContext;

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
        Node pageNode = getAggregationState().getMainContentNode();
        try {
            if (pageNode != null && pageNode.getDepth() > 0) {
                Node parent = pageNode.getParent();
                if (parent != null) {
                    WebContext webContext = getWebContext();
                    String path = parent.getPath();
                    if (!"/".equals(parent.getPath())) {
                        path = LinkTool.LinkType.REDIRECT.toLink(parent);
                    }
                    dispatch(PERMANENT_PREFIX + path, webContext.getRequest(), webContext.getResponse());
                }
            }
        } catch (RepositoryException e) {
            LOGGER.info("Can't get current page node.", e);
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
}
