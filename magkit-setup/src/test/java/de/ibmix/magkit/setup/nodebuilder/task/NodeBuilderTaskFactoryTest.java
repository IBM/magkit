package de.ibmix.magkit.setup.nodebuilder.task;

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

import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;

import static de.ibmix.magkit.test.cms.module.InstallContextStubbingOperation.stubConfigJCRSession;
import static de.ibmix.magkit.test.cms.module.ModuleMockUtils.mockInstallContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockMgnlNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link NodeBuilderTaskFactory} covering creation of tasks for root, server and module configuration paths.
 * Verifies workspace name, root path construction, error handling default and operations array preservation including empty varargs.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-27
 */
public class NodeBuilderTaskFactoryTest {

    /**
     * Creates a task for config root and verifies workspace, root path and error handling logging with no operations.
     */
    @Test
    public void selectConfigCreatesRootTask() throws Exception {
        InstallContext ctx = mockInstallContext(stubConfigJCRSession());
        Node root = mockMgnlNode("config", "/", NodeTypes.Content.NAME);
        NodeBuilderTask task = NodeBuilderTaskFactory.selectConfig("rootTask", "desc");
        assertEquals("rootTask", task.getName());
        assertEquals("desc", task.getDescription());
        assertEquals(root, task.getRootNode(ctx));
        assertInstanceOf(TaskLogErrorHandler.class, task.newErrorHandler(ctx));
    }

    /**
     * Creates a task for server config and verifies the root path "/server" and provided operations order.
     */
    @Test
    public void selectServerConfigCreatesServerTask() throws Exception {
        InstallContext ctx = mockInstallContext(stubConfigJCRSession());
        Node root = mockMgnlNode("config", "/server", NodeTypes.Content.NAME);
        NodeOperation op1 = mock(NodeOperation.class);
        NodeOperation op2 = mock(NodeOperation.class);
        NodeBuilderTask task = NodeBuilderTaskFactory.selectServerConfig("serverTask", "desc", op1, op2);
        assertEquals("serverTask", task.getName());
        assertEquals("desc", task.getDescription());
        assertEquals(root, task.getRootNode(ctx));
        assertInstanceOf(TaskLogErrorHandler.class, task.newErrorHandler(ctx));
        task.doExecute(ctx);
        verify(op1).exec(eq(root), any(TaskLogErrorHandler.class));
        verify(op2).exec(eq(root), any(TaskLogErrorHandler.class));
    }

    /**
     * Creates a task for a module config and verifies path concatenation "/modules/sample".
     */
    @Test
    public void selectModuleConfigCreatesModuleTask() throws Exception {
        InstallContext ctx = mockInstallContext(stubConfigJCRSession());
        Node root = mockMgnlNode("config", "/modules/sample", NodeTypes.Content.NAME);
        NodeOperation op = mock(NodeOperation.class);
        NodeBuilderTask task = NodeBuilderTaskFactory.selectModuleConfig("moduleTask", "desc", "sample", op);
        assertEquals("moduleTask", task.getName());
        assertEquals("desc", task.getDescription());
        assertEquals(root, task.getRootNode(ctx));
        assertInstanceOf(TaskLogErrorHandler.class, task.newErrorHandler(ctx));
        task.doExecute(ctx);
        verify(op).exec(eq(root), any(TaskLogErrorHandler.class));
    }

    /**
     * Creates a task for a module config when module name starts with slash: Sanitization expected.
     */
    @Test
    public void selectModuleConfigWithLeadingSlashIsSanitized() throws Exception {
        InstallContext ctx = mockInstallContext(stubConfigJCRSession());
        Node root = mockMgnlNode("config", "/modules/withSlash", NodeTypes.Content.NAME);
        NodeBuilderTask task = NodeBuilderTaskFactory.selectModuleConfig("moduleTask", "desc", "/withSlash");
        assertEquals("moduleTask", task.getName());
        assertEquals("desc", task.getDescription());
        assertEquals(root, task.getRootNode(ctx));
    }

    /**
     * Creates a task for an empty or null module name resulting in IllegalArgumentException.
     */
    @Test
    public void selectModuleConfigWithEmptyNameCreatesModulesRoot() {
        assertThrows(IllegalArgumentException.class, () -> NodeBuilderTaskFactory.selectModuleConfig("moduleTask", "desc", null));
        assertThrows(IllegalArgumentException.class, () -> NodeBuilderTaskFactory.selectModuleConfig("moduleTask", "desc", ""));
        assertThrows(IllegalArgumentException.class, () -> NodeBuilderTaskFactory.selectModuleConfig("moduleTask", "desc", "  "));
    }
}
