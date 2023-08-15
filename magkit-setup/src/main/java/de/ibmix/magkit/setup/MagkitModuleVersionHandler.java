package de.ibmix.magkit.setup;

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

import de.ibmix.magkit.setup.delta.ReplaceTemplateTask;
import de.ibmix.magkit.setup.delta.StandardTasks;
import de.ibmix.magkit.setup.security.AuthorFormClientCallback;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.setup.initial.AddFilterBypassTask;
import info.magnolia.voting.voters.ExtensionVoter;
import info.magnolia.voting.voters.URIStartsWithVoter;

import java.util.ArrayList;
import java.util.List;

import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.addOrGetContentNode;
import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.addOrGetNode;
import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.addOrSetProperty;
import static de.ibmix.magkit.setup.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static de.ibmix.magkit.setup.nodebuilder.task.NodeBuilderTaskFactory.selectServerConfig;
import static info.magnolia.jcr.nodebuilder.Ops.getNode;
import static info.magnolia.jcr.nodebuilder.Ops.setProperty;
import static info.magnolia.module.delta.DeltaBuilder.update;
import static info.magnolia.repository.RepositoryConstants.CONFIG;

/**
 * The MagKitModuleVersionHandler for the MagKit module.
 *
 * @author frank.sommer
 * @since 03.04.2008
 */
public class MagkitModuleVersionHandler extends BootstrapModuleVersionHandler {
    private static final String PATH_FILTER = "/server/filters";
    private static final long IMAGING_QUALITY = 95L;

    private final Task _addBypassForMonitoring = new NodeExistsDelegateTask("Check monitoring bypass", "Check monitoring bypass in server config.", CONFIG, PATH_FILTER + "/bypasses/monitoring", null,
        new AddFilterBypassTask(PATH_FILTER, "monitoring", URIStartsWithVoter.class, "/monitoring")
    );

    private final Task _setSecurityCallback = selectServerConfig("Change callback", "Set the author form client callback.",
        getNode("filters/securityCallback/clientCallbacks/form").then(
            setProperty(StandardTasks.PN_CLASS, AuthorFormClientCallback.class.getName())
        )
    );

    private final Task _increaseImageQuality = selectModuleConfig("Increase image quality", "Increase imaging rendering quality to " + IMAGING_QUALITY + "%.", "imaging",
        addOrGetNode("config/generators").then(
            addOrGetContentNode("mte/outputFormat").then(
                addOrSetProperty("quality", IMAGING_QUALITY)
            )
        )
    );

    private final Task _disableRangeForPdf = selectServerConfig("Disable range for PDF", "Disable range support for PDF - IE (9, 10, 11) cannot handle big PDF > 4 MB sent in chunks",
        getNode("filters/range/bypasses").then(
            addOrGetContentNode("pdf").then(
                addOrSetProperty(StandardTasks.PN_CLASS, ExtensionVoter.class.getName()),
                addOrSetProperty("allow", "pdf")
            )
        )
    );

    /**
     * Constructor for adding update builder.
     */
    public MagkitModuleVersionHandler() {
        Task addNew404Config = new BootstrapConditionally("Check config", "Check 404 config in magkit", "/mgnl-bootstrap/install/magkit/config.modules.magkit.config.notFoundConfig.xml");
        DeltaBuilder update313 = update("3.1.3", "Updates for version 3.1.3.").addTask(addNew404Config);
        register(update313);

        DeltaBuilder update314 = update("3.1.4", "Updates for version 3.1.4.").addTask(_disableRangeForPdf);
        register(update314);

        final Task moveFolderTemplate = new ReplaceTemplateTask("magkit-stk:pages/folder", "magkit:pages/folder");
        DeltaBuilder update320 = update("3.2.0", "Update to Magkit 3.2.0.").addTask(moveFolderTemplate);
        register(update320);
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> installTasks = new ArrayList<>(super.getExtraInstallTasks(installContext));
        installTasks.add(_addBypassForMonitoring);
        installTasks.add(_setSecurityCallback);
        installTasks.add(_increaseImageQuality);
        installTasks.add(_disableRangeForPdf);
        return installTasks;
    }
}
