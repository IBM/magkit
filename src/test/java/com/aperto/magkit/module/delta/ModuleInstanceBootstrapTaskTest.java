package com.aperto.magkit.module.delta;

import com.aperto.magkit.mockito.InstallContextMockUtils;
import com.aperto.magkit.mockito.InstallContextStubbingOperation;
import com.aperto.magkit.mockito.ModuleDefinitionMockUtils;
import com.aperto.magkit.mockito.ModuleDefinitionStubbingOperation;
import com.aperto.magkit.mockito.ServerConfigurationMockUtils;
import com.aperto.magkit.mockito.ServerConfigurationStubbingOperation;
import info.magnolia.module.InstallContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        _ctx = InstallContextMockUtils.mockInstallContext(
            InstallContextStubbingOperation.stubModuleDefinition(
                ModuleDefinitionMockUtils.mockModuleDefinition(
                    ModuleDefinitionStubbingOperation.stubName("my-module"))));
        ServerConfigurationMockUtils.cleanServerConfiguration();
    }

    @Test
    public void acceptAuthorXmlResource() {
        ServerConfigurationMockUtils.mockServerConfiguration(ServerConfigurationStubbingOperation.stubIsAdmin(true));

        Assert.assertTrue(_task.acceptResource(_ctx, "/mgnl-bootstrap/author/my-module/config.modules.my-module.config.service.xml"));
    }

    @Test
    public void acceptAuthorYamlResource() {
        ServerConfigurationMockUtils.mockServerConfiguration(ServerConfigurationStubbingOperation.stubIsAdmin(true));

        Assert.assertTrue(_task.acceptResource(_ctx, "/mgnl-bootstrap/author/my-module/config.modules.my-module.config.service.yaml"));
    }

    @Test
    public void rejectAuthorJsonResource() {
        ServerConfigurationMockUtils.mockServerConfiguration(ServerConfigurationStubbingOperation.stubIsAdmin(true));

        Assert.assertFalse(_task.acceptResource(_ctx, "/mgnl-bootstrap/author/my-module/config.modules.my-module.config.service.json"));
    }

    @Test
    public void rejectPublicXmlResource() {
        ServerConfigurationMockUtils.mockServerConfiguration(ServerConfigurationStubbingOperation.stubIsAdmin(true));

        Assert.assertFalse(_task.acceptResource(_ctx, "/mgnl-bootstrap/public/my-module/config.modules.my-module.config.service.xml"));
    }

    @Test
    public void acceptPublicXmlResource() {
        ServerConfigurationMockUtils.mockServerConfiguration(ServerConfigurationStubbingOperation.stubIsAdmin(false));

        Assert.assertTrue(_task.acceptResource(_ctx, "/mgnl-bootstrap/public/my-module/config.modules.my-module.config.service.xml"));
    }

    @Test
    public void acceptPublicYamlResource() {
        ServerConfigurationMockUtils.mockServerConfiguration(ServerConfigurationStubbingOperation.stubIsAdmin(false));

        Assert.assertTrue(_task.acceptResource(_ctx, "/mgnl-bootstrap/public/my-module/config.modules.my-module.config.service.yaml"));
    }

    @Test
    public void rejectPublicJsonResource() {
        ServerConfigurationMockUtils.mockServerConfiguration(ServerConfigurationStubbingOperation.stubIsAdmin(false));

        Assert.assertFalse(_task.acceptResource(_ctx, "/mgnl-bootstrap/public/my-module/config.modules.my-module.config.service.json"));
    }

    @Test
    public void rejectAuthorXmlResource() {
        ServerConfigurationMockUtils.mockServerConfiguration(ServerConfigurationStubbingOperation.stubIsAdmin(false));

        Assert.assertFalse(_task.acceptResource(_ctx, "/mgnl-bootstrap/author/my-module/config.modules.my-module.config.service.xml"));
    }

}
