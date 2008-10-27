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
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.ModuleBootstrapTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PropertyValueDelegateTask;
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
    private static final String PATH_FILTER = "/server/filters";
    private static final String PATH_FILTER_CMS = PATH_FILTER + "/cms";
    private static final String PATH_FILTER_VALIDATOR = PATH_FILTER + "/validator";
    private static final String PATH_I18N = "/server/i18n/content";
    private static final String PATH_FILTER_MULTIPART = PATH_FILTER + "/multipartRequest";
    private static final String CLASS_MAGKIT_MP_FILTER = "com.aperto.magkit.filter.CosMultipartRequestFilter";
    private static final String PATH_CACHE_DENY_35 = "/modules/cache/config/URI/deny";
    private static final String PATH_CACHE_EXCLUDE_36 = "/modules/cache/config/configurations/default/cachePolicy/voters/urls/excludes";
    private static final String PATH_CACHE_CAPTCHA_35 = PATH_CACHE_DENY_35 + "/captcha";
    private static final String PATH_CACHE_DEBUG_35 = PATH_CACHE_DENY_35 + "/debug";
    private static final String PATH_CACHE_CAPTCHA_36 = PATH_CACHE_EXCLUDE_36 + "/captcha";
    private static final String PATH_CACHE_DEBUG_36 = PATH_CACHE_EXCLUDE_36 + "/debug";

    private final Task _addCmsFilterBypassTask = new ArrayDelegateTask("Filter", "Add bypasses for filter 'cms'", new Task[]{
        new AddFilterBypassTask(PATH_FILTER_CMS, "captcha", info.magnolia.voting.voters.URIStartsWithVoter.class, "/service/captcha"),
        new AddFilterBypassTask(PATH_FILTER_CMS, "core", info.magnolia.voting.voters.URIStartsWithVoter.class, "/core"),
        new AddFilterBypassTask(PATH_FILTER_CMS, "magkit", info.magnolia.voting.voters.URIStartsWithVoter.class, "/magkit")
    });
    private final Task _addValidatorFilterTask = new ArrayDelegateTask("Filter", "Add the Validator filter.", new Task[]{
        new CreateNodeTask("Validator-Filter", "Create Validator filter node", ContentRepository.CONFIG, PATH_FILTER, "validator", ItemType.CONTENT.getSystemName()),
        new SetPropertyTask(ContentRepository.CONFIG, PATH_FILTER_VALIDATOR, "class", "com.aperto.magkit.filter.HtmlValidatorFilter"),
        new SetPropertyTask(ContentRepository.CONFIG, PATH_FILTER_VALIDATOR, "enabled", "true"),
        new CreateNodeTask("Validator-Filter config", "Create config node for validator filter node", ContentRepository.CONFIG, PATH_FILTER_VALIDATOR, "config", ItemType.CONTENTNODE.getSystemName()),
        new SetPropertyTask(ContentRepository.CONFIG, PATH_FILTER_VALIDATOR + "/config", HtmlValidatorFilter.W3C_VALIDATOR_CHECK_URL_PARAM_NAME, "http://validator.aperto.de/w3c-markup-validator/check"),
        new FilterOrderingTask("validator", new String[]{"contentType", "uriSecurity"})
    });

    private final Task _addValidatorFilterBypassTask = new ArrayDelegateTask("Filter", "Add the bypass for validator filter.", new Task[]{
        new AddFilterBypassTask(PATH_FILTER_VALIDATOR, "isAdmin", info.magnolia.voting.voters.OnAdminVoter.class, ""),
        new SetPropertyTask(ContentRepository.CONFIG, PATH_FILTER_VALIDATOR + "/bypasses/isAdmin", "not", "true"),
        new AddFilterBypassTask(PATH_FILTER_VALIDATOR, "magkit", info.magnolia.voting.voters.URIStartsWithVoter.class, "/magkit"),
    });

    private final Task _setAdminInterfaceExportClassTask = new SetPropertyTask(
        ContentRepository.CONFIG,
        "/modules/adminInterface/pages/export",
        "class",
        "com.aperto.magkit.export.ExportPageAlphabetically"
    );

    private final Task _setI18nContentSupportTask = new ArrayDelegateTask("Filter", "Set i18n support.", new Task[]{
        new SetPropertyTask(ContentRepository.CONFIG, PATH_I18N, "class", "com.aperto.magkit.i18n.HandleI18nContentSupport"),
        new SetPropertyTask(ContentRepository.CONFIG, PATH_I18N, "enabled", "true")
    });

    private final Task _checkMultpartFilter = new PropertyValueDelegateTask(
        "Check multipart filter",
        "Check multipart filter setting in server config.",
        ContentRepository.CONFIG,
        PATH_FILTER_MULTIPART,
        "class",
        CLASS_MAGKIT_MP_FILTER,
        true,
        null,
        new SetPropertyTask(ContentRepository.CONFIG, PATH_FILTER_MULTIPART, "class", CLASS_MAGKIT_MP_FILTER)
    );

    private final Task _addBypassFor404 = new NodeExistsDelegateTask(
        "Check 404 bypass",
        "Check 404 bypass in server config.",
        ContentRepository.CONFIG,
        PATH_FILTER + "/bypasses/404",
        null,
        new AddFilterBypassTask(PATH_FILTER, "404", info.magnolia.voting.voters.URIStartsWithVoter.class, "/docroot/magkit")
    );

    private final Task _addBypassForStatus = new NodeExistsDelegateTask(
        "Check status bypass",
        "Check status bypass in server config.",
        ContentRepository.CONFIG,
        PATH_FILTER + "/bypasses/status",
        null,
        new AddFilterBypassTask(PATH_FILTER, "status", info.magnolia.voting.voters.URIStartsWithVoter.class, "/status")
    );

    private final Task _addBypassForDebugSuite = new NodeExistsDelegateTask(
        "Check debug suite bypass", "Check debug suite bypass in server config.",
        ContentRepository.CONFIG, PATH_FILTER + "/bypasses/debug", null,
        new AddFilterBypassTask(PATH_FILTER, "debug", info.magnolia.voting.voters.URIStartsWithVoter.class, "/debug/")
    );

    private final Task _addCacheConfig35 = new ArrayDelegateTask("Captcha config", "Add the cache config for captcha.", new Task[]{
        new CreateNodeTask("captcha", "Create cache config node.", ContentRepository.CONFIG, PATH_CACHE_DENY_35, "captcha", ItemType.CONTENTNODE.getSystemName()),
        new SetPropertyTask(ContentRepository.CONFIG, PATH_CACHE_CAPTCHA_35, "URI", "/service/captcha/*"),
        new CreateNodeTask("debug", "Create cache config node.", ContentRepository.CONFIG, PATH_CACHE_DENY_35, "debug", ItemType.CONTENTNODE.getSystemName()),
        new SetPropertyTask(ContentRepository.CONFIG, PATH_CACHE_DEBUG_35, "URI", "/debug/*"),
    });

    private final Task _addCacheConfig36 = new ArrayDelegateTask("Captcha config", "Add the cache config for captcha.", new Task[]{
        new CreateNodeTask("captcha", "Create cache config node.", ContentRepository.CONFIG, PATH_CACHE_EXCLUDE_36, "captcha", ItemType.CONTENTNODE.getSystemName()),
        new SetPropertyTask(ContentRepository.CONFIG, PATH_CACHE_CAPTCHA_36, "pattern", "/service/captcha/*"),
        new SetPropertyTask(ContentRepository.CONFIG, PATH_CACHE_CAPTCHA_36, "class", "info.magnolia.voting.voters.URIStartsWithVoter"),
        new CreateNodeTask("debug", "Create cache config node.", ContentRepository.CONFIG, PATH_CACHE_EXCLUDE_36, "debug", ItemType.CONTENTNODE.getSystemName()),
        new SetPropertyTask(ContentRepository.CONFIG, PATH_CACHE_DEBUG_36, "pattern", "/debug/*"),
        new SetPropertyTask(ContentRepository.CONFIG, PATH_CACHE_DEBUG_36, "class", "info.magnolia.voting.voters.URIStartsWithVoter"),
    });

    private final Task _checkCacheConfig36 = new NodeExistsDelegateTask(
        "Check cache config",
        "Check cache config.",
        ContentRepository.CONFIG,
        PATH_CACHE_CAPTCHA_36,
        null,
        _addCacheConfig36
    );

    private final Task _checkCacheConfig35 = new NodeExistsDelegateTask(
        "Check cache config",
        "Check cache config.",
        ContentRepository.CONFIG,
        PATH_CACHE_DENY_35,
        null,
        _addCacheConfig35
    );

    private final Task _checkCacheVersion = new NodeExistsDelegateTask(
        "Check cache version",
        "Check cache version.",
        ContentRepository.CONFIG,
        PATH_CACHE_DENY_35,
        _checkCacheConfig35,
        _checkCacheConfig36
    );

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
        tasks.add(_addBypassForDebugSuite);
        tasks.add(_checkCacheVersion);
        tasks.add(_checkMultpartFilter);
        return tasks;
    }

    @Override
    protected List getDefaultUpdateTasks(Version forVersion) {
        List updateTasks = super.getDefaultUpdateTasks(forVersion);
        updateTasks.add(_bootstrapModuleConfigTask);
        updateTasks.add(_addBypassFor404);
        updateTasks.add(_addBypassForStatus);
        updateTasks.add(_addBypassForDebugSuite);
        updateTasks.add(_checkCacheVersion);
        updateTasks.add(_checkMultpartFilter);
        return updateTasks;
    }
}