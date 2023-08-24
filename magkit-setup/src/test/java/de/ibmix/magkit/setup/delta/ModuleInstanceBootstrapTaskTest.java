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

import info.magnolia.module.InstallContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static de.ibmix.magkit.test.cms.context.InstallContextMockUtils.mockInstallContext;
import static de.ibmix.magkit.test.cms.context.InstallContextStubbingOperation.stubModuleDefinition;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.cleanServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.mockServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubIsAdmin;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionMockUtils.mockModuleDefinition;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubName;
import static org.junit.Assert.assertFalse;

/**
 * @author Philipp Güttler (Aperto GmbH - An IBM Company)
 * @since 03.05.2018
 */
public class ModuleInstanceBootstrapTaskTest {

    private ModuleInstanceBootstrapTask _task;
    private InstallContext _ctx;

    @Before
    public void setUp() {
        _task = new ModuleInstanceBootstrapTask();
        _ctx = mockInstallContext(stubModuleDefinition(mockModuleDefinition(stubName("my-module"))));
        cleanServerConfiguration();
    }

    @Test
    public void acceptAuthorXmlResource() {
        mockServerConfiguration(stubIsAdmin(true));

        Assert.assertTrue(_task.acceptResource(_ctx, "/mgnl-bootstrap/author/my-module/config.modules.my-module.config.service.xml"));
    }

    @Test
    public void acceptAuthorYamlResource() {
        mockServerConfiguration(stubIsAdmin(true));

        Assert.assertTrue(_task.acceptResource(_ctx, "/mgnl-bootstrap/author/my-module/config.modules.my-module.config.service.yaml"));
    }

    @Test
    public void rejectAuthorJsonResource() {
        mockServerConfiguration(stubIsAdmin(true));

        assertFalse(_task.acceptResource(_ctx, "/mgnl-bootstrap/author/my-module/config.modules.my-module.config.service.json"));
    }

    @Test
    public void rejectPublicXmlResource() {
        mockServerConfiguration(stubIsAdmin(true));

        assertFalse(_task.acceptResource(_ctx, "/mgnl-bootstrap/public/my-module/config.modules.my-module.config.service.xml"));
    }

    @Test
    public void acceptPublicXmlResource() {
        mockServerConfiguration(stubIsAdmin(false));

        Assert.assertTrue(_task.acceptResource(_ctx, "/mgnl-bootstrap/public/my-module/config.modules.my-module.config.service.xml"));
    }

    @Test
    public void acceptPublicYamlResource() {
        mockServerConfiguration(stubIsAdmin(false));

        Assert.assertTrue(_task.acceptResource(_ctx, "/mgnl-bootstrap/public/my-module/config.modules.my-module.config.service.yaml"));
    }

    @Test
    public void rejectPublicJsonResource() {
        mockServerConfiguration(stubIsAdmin(false));

        assertFalse(_task.acceptResource(_ctx, "/mgnl-bootstrap/public/my-module/config.modules.my-module.config.service.json"));
    }

    @Test
    public void rejectAuthorXmlResource() {
        mockServerConfiguration(stubIsAdmin(false));

        assertFalse(_task.acceptResource(_ctx, "/mgnl-bootstrap/author/my-module/config.modules.my-module.config.service.xml"));
    }

}
