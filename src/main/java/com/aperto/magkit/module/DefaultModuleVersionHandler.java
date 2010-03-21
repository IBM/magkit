package com.aperto.magkit.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;

/**
 * A default {@link ModuleVersionHandler} implementation that can be used to extend
 * for own implementations.
 * <p/>
 * It uses the functionality of {@link info.magnolia.module.DefaultModuleVersionHandler}
 * by delegating all method invocation to that implementation but tries to define
 * a more suitable interface for inheritance.
 *
 * @author Norman Wiechmann, Aperto AG
 */
public abstract class DefaultModuleVersionHandler implements ModuleVersionHandler {

    private final DefaultModuleVersionHandlerAdapter _delegate = new DefaultModuleVersionHandlerAdapter();

    ///////////////////////////////////////////////////////////////////////////
    //  Interface implementation CustomModuleVersionHandler

    @Override
    public final Version getCurrentlyInstalled(final InstallContext installContext) {
        return _delegate.getCurrentlyInstalled(installContext);
    }

    @Override
    public final List<Delta> getDeltas(final InstallContext installContext, final Version from) {
        List<Delta> deltas;
        if (from == null) {
            _delegate.registerInstallTasks(getInstallTasks(installContext));
            deltas = _delegate.getDeltas(installContext, from);
        } else {
            _delegate.registerExtraUpdateTasks(getExtraUpdateTasks(installContext));
            _delegate.registerDeltas(getDeltas(installContext));
            deltas = new ArrayList<Delta>();
            deltas.addAll(_delegate.getDeltas(installContext, from));
            // add current version tasks here because they have to be executed always even if
            // the version has not changed
            Version version = installContext.getCurrentModuleDefinition().getVersion();
            List<Task> tasks = new ArrayList<Task>(getCurrentVersionTasks(installContext));
            if (!tasks.isEmpty()) {
                deltas.add(DeltaBuilder.update(version, "").addTasks(tasks));
            }
        }
        return deltas;
    }

    @Override
    public final Delta getStartupDelta(final InstallContext installContext) {
        return _delegate.getStartupDelta(installContext);
    }

    ///////////////////////////////////////////////////////////////////////////
    //  Inheritance method declaration

    /**
     * Return all tasks that have to be executed when a module gets installed
     * the first time. This method will not be invoked if the module is
     * already installed.
     * <p/>
     * You don't need to return the most common installation tasks like
     * register repositories, nodetypes and workspaces as stated in the
     * module definition, bootstrap the module's mgnl-bootstrap files,
     * extract the module's mgnl-files files, register the module's servlets.
     * Those tasks will be executed at default.
     */
    public Collection<Task> getInstallTasks(final InstallContext installContext) {
        return Collections.emptyList();
    }

    /**
     * Return all delta definitions to update a module to a newer version.
     */
    public abstract Collection<Delta> getDeltas(InstallContext installContext);

    /**
     * Return tasks that belong to this module version.
     * <p/>
     * Note: This method is dedicated to development time. Keep this list short
     * and try to move the tasks to a delta when creating a release version.
     */
    public Collection<Task> getCurrentVersionTasks(final InstallContext installContext) {
        return Collections.emptyList();
    }

    /**
     * Override this method to define extra update tasks that you would like
     * to execute with each delta update.
     * <p/>
     * At default there are tasks that extract files that come with the module
     * and also update the version number within the jcr. You can not override those
     * default tasks.
     */
    public Collection<Task> getExtraUpdateTasks(final InstallContext installContext) {
        return Collections.emptyList();
    }

    ///////////////////////////////////////////////////////////////////////////
    //  Inner class

    /**
     * An adapter class to access the protected methods of the
     * {@link info.magnolia.module.AbstractModuleVersionHandler}.
     */
    private static class DefaultModuleVersionHandlerAdapter
        extends info.magnolia.module.DefaultModuleVersionHandler {

        private List<Task> _extraInstallTasks;
        private Collection<Task> _extraUpdateTasks;

        void registerInstallTasks(final Collection<Task> tasks) {
            _extraInstallTasks = tasks instanceof List ? (List<Task>) tasks : new ArrayList<Task>(tasks);
        }

        /**
         * Returns the extra install tasks that have been registered before.
         *
         * @see #registerInstallTasks(java.util.Collection)
         */
        @Override
        protected List<Task> getExtraInstallTasks(InstallContext installContext) {
            return _extraInstallTasks != null ? _extraInstallTasks : super.getExtraInstallTasks(installContext);
        }

        void registerExtraUpdateTasks(Collection<Task> tasks) {
            _extraUpdateTasks = tasks;
        }

        /**
         * Returns the default update tasks as defined by super method and adds those registered before.
         *
         * @see #registerExtraUpdateTasks(java.util.Collection)
         */
        @Override
        protected List<Task> getDefaultUpdateTasks(Version forVersion) {
            List<Task> tasks = super.getDefaultUpdateTasks(forVersion);
            tasks.addAll(_extraUpdateTasks);
            return tasks;
        }

        void registerDeltas(final Collection<Delta> deltas) {
            if (deltas != null) {
                for (Delta delta : deltas) {
                    register(delta);
                }
            }
        }
    }
}