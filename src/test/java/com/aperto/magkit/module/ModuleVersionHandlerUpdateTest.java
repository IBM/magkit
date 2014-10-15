package com.aperto.magkit.module;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static info.magnolia.module.model.Version.parseVersion;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test the module update handling with custom revision classifiers.
 *
 * @author frank.sommer
 * @since 04.06.13
 */
public class ModuleVersionHandlerUpdateTest {

    @Test
    public void testModuleWoClassifiers() {
        List<Delta> updateDeltas = doTestWithVersions("1.0.1", "1.0.1");
        assertThat(updateDeltas.size(), is(0));
    }

    @Test
    public void testModuleWoClassifiersUpdate() {
        List<Delta> updateDeltas = doTestWithVersions("1.0.0", "1.0.1");
        assertThat(updateDeltas.size(), is(1));
    }

    @Test
    public void testReleasedModuleUpdate() {
        List<Delta> updateDeltas = doTestWithVersions("1.0.1-rabcde", "1.0.1");
        assertThat(updateDeltas.size(), is(1));
    }

    @Test
    public void testSameModuleVersion() {
        List<Delta> updateDeltas = doTestWithVersions("1.0.1-rabcde", "1.0.1-rabcde");
        assertThat(updateDeltas.size(), is(0));
    }

    @Test
    public void testNewModuleVersion() {
        List<Delta> updateDeltas = doTestWithVersions("1.0.0-rabcde", "1.0.1-r1234");
        assertThat(updateDeltas.size(), is(1));
    }

    @Test
    public void testOldModuleVersion() {
        List<Delta> updateDeltas = doTestWithVersions("1.0.1-rabcde", "1.0.0-r1234");
        assertThat(updateDeltas.size(), is(0));
    }

    @Test
    public void testNewClassifierVersion() {
        List<Delta> updateDeltas = doTestWithVersions("1.0.1-rabcde", "1.0.1-r1234");
        assertThat(updateDeltas.size(), is(1));
    }

    private List<Delta> doTestWithVersions(final String from, final String to) {
        BootstrapModuleVersionHandler handler = new BootstrapModuleVersionHandler();
        Version toVersion = parseVersion(to);
        Version fromVersion = parseVersion(from);

        InstallContext installContext = Mockito.mock(InstallContext.class);
        ModuleDefinition definition = new ModuleDefinition("test", toVersion, "", null);
        when(installContext.getCurrentModuleDefinition()).thenReturn(definition);

        return handler.getUpdateDeltas(installContext, fromVersion);
    }
}
