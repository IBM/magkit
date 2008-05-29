package com.aperto.magkit.module;

import java.util.ArrayList;
import java.util.List;

import com.aperto.magkit.filter.HtmlValidatorFilter;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.ModuleBootstrapTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.setup.AddFilterBypassTask;

/**
 * The MagKitModuleVersionHandler for the MagKit module.
 *
 * @author frank.sommer
 *         Date: 03.04.2008
 */
public class MagKitModuleVersionHandler extends DefaultModuleVersionHandler {

    private final Task _addCmsFilterBypassTask = new ArrayDelegateTask("Filter", "Add bypasses for filter 'cms'", new Task[]{
        new AddFilterBypassTask("/server/filters/cms", "debug", info.magnolia.voting.voters.URIStartsWithVoter.class, "/debug"),
        new AddFilterBypassTask("/server/filters/cms", "captcha", info.magnolia.voting.voters.URIStartsWithVoter.class, "/captcha"),
        new AddFilterBypassTask("/server/filters/cms", "core", info.magnolia.voting.voters.URIStartsWithVoter.class, "/core"),
        new AddFilterBypassTask("/server/filters/cms", "magkit", info.magnolia.voting.voters.URIStartsWithVoter.class, "/magkit")
    });
    private final Task _addValidatorFilterTask = new ArrayDelegateTask("Filter", "Add the Validator filter.", new Task[]{
        new CreateNodeTask("Validator-Filter", "Create Validator filter node", ContentRepository.CONFIG, "/server/filters", "validator", ItemType.CONTENT.getSystemName()),
        new SetPropertyTask(ContentRepository.CONFIG, "/server/filters/validator", "class", "com.aperto.magkit.filter.HtmlValidatorFilter"),
        new SetPropertyTask(ContentRepository.CONFIG, "/server/filters/validator", "enabled", "true"),
        new CreateNodeTask("Validator-Filter config", "Create config node for validator filter node", ContentRepository.CONFIG, "/server/filters/validator", "config", ItemType.CONTENTNODE.getSystemName()),
        new SetPropertyTask(ContentRepository.CONFIG, "/server/filters/validator/config", HtmlValidatorFilter.W3C_VALIDATOR_CHECK_URL_PARAM_NAME, "http://validator.aperto.de/w3c-markup-validator/check"),
        new FilterOrderingTask("validator", new String[]{"contentType", "uriSecurity"})
    });

    private final Task _addValidatorFilterBypassTask = new ArrayDelegateTask("Filter", "Add the bypass for validator filter.", new Task[]{
        new AddFilterBypassTask("/server/filters/validator", "isAdmin", info.magnolia.voting.voters.OnAdminVoter.class, ""),
        new SetPropertyTask(ContentRepository.CONFIG, "/server/filters/validator/bypasses/isAdmin", "not", "true"),
        new AddFilterBypassTask("/server/filters/validator", "magkit", info.magnolia.voting.voters.URIStartsWithVoter.class, "/magkit"),
    });

    private final Task _setAdminInterfaceExportClassTask = new SetPropertyTask(ContentRepository.CONFIG, "/modules/adminInterface/pages/export", "class", "com.aperto.magkit.export.ExportPageAlphabetically");
    private final Task _setI18nContentSupportTask = new ArrayDelegateTask("Filter", "Set i18n support.", new Task[]{
        new SetPropertyTask(ContentRepository.CONFIG, "/server/i18n/content", "class", "com.aperto.magkit.i18n.HandleI18nContentSupport"),
        new SetPropertyTask(ContentRepository.CONFIG, "/server/i18n/content", "enabled", "true")
    });

    private final Task _addBypassFor404 = new ArrayDelegateTask("Bypass", "Add the bypass for 404 redirect.", new Task[]{
        new NodeExistsDelegateTask("Check 404 bypass", "Check 404 bypass in server config.", "config", "/server/filters/bypasses/404", null, new AddFilterBypassTask("/server/filters", "404", info.magnolia.voting.voters.URIStartsWithVoter.class, "/docroot/magkit")),
    });

    private final Task _add404Config = new ArrayDelegateTask("Bypass", "Add the bypass for 404 redirect.", new Task[]{
        new CreateNodeTask("Config node", "Create config node.", "config", "/modules/magkit", "config", ItemType.CONTENT.getSystemName()),
        new CreateNodeTask("404 node", "Create config node for 404.", "config", "/modules/magkit/config", "404", ItemType.CONTENT.getSystemName()),
        new SetPropertyTask(ContentRepository.CONFIG, "/modules/magkit/config/404", "handle", "/content/de.html"),
    });

    private final Task _check404Config = new ArrayDelegateTask("Bypass config", "Add the config for the 404 redirect.", new Task[]{
        new NodeExistsDelegateTask("Check 404 config", "Check 404 config in magkit.", "config", "/server/filters/bypasses/404", _add404Config),
    });

    /**
     * this is used to bootstrap the new module-specific templates, dialogs ... .
     * bootstraps everything from "mgnl-bootstrap" folder.
     */
    private final Task _bootstrapModuleConfigTask = new ModuleBootstrapTask();

    /**
     * Constructor for adding update builder.
     */
    public MagKitModuleVersionHandler() {
        DeltaBuilder builder001 = DeltaBuilder.update("0.0.3", "Upgrading to Magkit 0.0.3");
        builder001.addTask(_addBypassFor404);
        builder001.addTask(_check404Config);
        register(builder001);
    }

    protected List getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add(_addCmsFilterBypassTask);
        tasks.add(_addValidatorFilterTask);
        tasks.add(_addValidatorFilterBypassTask);
        tasks.add(_setAdminInterfaceExportClassTask);
        tasks.add(_setI18nContentSupportTask);
        tasks.add(_addBypassFor404);
        tasks.add(_add404Config);
        return tasks;
    }

    @Override
    protected List getDefaultUpdateTasks(Version forVersion) {
        List updateTasks = super.getDefaultUpdateTasks(forVersion);
        updateTasks.add(_bootstrapModuleConfigTask);
        return updateTasks;
    }
}