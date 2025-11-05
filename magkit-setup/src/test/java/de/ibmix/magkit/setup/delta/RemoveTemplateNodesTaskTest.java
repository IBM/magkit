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
import de.ibmix.magkit.test.cms.module.InstallContextStubbingOperation;
import de.ibmix.magkit.test.cms.module.ModuleMockUtils;
import de.ibmix.magkit.test.jcr.NodeMockUtils;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import java.util.ArrayList;
import java.util.List;

import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link RemoveTemplateNodesTask} covering query building branches, node operation batching and exception handling.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-27
 */
public class RemoveTemplateNodesTaskTest {

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    /**
     * Auto-detects component node type when template contains ":components/" and adds descendant clause for base path.
     */
    @Test
    public void buildQueryStatementDetectsComponentWithDescendantClause() {
        RemoveTemplateNodesTask task = new RemoveTemplateNodesTask("app:components/teaser", "/parent/path", null, "test");
        String statement = task.buildQueryStatement();
        assertEquals("select * from [mgnl:component] where [mgnl:template] = 'app:components/teaser' and ISDESCENDANTNODE([/parent/path])", statement);
    }

    /**
     * Auto-detects page node type when template does not contain ":components/" and omits descendant clause for root base path.
     */
    @Test
    public void buildQueryStatementDetectsPageNoDescendantClause() {
        RemoveTemplateNodesTask task = new RemoveTemplateNodesTask("app:pages/home", "/", null, "test");
        String statement = task.buildQueryStatement();
        assertEquals("select * from [mgnl:page] where [mgnl:template] = 'app:pages/home'", statement);
    }

    /**
     * Uses explicitly provided query type ignoring auto detection logic.
     */
    @Test
    public void buildQueryStatementRespectsExplicitQueryType() {
        RemoveTemplateNodesTask task = new RemoveTemplateNodesTask("app:components/ignored", "/", NodeTypes.Page.NAME, "test");
        String statement = task.buildQueryStatement();
        assertEquals("select * from [mgnl:page] where [mgnl:template] = 'app:components/ignored'", statement);
    }

    /**
     * Batches session.save calls every 100 nodes and performs final save for remaining nodes.
     */
    @Test
    public void doNodeOperationsBatchesSessionSaves() throws RepositoryException {
        Session session = mock(Session.class);
        List<Node> nodeList = new ArrayList<>();
        for (int i = 0; i < 250; i++) {
            Node n = NodeMockUtils.mockNode("website", "/root/n" + i);
            nodeList.add(n);
        }
        NodeIterator iterator = new NodeIteratorAdapter(nodeList);
        RemoveTemplateNodesTask task = new RemoveTemplateNodesTask("app:pages/test");
        task.doNodeOperations(session, iterator);
        verify(session, times(3)).save();
        for (Node n : nodeList) {
            verify(n, times(1)).remove();
        }
    }

    /**
     * Does not perform an extra final save when processed nodes count is an exact multiple of the threshold.
     */
    @Test
    public void doNodeOperationsExactMultipleNoFinalSave() throws RepositoryException {
        Session session = mock(Session.class);
        List<Node> nodeList = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            nodeList.add(NodeMockUtils.mockNode("website", "/root/m" + i));
        }
        NodeIterator iterator = new NodeIteratorAdapter(nodeList);
        RemoveTemplateNodesTask task = new RemoveTemplateNodesTask("app:pages/test");
        task.doNodeOperations(session, iterator);
        verify(session, times(2)).save();
    }

    /**
     * Performs no session.save calls for empty result iterator.
     */
    @Test
    public void doNodeOperationsEmptyDoesNotSave() throws RepositoryException {
        Session session = mock(Session.class);
        NodeIterator iterator = new NodeIteratorAdapter(new ArrayList<>());
        RemoveTemplateNodesTask task = new RemoveTemplateNodesTask("app:pages/test");
        task.doNodeOperations(session, iterator);
        verify(session, times(0)).save();
    }

    /**
     * Catches RepositoryException from node.remove and continues without propagating.
     */
    @Test
    public void doNodeOperationCatchesRepositoryException() throws RepositoryException {
        Node failing = NodeMockUtils.mockNode("website", "/root/fail");
        doThrow(new RepositoryException("fail")).when(failing).remove();
        RemoveTemplateNodesTask task = new RemoveTemplateNodesTask("app:pages/test");
        task.doNodeOperation(failing);
        verify(failing, times(1)).remove();
    }

    /**
     * Executes repository task building query and delegating to overridden executeQuery and doNodeOperations.
     */
    @Test
    public void doExecuteBuildsQueryAndDelegates() throws Exception {
        InstallContext ctx = ModuleMockUtils.mockInstallContext(InstallContextStubbingOperation.stubJcrSession(WEBSITE));
        RemoveTemplateNodesTask task = new RemoveTemplateNodesTask("app:pages/home", "/", null, "execTest");
        String query = "select * from [mgnl:page] where [mgnl:template] = 'app:pages/home'";
        Node n1 = mock(Node.class);
        Node n2 = mock(Node.class);
        ContextMockUtils.mockQueryResult(WEBSITE, Query.JCR_SQL2, query, n1, n2);
        task.doExecute(ctx);
        verify(n1).remove();
        verify(n2).remove();
        verify(ctx.getJCRSession(WEBSITE)).save();
    }
}
