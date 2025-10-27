package de.ibmix.magkit.setup.delta;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2025 IBM iX
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
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.TaskExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import static de.ibmix.magkit.test.cms.module.InstallContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.cms.module.ModuleMockUtils.mockInstallContext;
import static de.ibmix.magkit.test.cms.module.ModuleMockUtils.mockModuleDefinition;
import static de.ibmix.magkit.test.cms.module.InstallContextStubbingOperation.stubCurrentModuleDefinition;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubName;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static info.magnolia.resourceloader.jcr.JcrResourceOrigin.RESOURCES_WORKSPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RemoveResourcesHotfixesTask} covering SQL2 statement construction and removal behavior for hotfix nodes.
 * Tests ensure that the task builds the expected query, removes all returned nodes and handles empty results without errors.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-27
 */
public class RemoveResourcesHotfixesTaskTest {

    private InstallContext installContext;

    @BeforeEach
    public void setUp() throws RepositoryException {
        ContextMockUtils.cleanContext();
        installContext = mockInstallContext(
            stubCurrentModuleDefinition(mockModuleDefinition(stubName("my-module-name"))),
            stubJcrSession(RESOURCES_WORKSPACE)
        );
    }

    /**
     * Verifies that all returned nodes from the query are removed and the SQL2 statement matches the expected structure.
     */
    @Test
    public void removesAllHotfixNodes() throws Exception {
        String basePath = "/my-module";
        RemoveResourcesHotfixesTask task = new RemoveResourcesHotfixesTask(basePath);
        String expectedStatement = "select * from [" + NodeTypes.Content.NAME + "] where [" + NodeTypes.Renderable.TEMPLATE + "] IS NULL and ISDESCENDANTNODE([" + basePath + "])";
        Node n1 = mockNode("resources", basePath + "/hotfix1");
        Node n2 = mockNode("resources", basePath + "/hotfix2");
        ContextMockUtils.mockQueryResult(RESOURCES_WORKSPACE, Query.JCR_SQL2, expectedStatement, n1, n2);
        task.execute(installContext);
        verify(n1, times(1)).remove();
        verify(n2, times(1)).remove();
    }

    /**
     * Verifies that no removal is attempted when the query returns no nodes.
     */
    @Test
    public void handlesEmptyResultWithoutRemoval() throws Exception {
        String basePath = "/empty-module";
        RemoveResourcesHotfixesTask task = new RemoveResourcesHotfixesTask(basePath);
        String expectedStatement = "select * from [" + NodeTypes.Content.NAME + "] where [" + NodeTypes.Renderable.TEMPLATE + "] IS NULL and ISDESCENDANTNODE([" + basePath + "])";
        QueryResult result = ContextMockUtils.mockQueryResult(RESOURCES_WORKSPACE, Query.JCR_SQL2, expectedStatement);
        task.execute(installContext);
        assertFalse(result.getNodes().hasNext());
    }

    /**
     * Verifies that a RepositoryException thrown during query creation is propagated to the caller.
     */
    @Test
    public void propagatesRepositoryExceptionFromQueryCreation() throws Exception {
        String basePath = "/exception-module";
        RemoveResourcesHotfixesTask task = new RemoveResourcesHotfixesTask(basePath);
        QueryManager qm = ContextMockUtils.mockQueryManager(RESOURCES_WORKSPACE);
        when(qm.createQuery(any(String.class), any(String.class))).thenThrow(new RepositoryException("boom"));
        assertThrows(TaskExecutionException.class, () -> task.execute(installContext));
        verify(qm).createQuery(any(String.class), any(String.class));
    }

    /**
     * Verifies removal also works when base path ends with a trailing slash.
     */
    @Test
    public void removesHotfixNodesWithTrailingSlashBasePath() throws Exception {
        String basePath = "/trailing-module/";
        RemoveResourcesHotfixesTask task = new RemoveResourcesHotfixesTask(basePath);
        Node n1 = mockNode("resources", basePath + "hotfixA");
        String expectedStatement = "select * from [" + NodeTypes.Content.NAME + "] where [" + NodeTypes.Renderable.TEMPLATE + "] IS NULL and ISDESCENDANTNODE([" + basePath + "])";
        ContextMockUtils.mockQueryResult(RESOURCES_WORKSPACE, Query.JCR_SQL2, expectedStatement, n1);
        task.execute(installContext);
        verify(n1).remove();
    }

    /**
     * Verifies task exposes expected name and description strings.
     */
    @Test
    public void exposesNameAndDescription() {
        String basePath = "/verify";
        RemoveResourcesHotfixesTask task = new RemoveResourcesHotfixesTask(basePath);
        assertEquals("Remove hotfixes for module resources", task.getName());
        assertEquals("Remove hotfixes for module resources below /verify.", task.getDescription());
    }
}
