package com.aperto.magkit.module.delta;

import com.aperto.magkit.nodebuilder.NodeOperationFactory;
import com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory;
import info.magnolia.jcr.nodebuilder.Ops;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.RegisterServletTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.ServletDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Checks the registration of all module servlets.
 *
 * @author frank.sommer
 * @since 14.06.11
 */
public class CheckModuleServletsTask extends ArrayDelegateTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckModuleServletsTask.class);
    private static final String DEFAULT_SERVLET_FILTER_PATH = "/server/filters/servlets";

    public CheckModuleServletsTask() {
        super("Register module servlets", "Registers servlets for this module.");
    }

    @Override
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        final ModuleDefinition moduleDefinition = installContext.getCurrentModuleDefinition();
        String errorMessage = "Unable to access workspace config to check on servlet definitions for module " + moduleDefinition.getName();

        try {
            Session session = installContext.getConfigJCRSession();
            // register servlets
            for (ServletDefinition servletDefinition : moduleDefinition.getServlets()) {
                String servletName = servletDefinition.getName();

                if (hasNode(session, servletName, errorMessage)) {
                    addTask(NodeBuilderTaskFactory.selectServerConfig("Remove Servlet Configuration", "Remove Servlet Configuration",
                        Ops.getNode("filters/servlets").then(
                            NodeOperationFactory.removeIfExists(servletName))
                    ));
                }
                addTask(new RegisterServletTask(servletDefinition));
            }
        } catch (RepositoryException e) {
            installContext.warn(errorMessage);
        }

        super.execute(installContext);
    }

    private boolean hasNode(final Session session, final String servletName, final String moduleErrorMessage) {
        try {
            return session.getNode(DEFAULT_SERVLET_FILTER_PATH + "/" + servletName) != null;
        } catch (RepositoryException e) {
            LOGGER.debug(moduleErrorMessage, e);
        }

        return false;
    }
}
