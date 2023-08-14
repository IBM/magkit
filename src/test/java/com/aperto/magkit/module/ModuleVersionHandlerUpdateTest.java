package com.aperto.magkit.module;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static info.magnolia.module.model.Version.parseVersion;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
