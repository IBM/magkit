package com.aperto.magkit.module;

import com.aperto.webkit.utils.ExceptionEater;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.*;
import info.magnolia.module.model.Version;
import info.magnolia.setup.AddFilterBypassTask;
import info.magnolia.voting.voters.OnAdminVoter;
import info.magnolia.voting.voters.URIStartsWithVoter;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static info.magnolia.cms.beans.config.ContentRepository.CONFIG;

/**
 * The MagKitModuleVersionHandler for the MagKit module.
 *
 * @author frank.sommer
 *         Date: 03.04.2008
 */
public class MagKitModuleVersionHandler extends DefaultModuleVersionHandler {
    private static final String PATH_FILTER = "/server/filters";
    private static final String PATH_FILTER_VALIDATOR = PATH_FILTER + "/validator";
    private static final String PATH_I18N = "/server/i18n/content";
    private static final String PATH_CACHE_EXCLUDE = "/modules/cache/config/configurations/default/cachePolicy/voters/urls/excludes";
    private static final String PATH_CACHE_CAPTCHA = PATH_CACHE_EXCLUDE + "/captcha";
    private static final String PATH_CACHE_DEBUG = PATH_CACHE_EXCLUDE + "/debug";

    private static final String PROPERTY_KEY = "environment";
    private static final String PROPERTY_VALUE_PRODUCTION_ENVIRONMENT = "production";

    private static final String[] ALLOWED_ENVIRONMENT_VALUES = {"local", "testing", PROPERTY_VALUE_PRODUCTION_ENVIRONMENT, "presentation"};

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
        "Check 404 bypass",
        "Check 404 bypass in server config.",
        CONFIG,
        PATH_FILTER + "/bypasses/404",
        null,
        new AddFilterBypassTask(PATH_FILTER, "404", URIStartsWithVoter.class, "/docroot/magkit")
    );

    private final Task _addBypassForStatus = new NodeExistsDelegateTask(
        "Check status bypass",
        "Check status bypass in server config.",
        CONFIG,
        PATH_FILTER + "/bypasses/status",
        null,
        new AddFilterBypassTask(PATH_FILTER, "status", URIStartsWithVoter.class, "/status")
    );

    private final Task _addBypassForDebugSuite = new NodeExistsDelegateTask(
        "Check debug suite bypass", "Check debug suite bypass in server config.",
        CONFIG, PATH_FILTER + "/bypasses/debug", null,
        new AddFilterBypassTask(PATH_FILTER, "debug", URIStartsWithVoter.class, "/debug/")
    );

    private final Task _addCacheConfig = new ArrayDelegateTask("Captcha config", "Add the cache config for captcha.",
            new CreateNodeTask("captcha", "Create cache config node.", CONFIG, PATH_CACHE_EXCLUDE, "captcha", ItemType.CONTENTNODE.getSystemName()),
            new SetPropertyTask(CONFIG, PATH_CACHE_CAPTCHA, "pattern", "/service/captcha/"),
            new SetPropertyTask(CONFIG, PATH_CACHE_CAPTCHA, "class", "info.magnolia.voting.voters.URIStartsWithVoter"),
            new CreateNodeTask("debug", "Create cache config node.", CONFIG, PATH_CACHE_EXCLUDE, "debug", ItemType.CONTENTNODE.getSystemName()),
            new SetPropertyTask(CONFIG, PATH_CACHE_DEBUG, "pattern", "/debug/"),
            new SetPropertyTask(CONFIG, PATH_CACHE_DEBUG, "class", "info.magnolia.voting.voters.URIStartsWithVoter"));

    private final Task _checkCacheConfig = new NodeExistsDelegateTask(
        "Check cache config",
        "Check cache config.",
        CONFIG,
        PATH_CACHE_CAPTCHA,
        null,
        _addCacheConfig
    );

    private final Task _bootstrapApertoTools = new BootstrapResourcesTask("Aperto Tools", "Bootstraps the Aperto Tools Menu.") {
        protected String[] getResourcesToBootstrap(final InstallContext installContext) {
            String[] returnValue;
            if (createApertoTools()) {
                returnValue = new String[] {
                    "/mgnl-bootstrap/apertoTools/config.modules.adminInterface.config.menu.aperto-tools.xml",
                    "/mgnl-bootstrap/apertoTools/config.modules.adminInterface.config.menu.aperto-tools.dmsJCR.xml",
                    "/mgnl-bootstrap/apertoTools/config.modules.adminInterface.config.menu.aperto-tools.groupsJCR.xml",
                    "/mgnl-bootstrap/apertoTools/config.modules.adminInterface.config.menu.aperto-tools.rolesJCR.xml",
                    "/mgnl-bootstrap/apertoTools/config.modules.adminInterface.config.menu.aperto-tools.usersJCR.xml",
                    "/mgnl-bootstrap/apertoTools/config.modules.magkit.trees.dms-jcr.xml",
                    "/mgnl-bootstrap/apertoTools/config.modules.magkit.trees.usergroups-jcr.xml",
                    "/mgnl-bootstrap/apertoTools/config.modules.magkit.trees.userroles-jcr.xml",
                    "/mgnl-bootstrap/apertoTools/config.modules.magkit.trees.users-jcr.xml"
                };
            } else {
                returnValue = new String[]{};
            }
            return returnValue;
        }
    };

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

    /**
     * Returns true if environment property was set and is not 'production' (#MGKT-126).
     *
     * @return create aperto tools or not
     */
    private boolean createApertoTools() {
        boolean returnValue = false;
        ResourceBundle resourceBundle = null;
        try {
            resourceBundle = ResourceBundle.getBundle("environment");
        } catch (MissingResourceException e) {
            ExceptionEater.eat(e);
        }
        if (resourceBundle != null && resourceBundle.containsKey(PROPERTY_KEY)) {
            String value = resourceBundle.getString(PROPERTY_KEY);
            if (isAllowedEnvironmentValue(value)) {
                returnValue = !StringUtils.equals(PROPERTY_VALUE_PRODUCTION_ENVIRONMENT, value);
            }
        }
        return returnValue;
    }

    private boolean isAllowedEnvironmentValue(String environmentValue) {
        return Arrays.asList(ALLOWED_ENVIRONMENT_VALUES).contains(environmentValue);
    }

    @Override
    protected List<Task> getStartupTasks(InstallContext installContext) {
        final List<Task> startupTasks = new ArrayList<Task>();
        startupTasks.add(_bootstrapApertoTools);
        return startupTasks;
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add(_addValidatorFilterTask);
        tasks.add(_addValidatorFilterBypassTask);
        tasks.add(_setI18nContentSupportTask);
        tasks.add(_addBypassFor404);
        tasks.add(_addBypassForStatus);
        tasks.add(_addBypassForDebugSuite);
        tasks.add(_checkCacheConfig);
        return tasks;
    }

    @Override
    protected List<Task> getDefaultUpdateTasks(Version forVersion) {
        List<Task> updateTasks = super.getDefaultUpdateTasks(forVersion);
        updateTasks.add(_bootstrapModuleConfigTask);
        updateTasks.add(_addBypassFor404);
        updateTasks.add(_addBypassForStatus);
        updateTasks.add(_addBypassForDebugSuite);
        updateTasks.add(_checkCacheConfig);
        return updateTasks;
    }
}