package com.aperto.magkit.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import info.magnolia.module.AbstractModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.delta.*;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.Assert;
import org.junit.Test;

/**
 * TODO: comment.
 *
 * @author Norman Wiechmann, Aperto AG
 */
public class DefaultModuleVersionHandlerTest {

    private static final Version VERSION1 = Version.parseVersion(1, 0, 0);
    private static final Version VERSION2 = Version.parseVersion(2, 0, 0);
    private static final Version VERSION3 = Version.parseVersion(3, 0, 0);
    private static final Version VERSION4 = Version.parseVersion(4, 0, 0);
    private static final Task DUMMY_TASK = new AbstractTask("", "") {

        @Override
        public void execute(InstallContext installContext) throws TaskExecutionException {
            // do nothing
        }
    };
    private static final Class[] DEFAULT_INSTALL_TASKS = {SetupModuleRepositoriesTask.class,
        ModuleBootstrapTask.class, SamplesBootstrapTask.class, ModuleFilesExtraction.class,
        RegisterModuleServletsTask.class, AbstractModuleVersionHandler.ModuleVersionToLatestTask.class};

    private static final Class[] DEFAULT_UPDATE_TASKS = {ModuleFilesExtraction.class,
        AbstractModuleVersionHandler.ModuleVersionToLatestTask.class};

    @Test
    public void install() {
        assertThat(new CustomModuleVersionHandler(), null, VERSION1, DEFAULT_INSTALL_TASKS);
    }

    @Test
    public void installMagnolia() {
        assertThat(new MagnoliaModuleVersionHandler(), null, VERSION1, DEFAULT_INSTALL_TASKS);
    }

    @Test
    public void update() {
        assertThat(new CustomModuleVersionHandler(), VERSION1, VERSION2, DEFAULT_UPDATE_TASKS);
    }

    @Test
    public void updateMagnolia() {
        assertThat(new MagnoliaModuleVersionHandler(), VERSION1, VERSION2, DEFAULT_UPDATE_TASKS);
    }

    /**
     * Add a new module of version 2 that defines a delta for version 1.
     */
    @Test
    public void installWithDelta() {
        Collection<Delta> delta = new ArrayList<Delta>();
        delta.add(DeltaBuilder.update(VERSION1, "").addTask(DUMMY_TASK));
        delta.add(DeltaBuilder.update(VERSION2, "").addTask(DUMMY_TASK));
        assertThat(new CustomModuleVersionHandler(delta), null, VERSION3, DEFAULT_INSTALL_TASKS);
        // TODO why was DUMMY_TASK not returned?
    }

    /**
     * Add a new module of version 2 that defines a delta for version 1 using magnolias default version handler.
     */
    @Test
    public void installMagnoliaWithDelta() {
        Collection<Delta> delta = new ArrayList<Delta>();
        delta.add(DeltaBuilder.update(VERSION1, "").addTask(DUMMY_TASK));
        delta.add(DeltaBuilder.update(VERSION2, "").addTask(DUMMY_TASK));
        assertThat(new MagnoliaModuleVersionHandler(delta), null, VERSION3, DEFAULT_INSTALL_TASKS);
        // TODO why was DUMMY_TASK not returned?
    }

    @Test
    public void updateWithDelta() {
        Collection<Delta> delta = new ArrayList<Delta>();
        delta.add(DeltaBuilder.update(VERSION2, "").addTask(DUMMY_TASK));
        delta.add(DeltaBuilder.update(VERSION3, "").addTask(DUMMY_TASK));
        assertThat(new CustomModuleVersionHandler(delta), VERSION1, VERSION4, DUMMY_TASK.getClass(),
            ModuleFilesExtraction.class, AbstractModuleVersionHandler.ModuleVersionToLatestTask.class, DUMMY_TASK.getClass(),
            ModuleFilesExtraction.class, AbstractModuleVersionHandler.ModuleVersionToLatestTask.class,
            ModuleFilesExtraction.class, AbstractModuleVersionHandler.ModuleVersionToLatestTask.class);
    }

    @Test
    public void updateMagnoliaWithDelta() {
        Collection<Delta> delta = new ArrayList<Delta>();
        delta.add(DeltaBuilder.update(VERSION2, "").addTask(DUMMY_TASK));
        delta.add(DeltaBuilder.update(VERSION3, "").addTask(DUMMY_TASK));
        assertThat(new MagnoliaModuleVersionHandler(delta), VERSION1, VERSION4, DUMMY_TASK.getClass(),
            ModuleFilesExtraction.class, AbstractModuleVersionHandler.ModuleVersionToLatestTask.class,
            DUMMY_TASK.getClass(),
            ModuleFilesExtraction.class, AbstractModuleVersionHandler.ModuleVersionToLatestTask.class,
            ModuleFilesExtraction.class, AbstractModuleVersionHandler.ModuleVersionToLatestTask.class);
    }

    private static void assertThat(final ModuleVersionHandler handler, final Version currentVersion,
                                   final Version newVersion, final Class... classes) {
        InstallContextImpl installContext = new InstallContextImpl();
        ModuleDefinition definition = new ModuleDefinition();
        definition.setVersion(newVersion);
        installContext.setCurrentModule(definition);
        List<Delta> deltas = handler.getDeltas(installContext, currentVersion);
        Assert.assertThat(deltas, notNullValue());
        List<Task> tasks = new ArrayList<Task>();
        for (Delta delta : deltas) {
            tasks.addAll(delta.getTasks());
        }
        Assert.assertThat(tasks.size(), is(classes.length));
        for (int i = 0; i < tasks.size(); i++) {
            Assert.assertThat(tasks.get(i), instanceOf(classes[i]));
        }
    }

    /**
      * Checkstyle needs an comment here.
      */
    static class CustomModuleVersionHandler extends DefaultModuleVersionHandler {

        private Collection<Delta> _deltas;

        CustomModuleVersionHandler() {
            _deltas = Collections.emptyList();
        }

        CustomModuleVersionHandler(Collection<Delta> deltas) {
            _deltas = deltas;
        }

        @Override
        public Collection<Delta> getDeltas(InstallContext installContext) {
            return _deltas;
        }
    }

    /**
      * Checkstyle needs an comment here.
      */
    static class MagnoliaModuleVersionHandler extends info.magnolia.module.DefaultModuleVersionHandler {

        MagnoliaModuleVersionHandler() {
        }

        MagnoliaModuleVersionHandler(Collection<Delta> deltas) {
            for (Delta delta : deltas) {
                register(delta);
            }
        }
    }
}