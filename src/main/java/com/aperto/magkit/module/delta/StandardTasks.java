package com.aperto.magkit.module.delta;

import info.magnolia.cms.beans.config.DefaultVirtualURIMapping;
import info.magnolia.module.delta.Task;

import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrSetProperty;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static info.magnolia.cms.core.MgnlNodeType.NT_CONTENTNODE;

/**
 * Collection of standard module version handler tasks.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class StandardTasks {
    public static final String URI_MAPPING = "virtualURIMapping";

    /**
     * Creates an menu for the given module with templates, paragraphs and dialogs links.
     */
    public static Task createAdminInterfaceMenu(final String moduleName, final String moduleDisplayName) {
        return selectModuleConfig("Module Menu", "Create " + moduleDisplayName + " menue items within module adminInterface.", "adminInterface",
            addOrGetNode("config/menu/" + moduleName, NT_CONTENTNODE).then(
                addOrSetProperty("icon", "/.resources/icons/24/gears.gif"),
                addOrSetProperty("onclick", "MgnlAdminCentral.showTree('config', '/modules/" + moduleName + "')"),
                addOrSetProperty("label", moduleDisplayName),
                addOrGetNode("templates", NT_CONTENTNODE).then(
                    addOrSetProperty("icon", "/.resources/icons/16/dot.gif"),
                    addOrSetProperty("onclick", "MgnlAdminCentral.showTree('config','/modules/" + moduleName + "/pages/templates')"),
                    addOrSetProperty("label", "menu.config.templates")
                ),
                addOrGetNode("paragraphs", NT_CONTENTNODE).then(
                    addOrSetProperty("icon", "/.resources/icons/16/dot.gif"),
                    addOrSetProperty("onclick", "MgnlAdminCentral.showTree('config','/modules/" + moduleName + "/pages/components')"),
                    addOrSetProperty("label", "menu.config.paragraphs")
                ),
                addOrGetNode("dialogs", NT_CONTENTNODE).then(
                    addOrSetProperty("icon", "/.resources/icons/16/dot.gif"),
                    addOrSetProperty("onclick", "MgnlAdminCentral.showTree('config','/modules/" + moduleName + "/dialogs')"),
                    addOrSetProperty("label", "menu.config.dialogs")
                )
            )
        );
    }

    /**
     * Maps {@code /robots.txt} to {@code /docroot/moduleName/robots.txt}.
     */
    public static Task virtualUriMappingOfRobotsTxt(final String moduleName) {
        return selectModuleConfig("Virtual UriMapping", "Add virtual URI mapping for robots.txt.", moduleName,
            addOrGetNode(URI_MAPPING).then(
                addOrGetNode("robots", NT_CONTENTNODE).then(
                    addOrSetProperty("class", DefaultVirtualURIMapping.class.getName()),
                    addOrSetProperty("fromURI", "/robots.txt"),
                    addOrSetProperty("toURI", "forward:/docroot/" + moduleName + "/robots.txt"))));
    }

    /**
     * Maps {@code /favicon.ico} to {@code /docroot/moduleName/favicon.ico}.
     */
    public static Task virtualUriMappingOfFavicon(final String moduleName) {
        return selectModuleConfig("Virtual UriMapping", "Add virtual URI mapping for favicon.", moduleName,
            addOrGetNode(URI_MAPPING).then(
                addOrGetNode("favicon", NT_CONTENTNODE).then(
                    addOrSetProperty("class", DefaultVirtualURIMapping.class.getName()),
                    addOrSetProperty("fromURI", "/favicon.ico"),
                    addOrSetProperty("toURI", "forward:/docroot/" + moduleName + "/favicon.ico"))));
    }

    protected StandardTasks() {
        // hidden default constructor
    }
}