package com.aperto.magkit.module;

import com.aperto.magkit.filter.HtmlValidatorFilter;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.*;
import info.magnolia.module.model.Version;
import info.magnolia.setup.AddFilterBypassTask;
import java.util.ArrayList;
import java.util.List;

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
        new NodeExistsDelegateTask("Check 404 bypass", "Check 404 bypass in server config.", ContentRepository.CONFIG, "/server/filters/bypasses/404", null, new AddFilterBypassTask("/server/filters", "404", info.magnolia.voting.voters.URIStartsWithVoter.class, "/docroot/magkit")),
    });

    private final Task _addBypassForStatus = new ArrayDelegateTask("Bypass", "Add the bypass for status output.", new Task[]{
        new NodeExistsDelegateTask("Check status bypass", "Check status bypass in server config.", ContentRepository.CONFIG, "/server/filters/bypasses/status", null, new AddFilterBypassTask("/server/filters", "status", info.magnolia.voting.voters.URIStartsWithVoter.class, "/status")),
    });

    private final Task _addCaptchaConfig = new ArrayDelegateTask("Captcha config", "Add the cache config for captcha.", new Task[]{
        new CreateNodeTask("captcha", "Create config node.", ContentRepository.CONFIG, "/modules/cache/config/URI/deny", "captcha", ItemType.CONTENTNODE.getSystemName()),
        new SetPropertyTask(ContentRepository.CONFIG, "/modules/cache/config/URI/deny/captcha", "URI", "/captcha/*"),
    });

    private final Task _checkCaptchaConfig = new ArrayDelegateTask("Captcha cache", "Add the config for the captcha cache exclusion.", new Task[]{
        new NodeExistsDelegateTask("Check cache config", "Check cache config.", ContentRepository.CONFIG, "/modules/cache/config/URI/deny/captcha", null, _addCaptchaConfig),
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
    }

    protected List getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add(_addCmsFilterBypassTask);
        tasks.add(_addValidatorFilterTask);
        tasks.add(_addValidatorFilterBypassTask);
        tasks.add(_setAdminInterfaceExportClassTask);
        tasks.add(_setI18nContentSupportTask);
        tasks.add(_addBypassFor404);
        tasks.add(_addBypassForStatus);
        tasks.add(_addCaptchaConfig);
        return tasks;
    }

    @Override
    protected List getDefaultUpdateTasks(Version forVersion) {
        List updateTasks = super.getDefaultUpdateTasks(forVersion);
        updateTasks.add(_bootstrapModuleConfigTask);
        updateTasks.add(_addBypassFor404);
        updateTasks.add(_addBypassForStatus);
        updateTasks.add(_checkCaptchaConfig);
        return updateTasks;
    }
}