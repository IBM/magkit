package com.aperto.magkit.module.delta;

import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.RegisterServletTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.ServletDefinition;
import info.magnolia.objectfactory.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckModuleServletsTask.class);
    private static final String DEFAULT_RELATIVE_PATH = "filters/servlets";
    private static final String DEFAULT_ABSOLUTE_PATH = "/server/" + DEFAULT_RELATIVE_PATH;

    public CheckModuleServletsTask() {
        super("Register module servlets", "Registers servlets for this module.");
    }

    @Override
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        final ModuleDefinition moduleDefinition = installContext.getCurrentModuleDefinition();
        String errorMessage = "Unable to access workspace config to check on servlet definitions for module '" + moduleDefinition.getName() + "'.";

        try {
            Session session = installContext.getConfigJCRSession();
            NodeNameHelper nodeNameHelper = Components.getComponent(NodeNameHelper.class);

            // iterate module servlets
            for (ServletDefinition servletDefinition : moduleDefinition.getServlets()) {
                String servletName = servletDefinition.getName();

                // add remove task for existing servlets before adding a register task
                if (session.itemExists(DEFAULT_ABSOLUTE_PATH + "/" + servletName)) {
                    addTask(
                        selectServerConfig("Remove servlet configuration", "Remove servlet configuration '" + servletName + "'.",
                            getNode(DEFAULT_RELATIVE_PATH).then(removeIfExists(servletName))
                        )
                    );
                }
                addTask(new RegisterServletTask(servletDefinition, nodeNameHelper));
            }
        } catch (RepositoryException e) {
            installContext.warn(errorMessage);
            LOGGER.info(errorMessage, e);
        }

        super.execute(installContext);
    }
}
