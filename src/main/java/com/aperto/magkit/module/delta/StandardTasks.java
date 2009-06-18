package com.aperto.magkit.module.delta;

import static com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.addNode;
import static com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.replaceNode;
import static com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.selectConfig;
import static com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.setProperty;
import info.magnolia.module.delta.Task;

/**
 * Collection of standard module version handler tasks.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class StandardTasks {

    private static final String PATH_URI_MAPPING = "/modules/adminInterface/virtualURIMapping";

    /**
     * Creates an menu for the given module with templates, paragraphs and dialogs links.
     */
    public static Task createAdminInterfaceMenu(final String moduleName, final String moduleDisplayName) {
        return selectConfig("AdminInterface Menu",
            "Create " + moduleDisplayName + " menue items within module adminInterface.",
            "/modules/adminInterface/config/menu",
            replaceNode(moduleName,
                setProperty("icon", "/.resources/icons/24/gears.gif"),
                setProperty("onclick", "MgnlAdminCentral.showTree('config', '/modules/" + moduleName + "')"),
                setProperty("label", moduleDisplayName),
                addNode("templates",
                    setProperty("icon", "/.resources/icons/16/dot.gif"),
                    setProperty("onclick", "MgnlAdminCentral.showTree('config','/modules/" + moduleName + "/templates')"),
                    setProperty("label", "menu.config.templates")),
                addNode("paragraphs",
                    setProperty("icon", "/.resources/icons/16/dot.gif"),
                    setProperty("onclick", "MgnlAdminCentral.showTree('config','/modules/" + moduleName + "/paragraphs')"),
                    setProperty("label", "menu.config.paragraphs")),
                addNode("dialogs",
                    setProperty("icon", "/.resources/icons/16/dot.gif"),
                    setProperty("onclick", "MgnlAdminCentral.showTree('config','/modules/" + moduleName + "/dialogs')"),
                    setProperty("label", "menu.config.dialogs"))));
    }

    /**
     * Maps {@code /robots.txt} to {@code /docroot/moduleName-module/robots.txt}.
     */
    public static Task virtualUriMappingOfRobotsTxt(final String moduleName) {
        return selectConfig("Virtual UriMapping", "Add virtual URI mapping for robots.txt.", PATH_URI_MAPPING,
            replaceNode("robots",
                setProperty("class", "info.magnolia.cms.beans.config.DefaultVirtualURIMapping"),
                setProperty("fromURI", "/robots.txt"),
                setProperty("toURI", "forward:/docroot/" + moduleName + "-module/robots.txt")));
    }

    /**
     * Maps {@code /favicon.ico} to {@code /docroot/moduleName-module/favicon.ico}.
     */
    public static Task virtualUriMappingOfFavicon(final String moduleName) {
        return selectConfig("Virtual UriMapping", "Add virtual URI mapping for favicon.", PATH_URI_MAPPING,
            replaceNode("favicon",
                setProperty("class", "info.magnolia.cms.beans.config.DefaultVirtualURIMapping"),
                setProperty("fromURI", "/favicon.ico"),
                setProperty("toURI", "forward:/docroot/" + moduleName + "-module/favicon.ico")));
    }

    protected StandardTasks() {
        // hidden default constructor
    }
}