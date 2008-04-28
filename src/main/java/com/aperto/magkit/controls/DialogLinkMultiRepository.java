package com.aperto.magkit.controls;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.dialog.DialogLink;
import info.magnolia.cms.gui.dialog.DialogUUIDLink;
import org.apache.log4j.Logger;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A link control with two buttons for different repositories.
 *
 * @author frank.sommer (08.02.2008)
 */
public class DialogLinkMultiRepository extends DialogUUIDLink {
    private static final Logger LOGGER = Logger.getLogger(DialogLinkMultiRepository.class);

    /**
     *  Dialog init.
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
        String repository = getConfigValue("secondRepository", "data");
        String tree = getConfigValue("tree", repository);
        String buttonOnClick = getConfigValue("buttonOnClick", "mgnlDialogLinkOpenBrowser('" + getName() + "','" + tree + "','" + extension + "');");
        getButton(1).setOnclick(buttonOnClick);
    }
}
