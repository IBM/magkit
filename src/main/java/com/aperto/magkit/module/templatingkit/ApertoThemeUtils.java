package com.aperto.magkit.module.templatingkit;

import com.aperto.magkit.urimapping.VersionNumberVirtualUriMapping;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.voting.voters.URIPatternVoter;

import static com.aperto.magkit.module.delta.StandardTasks.PN_CLASS;
import static com.aperto.magkit.module.delta.StandardTasks.PN_PATTERN;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrSetProperty;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static info.magnolia.jcr.nodebuilder.Ops.getNode;

/**
 * @author daniel.kasmeroglu@aperto.de
 */
public final class ApertoThemeUtils {

    private ApertoThemeUtils() {
    }

    /**
     * Returns what we assume to be the theme module's name in the classpath:
     * "themeName" property in the module's definition, suffixed with "-theme"
     * (according to Aperto's naming conventions).
     *
     * @deprecated theme should configured in your own theme module, so we need no theme name property anymore
     */
    @Deprecated
    public static String getThemeName(InstallContext installContext) {
        // a bit dodgy to rely on the theme Maven module's name to end with "-theme", what would be the alternative?
        return installContext.getCurrentModuleDefinition().getProperty("themeName") + "-theme";
    }

    public static Task addVirtualUriMapping(InstallContext installContext) {
        return addVirtualUriMapping(installContext, null);
    }

    public static Task addVirtualUriMapping(InstallContext installContext, String versionPattern) {
        String pattern = VersionNumberVirtualUriMapping.GIT_PATTERN;
        if (versionPattern != null) {
            pattern = versionPattern;
        }
        String themeModuleName = installContext.getCurrentModuleDefinition().getName();
        return selectModuleConfig("Virtual URI Mapping", "Maps external image, style and js uris containing version numbers to internal resources.", "magkit",
            addOrGetNode("virtualURIMapping").then(
                addOrGetNode("mapThemeFiles", NodeTypes.ContentNode.NAME).then(
                    addOrSetProperty(PN_CLASS, VersionNumberVirtualUriMapping.class.getName()),
                    addOrSetProperty("fromPrefix", "/.resources/" + themeModuleName),
                    addOrSetProperty(PN_PATTERN, pattern),
                    addOrSetProperty("toUri", "forward:/.resources/" + themeModuleName + "/%s")
                )
            )
        );
    }

    /**
     * Configures browser cache policy so that creates expiration fields within the far future (one year)
     * for all resources below the path /.resources/[moduleName].
     */
    public static Task configureCacheModule(InstallContext installContext) {
        String themeModuleName = installContext.getCurrentModuleDefinition().getName();
        return selectModuleConfig("Config cache", "Configure cache module.", "cache",
            getNode("config/contentCaching/defaultPageCache/browserCachePolicy/policies/farFuture/voters").then(
                addOrGetNode("themeResources", NodeTypes.ContentNode.NAME).then(
                    addOrSetProperty(PN_CLASS, URIPatternVoter.class.getName()),
                    addOrSetProperty(PN_PATTERN, "*/.resources/" + themeModuleName + "/*")
                )
            )
        );
    }
}
