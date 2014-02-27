package com.aperto.magkit.module.delta;

import com.aperto.magkit.filter.ExtendedMultipartRequestFilter;
import com.aperto.magkit.filter.SecureRedirectFilter;
import com.aperto.magkit.filter.TemplateNameVoter;
import info.magnolia.cms.beans.config.DefaultVirtualURIMapping;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.voting.voters.URIStartsWithVoter;

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
    public static final String URI_MAPPING = "virtualURIMapping";
    public static final String PN_CLASS = "class";
    public static final String PN_ENABLED = "enabled";
    public static final String PN_FROM_URI = "fromURI";
    public static final String PN_TO_URI = "toURI";
    public static final String PN_PATTERN = "pattern";
    public static final String ICON_DOT = "/.resources/icons/16/dot.gif";
    public static final String ICON_GEARS = "/.resources/icons/24/gears.gif";

    /**
     * Maps {@code /robots.txt} to {@code /docroot/moduleName/robots.txt}.
     */
    public static Task virtualUriMappingOfRobotsTxt(final String moduleName) {
        return selectModuleConfig("Virtual UriMapping", "Add virtual URI mapping for robots.txt.", moduleName,
            addOrGetNode(URI_MAPPING).then(
                addOrGetNode("robots", NT_CONTENTNODE).then(
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
                addOrGetNode("favicon", NT_CONTENTNODE).then(
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
     * @see SecureRedirectFilter
     */
    public static Task secureRedirectFilter() {
        return new ArrayDelegateTask("Install secure redirect", "Install secure redirect filter in filter chain.",
            selectServerConfig("Add filter node", "Add filter node to chain.",
                addOrGetNode("filters/cms/secure-redirect").then(
                    addOrSetProperty(PN_CLASS, SecureRedirectFilter.class.getName()),
                    addOrSetProperty(PN_ENABLED, Boolean.TRUE),
                    addOrGetNode("secure", NT_CONTENTNODE).then(
                        addOrGetNode("template_de", NT_CONTENTNODE).then(
                            addOrGetNode("templates", NT_CONTENTNODE).then(
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
                addOrGetNode("useSystemDefault", NT_CONTENTNODE).then(
                    addOrGetNode("magnoliaUri", NT_CONTENTNODE).then(
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
     * @param color color value of the group, if empty no color will be set. E.g. #cccccc
     * @param permanent group is permanent (on top) or collapsed (on bottom)
     * @param appNames names of the single apps
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

    private StandardTasks() {
        // hidden default constructor
    }
}