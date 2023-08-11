package com.aperto.magkit.module.templatingkit;

import com.aperto.magkit.module.templatingkit.ApertoThemeVersionHandler.ThemeFileConfig;
import com.aperto.magkit.utils.Item;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.aperto.magkit.module.delta.StandardTasks.registerThemeFile;
import static com.aperto.magkit.module.delta.StandardTasks.setupSiteTheme;
import static com.aperto.magkit.module.templatingkit.ApertoThemeUtils.getThemeName;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetContentNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.removeAllChilds;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static info.magnolia.jcr.nodebuilder.Ops.getNode;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Tasks to install all theme files in resources workspace.
 *
 * @author frank.sommer
 * @deprecated solved now in yaml by theme archetype
 */
@Deprecated
public class ApertoThemeInstallTask extends AbstractTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApertoThemeInstallTask.class);

    private boolean _isInstall;
    private List<ThemeFileConfig> _themeFiles;
    private String _versionPattern;

    public ApertoThemeInstallTask(List<ThemeFileConfig> themeFiles) {
        this(false, themeFiles, null);
    }

    public ApertoThemeInstallTask(boolean isInstall, List<ThemeFileConfig> themeFiles) {
        this(isInstall, themeFiles, null);
    }

    public ApertoThemeInstallTask(boolean isInstall, List<ThemeFileConfig> themeFiles, String version) {
        super("Install theme", "Installs theme resources in resources workspace and register theme css and js files.");
        _isInstall = isInstall;
        _themeFiles = themeFiles;
        _versionPattern = version;
    }

    @Override
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        String themeName = getThemeName(installContext);
        ModuleDefinition moduleDefinition = installContext.getCurrentModuleDefinition();
        String themeModuleName = moduleDefinition.getName();

        if (_isInstall) {
            setupSiteTheme(themeName, themeModuleName).execute(installContext);
        }

        Version currentVersion = moduleDefinition.getVersion();
        if (currentVersion == Version.UNDEFINED_DEVELOPMENT_VERSION) {
            LOGGER.warn("Deployment issue. The current development version is undefined !");
            throw new TaskExecutionException("Deployment issue. The current development version is undefined !");
        }
        String version = currentVersion.toString();
        List<Task> tasks = registerFiles(themeName, themeModuleName, version, _themeFiles);
        for (Task task : tasks) {
            task.execute(installContext);
        }

        if (_isInstall) {
            LOGGER.info("Executes theme install tasks, too.");
            addVirtualUriMapping(installContext).execute(installContext);
            configureCacheModule(installContext).execute(installContext);
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
     * This function delegates to {@link ApertoThemeUtils#configureCacheModule(InstallContext)}. It's main
     * purpose is to provide a possibility to be overridden.
     */
    protected Task configureCacheModule(InstallContext installContext) {
        return ApertoThemeUtils.configureCacheModule(installContext);
    }

    /**
     * Builds tasks for theme files registration.
     * {module.version} in link will be replaced by the current version number.
     *
     * @deprecated use {@link #registerFiles(String, String, String, List)}
     */
    @Deprecated
    protected List<Task> registerFiles(String themeName, final String version, List<ThemeFileConfig> themeFiles) {
        return registerFiles(themeName, themeName + "-theme", version, themeFiles);
    }

    /**
     * Builds tasks for theme files registration.
     * {module.version} in link will be replaced by the current version number.
     */
    protected List<Task> registerFiles(String themeName, String themeModuleName, final String version, List<ThemeFileConfig> themeFiles) {
        List<Task> registerTasks = new ArrayList<>();
        if (themeFiles != null) {
            Task cleanupTask = selectModuleConfig("Clean up theme files", "Clean up files in theme configuration", themeModuleName,
                getNode("themes/" + themeName).then(
                    addOrGetContentNode("cssFiles").then(
                        removeAllChilds()
                    ),
                    addOrGetContentNode("jsFiles").then(
                        removeAllChilds()
                    )
                )
            );
            registerTasks.add(cleanupTask);

            for (ThemeFileConfig themeFile : themeFiles) {
                Item[] propertyItems = new Item[2];
                propertyItems[0] = new Item("link", themeFile.getLink().replaceAll("\\{module.version\\}", version));
                propertyItems[1] = new Item("addFingerPrint", String.valueOf(themeFile.isAddFingerPrint()));
                if (isNotBlank(themeFile.getConditionalComment())) {
                    propertyItems = (Item[]) ArrayUtils.add(propertyItems, new Item("conditionalComment", themeFile.getConditionalComment()));
                }
                if (isNotBlank(themeFile.getMedia())) {
                    propertyItems = (Item[]) ArrayUtils.add(propertyItems, new Item("media", themeFile.getMedia()));
                }
                registerTasks.add(registerThemeFile(themeName, themeModuleName, themeFile.getName(), themeFile.isCss(), propertyItems));
            }
        }
        return registerTasks;
    }
}
