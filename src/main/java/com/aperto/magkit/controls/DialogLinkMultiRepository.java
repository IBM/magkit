package com.aperto.magkit.controls;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.dialog.DialogUUIDLink;
import info.magnolia.link.LinkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static info.magnolia.context.MgnlContext.getHierarchyManager;
import static info.magnolia.context.MgnlContext.getParameter;
import static info.magnolia.link.LinkUtil.convertUUIDtoHandle;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * A link control with two buttons for different repositories.
 *
 * @author frank.sommer (08.02.2008)
 */
public class DialogLinkMultiRepository extends DialogUUIDLink {
    private static final Logger LOGGER = LoggerFactory.getLogger(DialogLinkMultiRepository.class);
    private static final String SECOND_REPOSITORY = "secondRepository";

    /**
     * Dialog init.
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode) throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        getButtons().add(new Button());
    }

    /**
     * Dialog html init.
     */
    protected void doBeforeDrawHtml() {
        super.doBeforeDrawHtml();

        // settings for the 2nd button
        String extension = getConfigValue("extension");
        getButton().setLabel(getMessage("dialog.link.internal.2nd"));
        getButton(1).setLabel(getMessage("dialog.link.internal"));
        getButton(1).setSaveInfo(false);
        String repository = getConfigValue(SECOND_REPOSITORY, "data");
        String tree = getConfigValue("tree", repository);
        String buttonOnClick = getConfigValue("buttonOnClick", "mgnlDialogLinkOpenBrowser('" + getName() + "','" + tree + "','" + extension + "');");
        getButton(1).setOnclick(buttonOnClick);
    }

    /**
     * Gets repository path.
     *
     * @return Current repository path.
     * @see info.magnolia.cms.gui.dialog.UUIDDialogControl#getRepository()
     */
    public String getRepository() {
        String repository = super.getRepository();
        if (getParameter(getName()) != null) {
            String value = getParameter(getName());
            if (!getHierarchyManager(repository).isExist(value)) {
                repository = getConfigValue(SECOND_REPOSITORY, "data");
            }
        } else if (getStorageNode() != null) {
            String value = readValue();
            String handle = null;
            try {
                handle = convertUUIDtoHandle(value, repository);
            } catch (LinkException e) {
                LOGGER.info("Error converting link {}.", value, e);
            }
            if (isBlank(handle)) {
                repository = getConfigValue(SECOND_REPOSITORY, "data");
            }
        }

        return repository;
    }
}