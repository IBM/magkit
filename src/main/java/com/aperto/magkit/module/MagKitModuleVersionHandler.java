package com.aperto.magkit.module;

import com.aperto.magkit.filter.HtmlValidatorFilter;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.*;
import info.magnolia.setup.AddFilterBypassTask;
import java.util.ArrayList;
import java.util.List;

/**
 * The MagKitModuleVersionHandler for the MagKit module.
 * @author frank.sommer
 * Date: 03.04.2008
 */
public class MagKitModuleVersionHandler extends DefaultModuleVersionHandler {

    private final Task _addCmsFilterBypassTask = new ArrayDelegateTask("Filter", "Add bypasses for filter 'cms'", new Task[]{
        new AddFilterBypassTask("/server/filters/cms", "debug", info.magnolia.voting.voters.URIStartsWithVoter.class, "/debug"),
        new AddFilterBypassTask("/server/filters/cms", "captcha", info.magnolia.voting.voters.URIStartsWithVoter.class, "/captcha"),
        new AddFilterBypassTask("/server/filters/cms", "core", info.magnolia.voting.voters.URIStartsWithVoter.class, "/core")
    });
    private final Task _addValidatorFilterTask = new ArrayDelegateTask("Filter", "Add the Validator filter.", new Task[]{
        new CreateNodeTask("Validator-Filter", "Create Validator filter node", ContentRepository.CONFIG, "/server/filters", "validator", ItemType.CONTENT.getSystemName()),
        new SetPropertyTask(ContentRepository.CONFIG, "/server/filters/validator", "class", "com.aperto.magkit.filter.HtmlValidatorFilter"),
        new SetPropertyTask(ContentRepository.CONFIG, "/server/filters/validator", "enabled", "true"),

        new CreateNodeTask("Validator-Filter config", "Create config node for validator filter node", ContentRepository.CONFIG, "/server/filters/validator", "config", ItemType.CONTENTNODE.getSystemName()),
        new SetPropertyTask(ContentRepository.CONFIG, "/server/filters/validator/config", HtmlValidatorFilter.W3C_VALIDATOR_CHECK_URL_PARAM_NAME, "http://validator.aperto.de/w3c-markup-validator/check"),
        new FilterOrderingTask("validator", new String[]{"contentType", "uriSecurity"})
    });

    private final Task _addValidatorFilterBypassTask = new ArrayDelegateTask("Filter", "Add the bypass for validator filter.", new Task[] {
        new AddFilterBypassTask("/server/filters/validator", "isAdmin", info.magnolia.voting.voters.OnAdminVoter.class, ""),
        new SetPropertyTask(ContentRepository.CONFIG, "/server/filters/validator/bypasses/isAdmin", "not", "true"),
    });

    private final Task _setAdminInterfaceExportClassTask = new SetPropertyTask(ContentRepository.CONFIG, "/modules/adminInterface/pages/export", "class", "com.aperto.magkit.export.ExportPageAlphabetically");

    /**
     * Whithin the default constructor the DeltaBuilder is updated with all nessesary Tasks for an update to module version 1.5 (for Magnolia 3.5.x).
     * <ul>
     *      <li>remove deprecated filter property 'priority'</li>
     *      <li>move acegi filter to correct position </li>
     * </ul>
     */
    public MagKitModuleVersionHandler() {

    }

    protected List getExtraInstallTasks(InstallContext installContext) {
        final List tasks = new ArrayList();
        tasks.add(_addCmsFilterBypassTask);
        tasks.add(_addValidatorFilterTask);
        tasks.add(_addValidatorFilterBypassTask);
        tasks.add(_setAdminInterfaceExportClassTask);
        return tasks;
    }
}