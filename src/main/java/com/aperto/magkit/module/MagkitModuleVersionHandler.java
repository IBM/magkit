package com.aperto.magkit.module;

import static com.aperto.magkit.module.delta.StandardTasks.PN_CLASS;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetContentNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrSetProperty;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.removeIfExists;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectServerConfig;
import static info.magnolia.jcr.nodebuilder.Ops.getNode;
import static info.magnolia.jcr.nodebuilder.Ops.setProperty;
import static info.magnolia.module.delta.DeltaBuilder.update;
import static info.magnolia.repository.RepositoryConstants.CONFIG;

import java.util.ArrayList;
import java.util.List;

import com.aperto.magkit.module.delta.ReplaceTemplateTask;
import com.aperto.magkit.security.AuthorFormClientCallback;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.setup.initial.AddFilterBypassTask;
import info.magnolia.voting.voters.ExtensionVoter;
import info.magnolia.voting.voters.URIStartsWithVoter;

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

    private final Task _addSpringByPass = new NodeExistsDelegateTask("Check spring bypass", "Check spring bypass in server config.", CONFIG, PATH_FILTER + "/cms/bypasses/spring", null,
        new AddFilterBypassTask(PATH_FILTER + "/cms", "spring", URIStartsWithVoter.class, "/service/")
    );

    private final Task _setSecurityCallback = selectServerConfig("Change callback", "Set the author form client callback.",
        getNode("filters/securityCallback/clientCallbacks/form").then(
            setProperty(PN_CLASS, AuthorFormClientCallback.class.getName())
        )
    );

    private final Task _increaseImageQuality = selectModuleConfig("Increase image quality", "Increase imaging rendering quality to " + IMAGING_QUALITY + "%.", "imaging",
        addOrGetNode("config/generators").then(
            addOrGetContentNode("mte/outputFormat").then(
                addOrSetProperty("quality", IMAGING_QUALITY)
            )
        )
    );

    private final Task _setTemplateLoaderConfig = selectServerConfig("Change FTL loader", "Change template jcr loader for supporting loading templates with extension for inplace editing.",
        getNode("rendering/freemarker/templateLoaders/jcr").then(
            removeIfExists("extension")
        )
    );

    private final Task _disableRangeForPdf = selectServerConfig("Disable range for PDF", "Disable range support for PDF - IE (9, 10, 11) cannot handle big PDF > 4 MB sent in chunks",
        getNode("filters/range/bypasses").then(
            addOrGetContentNode("pdf").then(
                addOrSetProperty(PN_CLASS, ExtensionVoter.class.getName()),
                addOrSetProperty("deny", "pdf")
            )
        )
    );

    private final Task _moveFolderTemplate = new ReplaceTemplateTask("magkit-stk:pages/folder", "magkit:pages/folder");

    /**
     * Constructor for adding update builder.
     */
    public MagkitModuleVersionHandler() {
        Task addNew404Config = new BootstrapConditionally("Check config", "Check 404 config in magkit", "/mgnl-bootstrap/install/magkit/config.modules.magkit.config.notFoundConfig.xml");

        DeltaBuilder update314 = update("3.1.4", "Updates for version 3.1.4.").addTask(_disableRangeForPdf);
        register(update314);

        DeltaBuilder update313 = update("3.1.3", "Updates for version 3.1.3.").addTask(addNew404Config);
        register(update313);

        DeltaBuilder update310 = update("3.1.0", "Update to Magkit 3.1.0.").addTask(_setTemplateLoaderConfig);
        register(update310);

        DeltaBuilder update320 = update("3.2.0", "Update to Magkit 3.2.0.").addTask(_moveFolderTemplate);
        register(update320);
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> installTasks = new ArrayList<Task>();
        installTasks.addAll(super.getExtraInstallTasks(installContext));
        installTasks.add(_addBypassForMonitoring);
        installTasks.add(_addSpringByPass);
        installTasks.add(_setSecurityCallback);
        installTasks.add(_setTemplateLoaderConfig);
        installTasks.add(_increaseImageQuality);
        installTasks.add(_disableRangeForPdf);
        installTasks.add(_moveFolderTemplate);
        return installTasks;
    }
}