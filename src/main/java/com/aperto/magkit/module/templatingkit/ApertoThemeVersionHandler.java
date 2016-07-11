package com.aperto.magkit.module.templatingkit;

import com.aperto.magkit.module.BootstrapModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;

import java.util.List;

/**
 * Custom theme version handler.
 *
 * @author frank.sommer
 * @see ApertoThemeInstallTask
 * @since 16.04.2012 (v2.1.2)
 */
public abstract class ApertoThemeVersionHandler extends BootstrapModuleVersionHandler {

    private String _themeName;

    /**
     * Creates a task allowing to install theme files. By default the task {@link ApertoThemeInstallTask} is being used.
     */
    protected Task createThemeInstallTask(boolean isInstall) {
        return new ApertoThemeInstallTask(isInstall, getThemeFiles());
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> extraTasks = super.getExtraInstallTasks(installContext);
        extraTasks.add(createThemeInstallTask(true));
        return extraTasks;
    }

    @Override
    protected List<Task> getDefaultUpdateTasks(final Version forVersion) {
        List<Task> tasks = super.getDefaultUpdateTasks(forVersion);
        tasks.add(createThemeInstallTask(false));
        return tasks;
    }

    public String getThemeName() {
        return _themeName;
    }

    @Override
    public List<Delta> getDeltas(final InstallContext installContext, final Version from) {
        _themeName = ApertoThemeUtils.getThemeName(installContext);
        return super.getDeltas(installContext, from);
    }

    /**
     * Deliver the theme files in your ThemeVersion Handler.
     */
    protected abstract List<ThemeFileConfig> getThemeFiles();

    /**
     * Bean for the possible theme file config.
     *
     * @author frank.sommer
     */
    protected class ThemeFileConfig {
        private boolean _css;
        private String _name;
        private String _link = "";
        private String _conditionalComment;
        private String _media = "all";
        private boolean _addFingerPrint = true;

        public ThemeFileConfig(final String name, final boolean css) {
            _css = css;
            _name = name;
        }

        public String getName() {
            return _name;
        }

        public String getLink() {
            return _link;
        }

        public ThemeFileConfig withLink(final String link) {
            _link = link;
            return this;
        }

        public String getConditionalComment() {
            return _conditionalComment;
        }

        public ThemeFileConfig withConditionalComment(final String conditionalComment) {
            _conditionalComment = conditionalComment;
            return this;
        }

        public ThemeFileConfig noFingerPrint() {
            _addFingerPrint = false;
            return this;
        }

        public String getMedia() {
            return _media;
        }

        public ThemeFileConfig withMedia(final String media) {
            _media = media;
            return this;
        }

        public boolean isCss() {
            return _css;
        }

        public boolean isAddFingerPrint() {
            return _addFingerPrint;
        }
    }
}