package com.aperto.magkit.module;

import com.aperto.magkit.security.AuthorFormClientCallback;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.setup.initial.AddFilterBypassTask;
import info.magnolia.voting.voters.URIStartsWithVoter;

import java.util.ArrayList;
import java.util.List;

import static com.aperto.magkit.module.delta.StandardTasks.PN_CLASS;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.*;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectServerConfig;
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
            addOrGetContentNode("stk/outputFormat").then(
                addOrSetProperty("quality", IMAGING_QUALITY)
            )
        )
    );

    /**
     * Constructor for adding update builder.
     */
    public MagkitModuleVersionHandler() {
        Task addNew404Config = new BootstrapConditionally("Check config", "Check 404 config in magkit", "/mgnl-bootstrap/install/magkit/config.modules.magkit.config.notFoundConfig.xml");
        DeltaBuilder update301 = update("3.0.1", "Updates for version 3.0.1.").addTask(addNew404Config);
        register(update301);

        DeltaBuilder update310 = update("3.1.0", "Update to Magkit 3.1.0.").addTask(
            selectServerConfig("Change FTL loader", "Change template jcr loader for supporting loading templates with extension for inplace editing.",
                getNode("rendering/freemarker/templateLoaders/jcr").then(
                    removeIfExists("extension")
                )
            )
        );
        register(update310);
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add(_addBypassForMonitoring);
        tasks.add(_addSpringByPass);
        tasks.add(_setSecurityCallback);
        tasks.add(_increaseImageQuality);
        return tasks;
    }
}