package de.ibmix.magkit.query.sql2.query.jcrwrapper;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import java.util.List;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.jcr.query.QueryStubbingOperation.stubResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NodesQuery} verifying execution delegation and exception propagation.
 * Test goals:
 * <ul>
 *   <li>Verify successful execution returns a {@link NodesResult} containing underlying node iterator.</li>
 *   <li>Verify propagation of {@link RepositoryException} and {@link InvalidQueryException} thrown by {@link Query#execute()}.</li>
 * </ul>
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-29
 */
class NodesQueryTest {
    /**
     * Verifies execute delegates to underlying query and wraps returned result providing node iteration.
     */
    @Test
    @DisplayName("execute delegates and returns NodesResult with iterable nodes")
    void testExecuteReturnsNodesResult() throws RepositoryException {
        Node node1 = mockNode("test", "/root/a", stubProperty("name", "value1"));
        Node node2 = mockNode("test", "/root/b", stubProperty("name", "value2"));
        Query query = ContextMockUtils.mockQuery("test", Query.JCR_SQL2, "SELECT * FROM [nt:base]", stubResult(node1, node2));

        NodesQuery nodesQuery = new NodesQuery(query);
        NodesResult nodesResult = nodesQuery.execute();
        assertNotNull(nodesResult);
        List<Node> collected = nodesResult.getNodeList();
        assertEquals(2, collected.size());
        assertEquals(node1.getPath(), collected.get(0).getPath());
        assertEquals(node2.getPath(), collected.get(1).getPath());
        verify(query).execute();
    }

    /**
     * Verifies execute propagates RepositoryException from underlying query.
     */
    @Test
    @DisplayName("execute propagates RepositoryException")
    void testExecutePropagatesRepositoryException() throws RepositoryException {
        Query query = mock(Query.class);
        when(query.execute()).thenThrow(new RepositoryException("failure"));
        NodesQuery nodesQuery = new NodesQuery(query);
        RepositoryException ex = assertThrows(RepositoryException.class, nodesQuery::execute);
        assertEquals("failure", ex.getMessage());
        verify(query).execute();
    }

    /**
     * Verifies execute propagates InvalidQueryException from underlying query.
     */
    @Test
    @DisplayName("execute propagates InvalidQueryException")
    void testExecutePropagatesInvalidQueryException() throws RepositoryException {
        Query query = mock(Query.class);
        when(query.execute()).thenThrow(new InvalidQueryException("invalid"));
        NodesQuery nodesQuery = new NodesQuery(query);
        InvalidQueryException ex = assertThrows(InvalidQueryException.class, nodesQuery::execute);
        assertEquals("invalid", ex.getMessage());
        verify(query).execute();
    }
}
