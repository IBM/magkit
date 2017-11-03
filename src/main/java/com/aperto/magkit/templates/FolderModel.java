package com.aperto.magkit.templates;

import info.magnolia.context.MgnlContext;
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

import static info.magnolia.cms.util.RequestDispatchUtil.REDIRECT_PREFIX;
import static info.magnolia.cms.util.RequestDispatchUtil.dispatch;
import static info.magnolia.link.LinkUtil.DEFAULT_EXTENSION;
import static org.apache.commons.lang.StringUtils.isNotBlank;

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
    public static final String TPL_REF = TPL_NAME_SPACE + ":" + TPL_NAME;

    private final TemplatingFunctions _templatingFunctions;

    @Inject
    public FolderModel(Node content, ConfiguredTemplateDefinition definition, RenderingModel<?> parent, TemplatingFunctions templatingFunctions) {
        super(content, definition, parent);

        _templatingFunctions = templatingFunctions;
    }

    @Override
    public String execute() {
        if (_templatingFunctions.isEditMode()) {
            setNodeDataBoolean(PN_HIDE_IN_NAV, true);
        } else {
            sendRedirect();
        }
        return super.execute();
    }

    private void sendRedirect() {
        Node pageNode = MgnlContext.getAggregationState().getMainContentNode();
        try {
            if (pageNode != null && pageNode.getDepth() > 0) {
                Node parent = pageNode.getParent();
                if (parent != null) {
                    WebContext webContext = MgnlContext.getWebContext();
                    String path = parent.getPath();
                    if (!"/".equals(parent.getPath())) {
                        path = path + "." + DEFAULT_EXTENSION;
                    }
                    dispatch(REDIRECT_PREFIX + path, webContext.getRequest(), webContext.getResponse());
                }
            }
        } catch (RepositoryException e) {
            LOGGER.info("Can't get current page node.", e);
        }
    }

    private void setNodeDataBoolean(String propertyName, boolean propertyValue) {
        try {
            if (isNotBlank(propertyName) && !content.hasProperty(propertyName)) {
                content.setProperty(propertyName, propertyValue);
                content.getSession().save();
            }
        } catch (RepositoryException e) {
            LOGGER.info("Can't set property for folder.", e);
        }
    }
}