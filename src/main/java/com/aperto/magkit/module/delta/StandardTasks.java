package com.aperto.magkit.module.delta;

import com.aperto.magkit.filter.SecureRedirectFilter;
import info.magnolia.cms.beans.config.DefaultVirtualURIMapping;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.Task;

import static com.aperto.magkit.nodebuilder.NodeOperationFactory.*;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectServerConfig;
import static info.magnolia.cms.core.MgnlNodeType.NT_CONTENTNODE;

/**
 * Collection of standard module version handler tasks.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public final class StandardTasks {
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
                addOrGetNode("pages", NT_CONTENTNODE).then(
                    addOrSetProperty("icon", "/.resources/icons/16/dot.gif"),
                    addOrSetProperty("onclick", "MgnlAdminCentral.showTree('config','/modules/" + moduleName + "/templates/pages')"),
                    addOrSetProperty("label", "menu.config.templates")
                ),
                addOrGetNode("components", NT_CONTENTNODE).then(
                    addOrSetProperty("icon", "/.resources/icons/16/dot.gif"),
                    addOrSetProperty("onclick", "MgnlAdminCentral.showTree('config','/modules/" + moduleName + "/templates/components')"),
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

    /**
     * Task for installing the secure redirect filter in the magnolia filter chain.
     * @see SecureRedirectFilter
     */
    public static Task secureRedirectFilter() {
        return new ArrayDelegateTask("Install secure redirect", "Install secure redirect filter in filter chain.",
            selectServerConfig("Add filter node", "Add filter node to chain.",
                addOrGetNode("filters/cms/secure-redirect").then(
                    addOrSetProperty("class", SecureRedirectFilter.class.getName()),
                    addOrSetProperty("enabled", true),
                    addOrGetNode("secure", NT_CONTENTNODE).then(
                        addOrGetNode("template_de", NT_CONTENTNODE).then(
                            addOrGetNode("templates", NT_CONTENTNODE).then(
                                addOrSetProperty("form", "standard-templating-kit:pages/stkForm")
                            )
                        )
                    ),
                    orderBefore("secure-redirect", "intercept")
                )
            )
        );
    }

    private StandardTasks() {
        // hidden default constructor
    }
}