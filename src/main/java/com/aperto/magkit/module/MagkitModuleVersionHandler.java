package com.aperto.magkit.module;

import com.aperto.magkit.security.AuthorFormClientCallback;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.setup.initial.AddFilterBypassTask;
import info.magnolia.voting.voters.URIStartsWithVoter;

import java.util.ArrayList;
import java.util.List;

import static com.aperto.magkit.module.delta.StandardTasks.PN_CLASS;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetContentNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrSetProperty;
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

    private final Task _addBypassForMonitoring = new NodeExistsDelegateTask("Check monitoring bypass", "Check monitoring bypass in server config.", CONFIG, PATH_FILTER + "/bypasses/monitoring", null,
        new AddFilterBypassTask(PATH_FILTER, "monitoring", URIStartsWithVoter.class, "/monitoring/")
    );

    private final Task _addSpringByPass = new NodeExistsDelegateTask("Check spring bypass", "Check spring bypass in server config.", CONFIG, PATH_FILTER + "/cms/bypasses/spring", null,
        new AddFilterBypassTask(PATH_FILTER + "/cms", "spring", URIStartsWithVoter.class, "/service/")
    );

    private final Task _setSecurityCallback = selectServerConfig("Change callback", "Set the author form client callback.",
        getNode("filters/securityCallback/clientCallbacks/form").then(
            setProperty(PN_CLASS, AuthorFormClientCallback.class.getName())
        )
    );

    private final Task _addNew404Config = selectModuleConfig("Add new config", "Add new 404 error config.", "magkit",
        addOrGetContentNode("config").then(
            addOrGetContentNode("notFoundConfig").then(
                addOrSetProperty("default", "/404"),
                addOrGetContentNode("errorMappings").then(
                    addOrGetContentNode("en").then(
                        addOrSetProperty("siteName", "default"),
                        addOrSetProperty("locale", "en"),
                        addOrSetProperty("errorPath", "/en/404")
                    )
                )
            )
        )
    );

    /**
     * Constructor for adding update builder.
     */
    public MagkitModuleVersionHandler() {
        DeltaBuilder update301 = update("3.0.1", "Updates for version 3.0.1.");
        update301.addTask(_addNew404Config);
        register(update301);
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add(_addBypassForMonitoring);
        tasks.add(_addSpringByPass);
        tasks.add(_setSecurityCallback);
        tasks.add(_addNew404Config);
        return tasks;
    }

    @Override
    protected List<Task> getDefaultUpdateTasks(Version forVersion) {
        List<Task> updateTasks = super.getDefaultUpdateTasks(forVersion);
        updateTasks.add(_addBypassForMonitoring);
        updateTasks.add(_addSpringByPass);
        updateTasks.add(_setSecurityCallback);
        return updateTasks;
    }
}