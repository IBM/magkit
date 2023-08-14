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
 * @deprecated solved by yaml files in theme archetype
 */
@Deprecated
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

        /**
         * With {module.version} placeholder set no finger print. Otherwise a error message is logged.
         */
        public ThemeFileConfig noFingerPrint() {
            _addFingerPrint = false;
            return this;
        }

        /**
         * Just only for css files.
         *
         * @return css target media
         */
        public String getMedia() {
            return isCss() ? _media : null;
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
