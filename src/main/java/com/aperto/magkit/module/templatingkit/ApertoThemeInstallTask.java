package com.aperto.magkit.module.templatingkit;

import com.aperto.magkit.utils.Item;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.ModuleBootstrapTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.aperto.magkit.module.delta.StandardTasks.SITE_MODULE;
import static com.aperto.magkit.module.delta.StandardTasks.registerThemeFile;
import static com.aperto.magkit.module.templatingkit.ApertoThemeUtils.getThemeName;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetContentNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.removeAllChilds;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;

import static info.magnolia.jcr.nodebuilder.Ops.getNode;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Tasks to install all theme files in resources workspace.
 *
 * @author frank.sommer
 */
public class ApertoThemeInstallTask extends AbstractTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApertoThemeInstallTask.class);
    /**
     * Set this property in your theme module to true for supporting the old binary install behaviour (without extension).
     */
    private static final String PN_OLD_BINARY_SUPPORT = "useOldBinaryInstall";

    private boolean _isInstall;
    private List<ApertoThemeVersionHandler.ThemeFileConfig> _themeFiles;
    private String _versionPattern;

    public ApertoThemeInstallTask(List<ApertoThemeVersionHandler.ThemeFileConfig> themeFiles) {
        this(false, themeFiles, null);
    }

    public ApertoThemeInstallTask(boolean isInstall, List<ApertoThemeVersionHandler.ThemeFileConfig> themeFiles) {
        this(isInstall, themeFiles, null);
    }
    
    public ApertoThemeInstallTask(boolean isInstall, List<ApertoThemeVersionHandler.ThemeFileConfig> themeFiles, String version) {
        super("Install theme", "Installs theme resources in resources workspace and register theme css and js files.");
        _isInstall = isInstall;
        _themeFiles = themeFiles;
        _versionPattern = version;
    }

    @Override
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        String themeName = getThemeName(installContext);
        ModuleDefinition moduleDefinition = installContext.getCurrentModuleDefinition();

        if (_isInstall) {
            selectModuleConfig("Create theme", "Create theme base config in Site module.", SITE_MODULE,
                addOrGetNode("config/themes", NodeTypes.Content.NAME).then(
                    addOrGetNode(themeName, NodeTypes.ContentNode.NAME).then(
                        addOrGetNode("jsFiles", NodeTypes.ContentNode.NAME),
                        addOrGetNode("cssFiles", NodeTypes.ContentNode.NAME),
                        addOrGetNode("imaging", NodeTypes.ContentNode.NAME)
                    )
                )
            ).execute(installContext);
        }

        new ModuleBootstrapTask().execute(installContext);

        Version currentVersion = moduleDefinition.getVersion();
        if (currentVersion == Version.UNDEFINED_DEVELOPMENT_VERSION) {
            LOGGER.warn("Deployment issue. The current development version is undefined !");
            throw new TaskExecutionException("Deployment issue. The current development version is undefined !");
        }
        String version = currentVersion.toString();
        List<Task> tasks = registerFiles(themeName, version, _themeFiles);
        for (Task task : tasks) {
            task.execute(installContext);
        }
        
        if (_isInstall) {
            LOGGER.info("Executes theme install tasks, too.");
            addVirtualUriMapping(installContext).execute(installContext);
            configurateCacheModule(installContext).execute(installContext);
        }
    }

    /**
     * This function delegates to {@link ApertoThemeUtils#addVirtualUriMapping(InstallContext)}. It's main
     * purpose is to provide a possibility to be overridden.
     */
    protected Task addVirtualUriMapping(InstallContext installContext) {
        return ApertoThemeUtils.addVirtualUriMapping(installContext, _versionPattern);
    }
    
    /**
     * This function delegates to {@link ApertoThemeUtils#configurateCacheModule(InstallContext)}. It's main
     * purpose is to provide a possibility to be overridden.
     */
    protected Task configurateCacheModule(InstallContext installContext) {
        return ApertoThemeUtils.configurateCacheModule(installContext);
    }

    /**
     * Builds tasks for theme files registration.
     * {module.version} in link will be replaced by the current version number.
     */
    protected List<Task> registerFiles(String themeName, final String version, List<ApertoThemeVersionHandler.ThemeFileConfig> themeFiles) {
        List<Task> registerTasks = new ArrayList<>();
        if (themeFiles != null) {
            Task cleanupTask = selectModuleConfig("Clean up theme files", "Clean up files in theme configuration", SITE_MODULE,
                getNode("config/themes/" + themeName).then(
                    addOrGetContentNode("cssFiles").then(
                        removeAllChilds()
                    ),
                    addOrGetContentNode("jsFiles").then(
                        removeAllChilds()
                    )
                )
            );
            registerTasks.add(cleanupTask);

            for (ApertoThemeVersionHandler.ThemeFileConfig themeFile : themeFiles) {
                Item[] propertyItems = new Item[2];
                propertyItems[0] = new Item("link", themeFile.getLink().replaceAll("\\{module.version\\}", version));
                propertyItems[1] = new Item("addFingerPrint", String.valueOf(themeFile.isAddFingerPrint()));
                if (isNotBlank(themeFile.getConditionalComment())) {
                    propertyItems = (Item[]) ArrayUtils.add(propertyItems, new Item("conditionalComment", themeFile.getConditionalComment()));
                }
                if (isNotBlank(themeFile.getMedia())) {
                    propertyItems = (Item[]) ArrayUtils.add(propertyItems, new Item("media", themeFile.getMedia()));
                }
                registerTasks.add(registerThemeFile(themeName, themeFile.getName(), themeFile.isCss(), propertyItems));
            }
        }
        return registerTasks;
    }
}
