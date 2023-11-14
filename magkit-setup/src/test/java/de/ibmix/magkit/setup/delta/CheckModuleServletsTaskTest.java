package de.ibmix.magkit.setup.delta;

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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.model.ModuleDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.module.InstallContextStubbingOperation.stubConfigJCRSession;
import static de.ibmix.magkit.test.cms.module.InstallContextStubbingOperation.stubCurrentModuleDefinition;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubName;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubServlet;
import static de.ibmix.magkit.test.cms.module.ModuleMockUtils.mockInstallContext;
import static de.ibmix.magkit.test.cms.module.ModuleMockUtils.mockModuleDefinition;
import static de.ibmix.magkit.test.cms.module.ModuleMockUtils.mockServletDefinition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the re-installation of the module servlets.
 *
 * @author frank.sommer
 * @since 03.01.2019
 */
public class CheckModuleServletsTaskTest {

    private CheckModuleServletsTask _servletsTask;
    private InstallContext _installContext;
    private ModuleDefinition _moduleDefinition;

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _moduleDefinition = mockModuleDefinition(stubName("TestModule"));
        _installContext = mockInstallContext(
            stubCurrentModuleDefinition(_moduleDefinition),
            stubConfigJCRSession()
        );

        mockComponentInstance(NodeNameHelper.class);
        _servletsTask = new CheckModuleServletsTask();
    }

    @Test
    public void noModuleServlets() throws Exception {
        _servletsTask.execute(_installContext);

        assertThat(_servletsTask.getDescription(), equalTo("Registers servlets for this module."));
        assertThat(_servletsTask.toString(), equalTo("[]"));
    }

    @Test
    public void oneNewServlet() throws Exception {
        stubServlet(mockServletDefinition("new-servlet")).of(_moduleDefinition);
        _servletsTask.execute(_installContext);

        assertThat(_servletsTask.getDescription(), equalTo("Registers servlets for this module."));
        assertThat(_servletsTask.toString(), equalTo("[task: Servlet new-servlet]"));
    }

    @Test
    public void newAndExistingServlets() throws Exception {
        stubServlet(mockServletDefinition("new-servlet")).of(_moduleDefinition);
        _servletsTask.execute(_installContext);

        _servletsTask = new CheckModuleServletsTask();
        stubServlet(mockServletDefinition("existing-servlet")).of(_moduleDefinition);
        _servletsTask.execute(_installContext);

        assertThat(_servletsTask.getDescription(), equalTo("Registers servlets for this module."));
        assertThat(_servletsTask.toString(), equalTo("[task: Remove servlet configuration, task: Servlet new-servlet, task: Servlet existing-servlet]"));
    }

    @Test
    public void repositoryException() throws Exception {
        stubServlet(mockServletDefinition("new-servlet")).of(_moduleDefinition);
        Session session = _installContext.getConfigJCRSession();
        when(session.itemExists(anyString())).thenThrow(new RepositoryException("Exception on item exists."));
        _servletsTask.execute(_installContext);

        assertThat(_servletsTask.getDescription(), equalTo("Registers servlets for this module."));
        assertThat(_servletsTask.toString(), equalTo("[]"));
        verify(_installContext).warn("Unable to access workspace config to check on servlet definitions for module 'TestModule'.");
    }

    @After
    public void tearDown() {
        cleanContext();
    }
}
