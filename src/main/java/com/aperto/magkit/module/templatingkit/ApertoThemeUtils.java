package com.aperto.magkit.module.templatingkit;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
 * @deprecated use cases are now configured by yaml in theme archetype
 */
@Deprecated
public final class ApertoThemeUtils {

    private ApertoThemeUtils() {
    }

    /**
     * Returns the value of the "themeName" property in the module's definition.
     */
    public static String getThemeName(InstallContext installContext) {
        return installContext.getCurrentModuleDefinition().getProperty("themeName");
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
                ),
                addOrSetProperty("op", "OR")
            )
        );
    }
}
