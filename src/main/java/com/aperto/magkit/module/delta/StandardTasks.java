package com.aperto.magkit.module.delta;

import com.aperto.magkit.utils.Item;
import com.aperto.magkit.workflow.AutoApproveHumanTaskWorkItemHandlerDefinition;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.virtualuri.mapping.DefaultVirtualUriMapping;
import info.magnolia.voting.voters.URIStartsWithVoter;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetContentNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrSetProperty;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addPatternVoter;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.removeIfExists;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectConfig;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static info.magnolia.jcr.nodebuilder.Ops.addNode;
import static info.magnolia.jcr.nodebuilder.Ops.getNode;
import static info.magnolia.jcr.nodebuilder.Ops.noop;
import static info.magnolia.jcr.nodebuilder.Ops.setProperty;

/**
 * Collection of standard module version handler tasks.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public final class StandardTasks {
    private static final Logger LOGGER = LoggerFactory.getLogger(StandardTasks.class);

    public static final String URI_MAPPING = "virtualURIMapping";
    public static final String PN_CLASS = "class";
    public static final String PN_IMPL_CLASS = "implementationClass";
    public static final String PN_ENABLED = "enabled";
    public static final String PN_FROM_URI = "fromUri";
    public static final String PN_TO_URI = "toUri";
    public static final String PN_PATTERN = "pattern";
    public static final String PN_EXTENDS = "extends";
    public static final String PN_ICON = "icon";
    public static final String PN_ROLES = "roles";
    public static final String NN_PERMISSIONS = "permissions";
    public static final String NN_CONFIG = "config";
    private static final String NN_WORKFLOW = "workflow";

    /**
     * @deprecated STK module should not be used anymore.
     */
    @Deprecated
    public static final String STK_MODULE = "standard-templating-kit";

    /**
     * Name of the magnolia site module.
     */
    public static final String SITE_MODULE = "site";

    /**
     * Maps {@code /robots.txt} to {@code /.resources/moduleName/robots.txt}.
     */
    public static Task virtualUriMappingOfRobotsTxt(final String moduleName) {
        return selectModuleConfig("Virtual UriMapping", "Add virtual URI mapping for robots.txt.", moduleName,
            addOrGetNode(URI_MAPPING).then(
                addOrGetNode("robots", NodeTypes.ContentNode.NAME).then(
                    addOrSetProperty(PN_CLASS, DefaultVirtualUriMapping.class.getName()),
                    addOrSetProperty(PN_FROM_URI, "/robots.txt"),
                    addOrSetProperty(PN_TO_URI, "forward:/.resources/" + moduleName + "/robots.txt")
                )
            )
        );
    }

    /**
     * Maps {@code /favicon.ico} to {@code /.resources/moduleName/favicon.ico}.
     */
    public static Task virtualUriMappingOfFavicon(final String moduleName) {
        return selectModuleConfig("Virtual UriMapping", "Add virtual URI mapping for favicon.", moduleName,
            addOrGetNode(URI_MAPPING).then(
                addOrGetNode("favicon", NodeTypes.ContentNode.NAME).then(
                    addOrSetProperty(PN_CLASS, DefaultVirtualUriMapping.class.getName()),
                    addOrSetProperty(PN_FROM_URI, "/favicon.ico"),
                    addOrSetProperty(PN_TO_URI, "forward:/.resources/" + moduleName + "/favicon.ico")
                )
            )
        );
    }

    /**
     * Task to add apps to the app launcher.
     *
     * @param groupName Name of the apps group on app launcher
     * @param color     color value of the group, if empty no color will be set. E.g. #cccccc
     * @param permanent group is permanent (on top) or collapsed (on bottom)
     * @param appNames  names of the single apps
     * @deprecated use {@link #addAppsToLauncher(String, String...)}
     */
    @Deprecated
    public static Task addAppsToLauncher(final String groupName, final String color, final boolean permanent, final String... appNames) {
        return addAppsToLauncher(groupName, appNames);
    }

    /**
     * Task to add apps to the app launcher.
     *
     * @param groupName Name of the apps group on app launcher
     * @param appNames  names of the single apps
     */
    public static Task addAppsToLauncher(final String groupName, final String... appNames) {
        List<NodeOperation> appsOperations = new ArrayList<>();
        for (String appName : appNames) {
            appsOperations.add(addOrGetContentNode(appName));
        }

        return selectModuleConfig("Add apps to " + groupName, "Add apps to app launcher to group: " + groupName, "ui-admincentral",
            getNode("config/appLauncherLayout/groups").then(
                addOrGetContentNode(groupName).then(
                    addOrGetContentNode("apps").then(
                        appsOperations.toArray(new NodeOperation[0])
                    )
                )
            )
        );
    }

    /**
     * Task to add a several roles to the app permissions config.
     *
     * @param module       module name
     * @param appName      app name to reconfigure
     * @param removeOthers remove all existing roles
     * @param roles        roles to set
     * @return Task to execute
     */
    public static Task addAppRolesPermission(final String module, final String appName, final boolean removeOthers, final String... roles) {
        List<NodeOperation> rolesOps = getSetPropertyOps(roles);

        return selectModuleConfig("Add app permissions", "Add app permissions for " + appName + " with roles " + ArrayUtils.toString(roles), module,
            getNode("apps/" + appName).then(
                addOrGetContentNode(NN_PERMISSIONS).then(
                    removeOthers ? removeIfExists(PN_ROLES) : noop(),
                    addOrGetContentNode(PN_ROLES).then(
                        rolesOps.toArray(new NodeOperation[0])
                    )
                )
            )
        );
    }

    private static List<NodeOperation> getSetPropertyOps(final String[] roles) {
        List<NodeOperation> rolesOps = new ArrayList<>();
        for (String role : roles) {
            rolesOps.add(addOrSetProperty(role, role));
        }
        return rolesOps;
    }

    /**
     * Adds roles permission to an app launcher group.
     *
     * @param groupName    group name
     * @param removeOthers other roles will remove
     * @param roles        roles to configure
     * @return Task to execute
     */
    public static Task addAppLauncherGroupPermission(final String groupName, final boolean removeOthers, final String... roles) {
        List<NodeOperation> rolesOps = getSetPropertyOps(roles);

        return selectModuleConfig("Add app launcher group permission", "Add app group permission for " + groupName + " with roles " + ArrayUtils.toString(roles), "ui-admincentral",
            getNode("config/appLauncherLayout/groups").then(
                addOrGetContentNode(groupName).then(
                    addOrGetContentNode(NN_PERMISSIONS).then(
                        removeOthers ? removeIfExists(PN_ROLES) : noop(),
                        addOrGetContentNode(PN_ROLES).then(
                            rolesOps.toArray(new NodeOperation[0])
                        )
                    )
                )
            )
        );
    }

    /**
     * Adds a cache exclude config to the magnolia and to the browser cache configuration.
     *
     * @param name          name of the entry
     * @param urlStartsWith begin of the url
     * @return task to execute
     */
    public static Task addCacheExclude(final String name, final String urlStartsWith) {
        return selectModuleConfig("Add cache exclude", "Add cache exclude for " + urlStartsWith, "cache",
            getNode("config/contentCaching/defaultPageCache").then(
                getNode("cachePolicy/shouldBypassVoters/urls/excludes").then(
                    addPatternVoter(name, URIStartsWithVoter.class.getName(), urlStartsWith)
                ),
                getNode("browserCachePolicy/policies/dontCachePages/voters").then(
                    addPatternVoter(name, URIStartsWithVoter.class.getName(), urlStartsWith)
                )
            )
        );
    }

    /**
     * Set for activation and deactivation the simple workflow.
     *
     * @return task to execute
     */
    public static Task setSimpleWorkflow() {
        return new ArrayDelegateTask("Set simple workflow", "Set all configurations simple workflow without approval step.",
            selectModuleConfig("Set auto approval", "Set auto approval human task handler.", "workflow-jbpm",
                getNode("workItemHandlers/humanTask").then(
                    setProperty(PN_CLASS, AutoApproveHumanTaskWorkItemHandlerDefinition.class.getName())
                )
            ),
            selectModuleConfig("Allow retry action", "Allow retry action on failure.", NN_WORKFLOW,
                getNode("messageViews/publish/actions/retry/availability/rules/CanDeleteTaskRule").then(
                    addOrSetProperty("assignee", false)
                )
            )
        );
    }

    /**
     * Compares the versions and the revision classifier. If the version numbers are equal, a different classifier should trigger a module update.
     */
    public static boolean hasModuleNewRevision(final Version fromVersion, final Version toVersion) {
        boolean triggerUpdate = false;
        if (toVersion.isEquivalent(fromVersion)) {
            String toClassifier = toVersion.getClassifier();
            String fromClassifier = fromVersion.getClassifier();
            if (toClassifier == null && fromClassifier != null) {
                LOGGER.debug("A released version was found. Trigger module update.");
                triggerUpdate = true;
            } else if (fromClassifier != null && !fromClassifier.equals(toClassifier)) {
                LOGGER.debug("A new classifier version was found. Trigger module update.");
                triggerUpdate = true;
            }
        }
        return triggerUpdate;
    }

    /**
     * Initial theme creation.
     *
     * @param themeName       name of the theme
     * @param themeModuleName name of the theme module
     */
    public static Task setupSiteTheme(final String themeName, final String themeModuleName) {
        // don't use selectModuleConfig, because module is not yet available
        return selectConfig("Create theme", "Create theme base config in theme module.",
            addOrGetNode("modules/" + themeModuleName + "/themes").then(
                addOrGetContentNode(themeName).then(
                    addOrGetContentNode("jsFiles"),
                    addOrGetContentNode("cssFiles"),
                    addOrGetContentNode("imaging")
                )
            )
        );
    }

    /**
     * Task to register a javascript or stylesheet file in the theme configuration.
     *
     * @param themeName       name of the stk theme
     * @param themeModuleName name of the theme module
     * @param nodeName        node name of the styles entry
     * @param isCss           flag to register css or javascript
     * @param propertyItems   array of items to set as property of the styles configuration
     * @return Task to execute
     */
    public static Task registerThemeFile(final String themeName, final String themeModuleName, final String nodeName, final boolean isCss, final Item... propertyItems) {
        if (propertyItems == null) {
            throw new RuntimeException("Properties must not be empty.");
        }

        String filesPath = isCss ? "/cssFiles" : "/jsFiles";

        NodeOperation[] propertyOperations = new NodeOperation[propertyItems.length];
        for (int i = 0; i < propertyItems.length; i++) {
            Item propertyItem = propertyItems[i];
            propertyOperations[i] = addOrSetProperty(propertyItem.getKey(), propertyItem.getValue());
        }

        return selectModuleConfig("Add theme file", "Add file to theme configuration", themeModuleName,
            getNode("themes/" + themeName + filesPath).then(
                removeIfExists(nodeName),
                addNode(nodeName, NodeTypes.ContentNode.NAME).then(propertyOperations)
            )
        );
    }

    /**
     * Registers a custom templating functions class for freemarker rendering.
     *
     * @see info.magnolia.rendering.module.setup.InstallRendererContextAttributeTask
     * @deprecated use Magnolia task instead {@link info.magnolia.rendering.module.setup.InstallRendererContextAttributeTask}
     */
    @Deprecated
    public static Task registerCustomTemplatingFunctions(final String name, final String className) {
        return selectConfig("Register custom templating", "Register the " + name + " templating functions freemarker",
            getNode("modules/rendering/renderers/freemarker/contextAttributes").then(
                addContextAttributeConfig(name, className)
            )
        );
    }

    private static NodeOperation addContextAttributeConfig(final String name, final String className) {
        return addOrGetContentNode(name).then(
            addOrSetProperty("name", name),
            addOrSetProperty("componentClass", className)
        );
    }

    private StandardTasks() {
        // hidden default constructor
    }
}
