package com.aperto.magkit.module.delta;

import com.aperto.magkit.mockito.InstallContextMockUtils;
import com.aperto.magkit.mockito.InstallContextStubbingOperation;
import com.aperto.magkit.mockito.ModuleDefinitionMockUtils;
import com.aperto.magkit.mockito.ModuleDefinitionStubbingOperation;
import info.magnolia.module.InstallContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Philipp Güttler (Aperto GmbH - An IBM Company)
 * @since 03.05.2018
 */
public class InstallBootstrapTaskTest {

    private InstallBootstrapTask _task;
    private InstallContext _ctx;

    @Before
    public void setUp() {
        _task = new InstallBootstrapTask();
        _ctx = InstallContextMockUtils.mockInstallContext(
            InstallContextStubbingOperation.stubModuleDefinition(
                ModuleDefinitionMockUtils.mockModuleDefinition(
                    ModuleDefinitionStubbingOperation.stubName("my-module"))));
    }

    @Test
    public void acceptXmlResource() {
        Assert.assertTrue(_task.acceptResource(_ctx, "/mgnl-bootstrap/install/my-module/config.modules.my-module.config.service.xml"));
    }

    @Test
    public void acceptYamlResource() {
        Assert.assertTrue(_task.acceptResource(_ctx, "/mgnl-bootstrap/install/my-module/config.modules.my-module.config.service.yaml"));
    }

    @Test
    public void rejectJsonResource() {
        Assert.assertFalse(_task.acceptResource(_ctx, "/mgnl-bootstrap/install/my-module/config.modules.my-module.config.service.json"));
    }

    @Test
    public void rejectWrongFolderResource() {
        Assert.assertFalse(_task.acceptResource(_ctx, "/mgnl-bootstrap/my-module/config.modules.my-module.config.service.yaml"));
    }
}
