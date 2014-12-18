package com.aperto.magkit.module.delta;

import com.aperto.magkit.filter.ExtendedMultipartRequestFilter;
import com.aperto.magkit.filter.SecureRedirectFilter;
import com.aperto.magkit.filter.TemplateNameVoter;

import info.magnolia.cms.beans.config.DefaultVirtualURIMapping;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.voting.voters.URIStartsWithVoter;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.aperto.magkit.filter.ExtendedMultipartRequestFilter.DEFAULT_MAX_SIZE;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.*;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectServerConfig;
import static info.magnolia.cms.core.MgnlNodeType.NT_CONTENTNODE;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

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
    public static final String PN_FROM_URI = "fromURI";
    public static final String PN_TO_URI = "toURI";
    public static final String PN_PATTERN = "pattern";
    public static final String PN_EXTENDS = "extends";
    public static final String PN_ICON = "icon";
    public static final String ICON_DOT = "/.resources/icons/16/dot.gif";
    public static final String ICON_GEARS = "/.resources/icons/24/gears.gif";
    public static final String PN_ROLES = "roles";
    public static final String NN_PERMISSIONS = "permissions";
    private static final String NN_WORKFLOW = "workflow";

    /**
     * Maps {@code /robots.txt} to {@code /docroot/moduleName/robots.txt}.
     */
    public static Task virtualUriMappingOfRobotsTxt(final String moduleName) {
        return selectModuleConfig("Virtual UriMapping", "Add virtual URI mapping for robots.txt.", moduleName,
            addOrGetNode(URI_MAPPING).then(
                addOrGetNode("robots", NodeTypes.ContentNode.NAME).then(
                    addOrSetProperty(PN_CLASS, DefaultVirtualURIMapping.class.getName()),
                    addOrSetProperty(PN_FROM_URI, "/robots.txt"),
                    addOrSetProperty(PN_TO_URI, "forward:/docroot/" + moduleName + "/robots.txt")
                )
            )
        );
    }

    /**
     * Maps {@code /favicon.ico} to {@code /docroot/moduleName/favicon.ico}.
     */
    public static Task virtualUriMappingOfFavicon(final String moduleName) {
        return selectModuleConfig("Virtual UriMapping", "Add virtual URI mapping for favicon.", moduleName,
            addOrGetNode(URI_MAPPING).then(
                addOrGetNode("favicon", NodeTypes.ContentNode.NAME).then(
                    addOrSetProperty(PN_CLASS, DefaultVirtualURIMapping.class.getName()),
                    addOrSetProperty(PN_FROM_URI, "/favicon.ico"),
                    addOrSetProperty(PN_TO_URI, "forward:/docroot/" + moduleName + "/favicon.ico")
                )
            )
        );
    }

    /**
     * Task for installing the secure redirect filter in the magnolia filter chain.
     * Including a default configuration for stkForm template.
     *
     * @see SecureRedirectFilter
     */
    public static Task secureRedirectFilter() {
        return new ArrayDelegateTask("Install secure redirect", "Install secure redirect filter in filter chain.",
            selectServerConfig("Add filter node", "Add filter node to chain.",
                addOrGetNode("filters/cms/secure-redirect").then(
                    addOrSetProperty(PN_CLASS, SecureRedirectFilter.class.getName()),
                    addOrSetProperty(PN_ENABLED, Boolean.TRUE),
                    addOrGetNode("secure", NodeTypes.ContentNode.NAME).then(
                        addOrGetNode("template_de", NodeTypes.ContentNode.NAME).then(
                            addOrGetNode("templates", NodeTypes.ContentNode.NAME).then(
                                addOrSetProperty("form", "standard-templating-kit:pages/stkForm")
                            ),
                            addOrSetProperty(PN_CLASS, TemplateNameVoter.class.getName()),
                            addOrSetProperty("rootPath", "/de")
                        )
                    ),
                    orderBefore("secure-redirect", "intercept")
                )
            )
        );
    }

    /**
     * Task for configuring the extended multi part filter.
     *
     * @param maxRequestSize set the max request set setting, if empty 50MB will be set
     */
    public static Task multiPartFilter(final String maxRequestSize) {
        return selectServerConfig("Configuring filter", "Configuring Multipart request filter",
            getNode("filters/multipartRequest").then(
                addOrSetProperty(PN_CLASS, ExtendedMultipartRequestFilter.class.getName()),
                addOrSetProperty(PN_ENABLED, Boolean.TRUE),
                addOrSetProperty("maxRequestSize", isBlank(maxRequestSize) ? DEFAULT_MAX_SIZE : maxRequestSize),
                addOrGetNode("useSystemDefault", NodeTypes.ContentNode.NAME).then(
                    addOrGetNode("magnoliaUri", NodeTypes.ContentNode.NAME).then(
                        addOrSetProperty(PN_CLASS, URIStartsWithVoter.class.getName()),
                        addOrSetProperty(PN_PATTERN, "/.magnolia")
                    )
                )
            )
        );
    }

    /**
     * Task to add an apps to the app launcher.
     *
     * @param groupName Name of the apps group on app launcher
     * @param color     color value of the group, if empty no color will be set. E.g. #cccccc
     * @param permanent group is permanent (on top) or collapsed (on bottom)
     * @param appNames  names of the single apps
     */
    public static Task addAppsToLauncher(final String groupName, final String color, final boolean permanent, final String... appNames) {
        List<NodeOperation> appsOperations = new ArrayList<NodeOperation>();
        for (String appName : appNames) {
            appsOperations.add(addOrGetContentNode(appName));
        }

        return selectModuleConfig("Add apps to " + groupName, "Add apps to app launcher to group: " + groupName, "ui-admincentral",
            getNode("config/appLauncherLayout/groups").then(
                addOrGetContentNode(groupName).then(
                    isNotEmpty(color) ? addOrSetProperty("color", color) : noop(),
                    addOrSetProperty("permanent", Boolean.toString(permanent)),
                    addOrGetContentNode("apps").then(
                        appsOperations.toArray(new NodeOperation[appsOperations.size()])
                    )
                )
            )
        );
    }

    /**
     * Task to add several workspaces to the cache flush policy.
     *
     * @param workspaces workspace names
     * @return Task to execute
     */
    public static Task addWorkspaceToCacheFlush(final String... workspaces) {
        List<NodeOperation> setWorkspaceOps = getSetPropertyOps(workspaces);

        return selectModuleConfig("Add entries to flushAll policy", "Add flushAll policy for " + ArrayUtils.toString(workspaces), "cache",
            getNode("config/configurations/default/flushPolicy/policies/flushAll/repositories").then(
                setWorkspaceOps.toArray(new NodeOperation[setWorkspaceOps.size()])
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
                        rolesOps.toArray(new NodeOperation[rolesOps.size()])
                    )
                )
            )
        );
    }

    private static List<NodeOperation> getSetPropertyOps(final String[] roles) {
        List<NodeOperation> rolesOps = new ArrayList<NodeOperation>();
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

        return selectModuleConfig("Add applauncher group permission", "Add app group permission for " + groupName + " with roles " + ArrayUtils.toString(roles), "ui-admincentral",
            getNode("config/appLauncherLayout/groups").then(
                addOrGetContentNode(groupName).then(
                    addOrGetContentNode(NN_PERMISSIONS).then(
                        removeOthers ? removeIfExists(PN_ROLES) : noop(),
                        addOrGetContentNode(PN_ROLES).then(
                            rolesOps.toArray(new NodeOperation[rolesOps.size()])
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
            getNode("config/configurations/default").then(
                getNode("cachePolicy/voters/urls/excludes").then(
                    addCacheConfigEntry(name, urlStartsWith)
                ),
                getNode("browserCachePolicy/policies/dontCachePages/voters").then(
                    addCacheConfigEntry(name, urlStartsWith)
                )
            )
        );
    }

    private static NodeOperation addCacheConfigEntry(final String nodeName, final String startsWithPattern) {
        return addOrGetContentNode(nodeName).then(
            addOrSetProperty(PN_CLASS, URIStartsWithVoter.class.getName()),
            addOrSetProperty(PN_PATTERN, startsWithPattern)
        );
    }

    /**
     * Set for activation and deactivation the simple workflow.
     *
     * @return task to execute
     */
    public static Task setSimpleWorkflow() {
        return selectModuleConfig("Configure simple workflow", "Set simple workflow for activate and deactivate.", NN_WORKFLOW,
            getNode("commands/" + NN_WORKFLOW).then(
                getNode("activate/activate").then(
                    setProperty(NN_WORKFLOW, "simpleActivate")
                ),
                getNode("deactivate/deactivate").then(
                    setProperty(NN_WORKFLOW, "simpleDeactivate")
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

    private StandardTasks() {
        // hidden default constructor
    }
}