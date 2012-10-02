package com.aperto.magkit.module;

import com.aperto.magkit.security.AuthorFormClientCallback;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.setup.initial.AddFilterBypassTask;
import info.magnolia.voting.voters.URIStartsWithVoter;

import java.util.ArrayList;
import java.util.List;

import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectServerConfig;
import static info.magnolia.nodebuilder.Ops.getNode;
import static info.magnolia.nodebuilder.Ops.setProperty;
import static info.magnolia.repository.RepositoryConstants.CONFIG;

/**
 * The MagKitModuleVersionHandler for the MagKit module.
 *
 * @author frank.sommer
 * @since 03.04.2008
 */
public class MagKitModuleVersionHandler extends BootstrapModuleVersionHandler {
    private static final String PATH_FILTER = "/server/filters";

    private final Task _addBypassForDebugSuite = new NodeExistsDelegateTask(
        "Check debug suite bypass", "Check debug suite bypass in server config.",
        CONFIG, PATH_FILTER + "/bypasses/debug", null,
        new AddFilterBypassTask(PATH_FILTER, "debug", URIStartsWithVoter.class, "/debug/")
    );

    private final Task _addSpringByPass = new NodeExistsDelegateTask(
        "Check spring bypass", "Check spring bypass in server config.",
        CONFIG, PATH_FILTER + "/cms/bypasses/spring", null,
        new AddFilterBypassTask(PATH_FILTER + "/cms", "spring", URIStartsWithVoter.class, "/service/")
    );

    private final Task _setSecurityCallback = selectServerConfig("Change callback", "Set the author form client callback.",
        getNode("filters/securityCallback/clientCallbacks/form").then(
            setProperty("class", AuthorFormClientCallback.class.getName())
        )
    );

    /**
     * Constructor for adding update builder.
     */
    public MagKitModuleVersionHandler() {
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add(_addBypassForDebugSuite);
        tasks.add(_addSpringByPass);
        tasks.add(_setSecurityCallback);
        return tasks;
    }

    @Override
    protected List<Task> getDefaultUpdateTasks(Version forVersion) {
        List<Task> updateTasks = super.getDefaultUpdateTasks(forVersion);
        updateTasks.add(_addBypassForDebugSuite);
        updateTasks.add(_addSpringByPass);
        updateTasks.add(_setSecurityCallback);
        return updateTasks;
    }
}