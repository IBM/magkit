package com.aperto.magkit.module.templatingkit;

import com.aperto.magkit.urimapping.VersionNumberVirtualUriMapping;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;

import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrSetProperty;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static info.magnolia.jcr.nodebuilder.Ops.getNode;

/**
 * @author daniel.kasmeroglu@aperto.de
 *
 */
public final class ApertoThemeUtils {
  
    private ApertoThemeUtils() {
    }
  
    /**
     * Returns what we assume to be the theme module's name in the classpath:
     * "themeName" property in the module's definition, suffixed with "-theme"
     * (according to Aperto's naming conventions).
     */
    public static String getThemeName(InstallContext installContext) {
        // a bit dodgy to rely on the theme Maven module's name to end with "-theme", what would be the alternative?
        return installContext.getCurrentModuleDefinition().getProperty("themeName") + "-theme";
    }

    public static Task addVirtualUriMapping(InstallContext installContext) {
        return addVirtualUriMapping(installContext, null);
    }

    @Deprecated
    public static Task addVirtualUriMapping(InstallContext installContext, String versionPattern) {
        String pattern = VersionNumberVirtualUriMapping.GIT_PATTERN;
        if (versionPattern != null) {
            pattern = versionPattern;
        }
        String themeName = getThemeName(installContext);
        return selectModuleConfig("Virtual URI Mapping", "Maps external image, style and js uris containing version numbers to internal resources.", "magkit",
            addOrGetNode("virtualURIMapping").then(
                addOrGetNode("mapThemeFiles", NodeTypes.ContentNode.NAME).then(
                    addOrSetProperty("class", VersionNumberVirtualUriMapping.class.getName()),
                    addOrSetProperty("fromPrefix", "/.resources/" + themeName),
                    addOrSetProperty("pattern", pattern),
                    addOrSetProperty("toUri", "forward:/.resources/" + themeName + "/%s")
                )
            )
        );
    }

    /**
     * Configurates browser cache policy so that creates expiration fields within the far future (one year)
     * for all resources below the path /resources/templating-kit/themes/[moduleName].
     */
    @Deprecated
    public static Task configurateCacheModule(InstallContext installContext) {
        String themeName = getThemeName(installContext);
        return selectModuleConfig("Config cache", "Configurate cache module.", "cache",
            getNode("config/configurations/default/browserCachePolicy/policies/farFuture/voters").then(
                addOrGetNode("themeResources", NodeTypes.ContentNode.NAME).then(
                    addOrSetProperty("class", "info.magnolia.voting.voters.URIPatternVoter"),
                    addOrSetProperty("pattern", "*/.resources/" + themeName + "/*")
                )
            )
        );
    }

}
