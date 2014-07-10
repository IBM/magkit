package com.aperto.magkit.module.delta;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.RegisterServletTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.ServletDefinition;

import static com.aperto.magkit.nodebuilder.NodeOperationFactory.removeIfExists;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectServerConfig;
import static info.magnolia.jcr.nodebuilder.Ops.getNode;

/**
 * Checks the registration of all module servlets.
 *
 * @author frank.sommer
 * @since 14.06.11
 */
public class CheckModuleServletsTask extends ArrayDelegateTask {
    private static final String DEFAULT_SERVLET_FILTER_PATH = "/server/filters/servlets";

    public CheckModuleServletsTask() {
        super("Register module servlets", "Registers servlets for this module.");
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        final ModuleDefinition moduleDefinition = installContext.getCurrentModuleDefinition();
        HierarchyManager hierarchyManager = installContext.getConfigHierarchyManager();

        // register servlets
        for (ServletDefinition servletDefinition : moduleDefinition.getServlets()) {
            if (hierarchyManager.isExist(DEFAULT_SERVLET_FILTER_PATH + "/" + servletDefinition.getName())) {
                addTask(selectServerConfig("Remove Servlet Configuration", "Remove Servlet Configuration",
                    getNode("filters/servlets").then(
                        removeIfExists(servletDefinition.getName()))
                ));
            }
            addTask(new RegisterServletTask(servletDefinition));
        }

        super.execute(installContext);
    }
}