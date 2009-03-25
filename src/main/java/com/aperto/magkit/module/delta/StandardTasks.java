package com.aperto.magkit.module.delta;

import static com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.createConfigNode;
import static com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.withProperty;
import static com.aperto.magkit.module.delta.CreateConfigNodeTreeTask.withSubNode;
import info.magnolia.module.delta.Task;

/**
 * Collection of standard module version handler tasks.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class StandardTasks {

    /**
     * Creates an menu for the given module with templates, paragraphs and dialogs links.
     */
    public static Task createAdminInterfaceMenu(final String moduleName, final String moduleDisplayName) {
        return createConfigNode("AdminInterface Menu",
            "Create " + moduleDisplayName + " menue items within module adminInterface.",
            "/modules/adminInterface/config/menu", moduleName,
            withProperty("icon", "/.resources/icons/24/gears.gif"),
            withProperty("onclick", "MgnlAdminCentral.showTree('config', '/modules/" + moduleName + "')"),
            withProperty("label", moduleDisplayName),
            withSubNode("templates",
                withProperty("icon", "/.resources/icons/16/dot.gif"),
                withProperty("onclick", "MgnlAdminCentral.showTree('config','/modules/" + moduleName + "/templates')"),
                withProperty("label", "menu.config.templates")),
            withSubNode("paragraphs",
                withProperty("icon", "/.resources/icons/16/dot.gif"),
                withProperty("onclick", "MgnlAdminCentral.showTree('config','/modules/" + moduleName + "/paragraphs')"),
                withProperty("label", "menu.config.paragraphs")),
            withSubNode("dialogs",
                withProperty("icon", "/.resources/icons/16/dot.gif"),
                withProperty("onclick", "MgnlAdminCentral.showTree('config','/modules/" + moduleName + "/dialogs')"),
                withProperty("label", "menu.config.dialogs")));
    }

    protected StandardTasks() {
        // hidden default constructor
    }
}
