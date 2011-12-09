package com.aperto.magkit.module;

import info.magnolia.cms.core.ItemType;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.*;
import info.magnolia.module.model.Version;
import info.magnolia.setup.AddFilterBypassTask;
import info.magnolia.voting.voters.OnAdminVoter;
import info.magnolia.voting.voters.URIStartsWithVoter;

import java.util.ArrayList;
import java.util.List;

import static info.magnolia.cms.beans.config.ContentRepository.CONFIG;

/**
 * The MagKitModuleVersionHandler for the MagKit module.
 *
 * @author frank.sommer
 * @since 03.04.2008
 */
public class MagKitModuleVersionHandler extends BootstrapModuleVersionHandler {
    private static final String PATH_FILTER = "/server/filters";
    private static final String PATH_FILTER_VALIDATOR = PATH_FILTER + "/validator";
    private static final String PATH_I18N = "/server/i18n/content";

    private final Task _addValidatorFilterTask = new ArrayDelegateTask("Filter", "Add the Validator filter.",
            new CreateNodeTask("Validator-Filter", "Create Validator filter node", CONFIG, PATH_FILTER, "validator", ItemType.CONTENT.getSystemName()),
            new SetPropertyTask(CONFIG, PATH_FILTER_VALIDATOR, "class", "com.aperto.magkit.filter.HtmlValidatorFilter"),
            new SetPropertyTask(CONFIG, PATH_FILTER_VALIDATOR, "enabled", "true"),
            new SetPropertyTask(CONFIG, PATH_FILTER_VALIDATOR, "w3cValidatorCheckUrl", "http://validator.aperto.de/w3c-markup-validator/check"),
            new SetPropertyTask(CONFIG, PATH_FILTER_VALIDATOR, "timeOut", "15000"),
            new FilterOrderingTask("validator", new String[]{"contentType", "uriSecurity", "gzip"}));

    private final Task _addValidatorFilterBypassTask = new ArrayDelegateTask("Filter", "Add the bypass for validator filter.", new Task[]{
        new AddFilterBypassTask(PATH_FILTER_VALIDATOR, "isAdmin", OnAdminVoter.class, ""),
        new SetPropertyTask(CONFIG, PATH_FILTER_VALIDATOR + "/bypasses/isAdmin", "not", "true"),
    });

    private final Task _setI18nContentSupportTask = new ArrayDelegateTask("Filter", "Set i18n support.", new Task[]{
        new SetPropertyTask(CONFIG, PATH_I18N, "class", "com.aperto.magkit.i18n.HandleI18nContentSupport"),
        new SetPropertyTask(CONFIG, PATH_I18N, "enabled", "true")
    });

    private final Task _addBypassFor404 = new NodeExistsDelegateTask(
        "Check 404 bypass", "Check 404 bypass in server config.",
        CONFIG, PATH_FILTER + "/bypasses/404", null,
        new AddFilterBypassTask(PATH_FILTER, "404", URIStartsWithVoter.class, "/docroot/magkit")
    );

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

    /**
     * Constructor for adding update builder.
     */
    public MagKitModuleVersionHandler() {
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add(_addValidatorFilterTask);
        tasks.add(_addValidatorFilterBypassTask);
        tasks.add(_setI18nContentSupportTask);
        tasks.add(_addBypassFor404);
        tasks.add(_addBypassForDebugSuite);
        tasks.add(_addSpringByPass);
        return tasks;
    }

    @Override
    protected List<Task> getDefaultUpdateTasks(Version forVersion) {
        List<Task> updateTasks = super.getDefaultUpdateTasks(forVersion);
        updateTasks.add(_addBypassFor404);
        updateTasks.add(_addBypassForDebugSuite);
        updateTasks.add(_addSpringByPass);
        return updateTasks;
    }
}