/*-
 * #%L
 * magkit-query
 * %%
 * Copyright (C) 2023 - 2025 IBM iX
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

package de.ibmix.magkit.query.sql2.query.jcrwrapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.List;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.query.QueryMockUtils.mockQueryResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link NodesResult} verifying node iteration and list aggregation behaviors.
 * Test goals:
 * <ul>
 *   <li>Verify getNodes returns iterator delivered by underlying {@link QueryResult}.</li>
 *   <li>Verify getNodes returns empty iterator on {@link RepositoryException}.</li>
 *   <li>Verify getNodeList collects all nodes preserving order.</li>
 *   <li>Verify getNodeList returns empty list when no nodes are present.</li>
 * </ul>
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-29
 */
class NodesResultTest {
    /**
     * Verifies getNodes returns underlying iterator (single invocation) and iteration order.
     */
    @Test
    @DisplayName("getNodes returns underlying iterator")
    void testGetNodes() throws RepositoryException {
        Node node1 = mockNode("query", "/root/a");
        Node node2 = mockNode("query", "/root/b");
        QueryResult result = mockQueryResult("test", Query.JCR_SQL2, "SELECT * FROM [nt:base]", node1, node2);
        NodesResult nodesResult = new NodesResult(result);
        NodeIterator it = (NodeIterator) nodesResult.getNodes();
        assertNotNull(it);
        assertEquals(node1, it.nextNode());
        assertEquals(node2, it.nextNode());
        verify(result).getNodes();
    }

    /**
     * Verifies getNodeList aggregates nodes and calls underlying getNodes exactly once.
     */
    @Test
    @DisplayName("getNodeList aggregates nodes preserving order")
    void testGetNodeListAggregates() throws RepositoryException {
        Node node1 = mockNode("query", "/root/a");
        Node node2 = mockNode("query", "/root/b");
        QueryResult result = mockQueryResult("test", Query.JCR_SQL2, "SELECT * FROM [nt:base]", node1, node2);
        NodesResult nodesResult = new NodesResult(result);
        List<Node> collected = nodesResult.getNodeList();
        assertEquals(2, collected.size());
        assertEquals(node1, collected.get(0));
        assertEquals(node2, collected.get(1));
        verify(result).getNodes();
    }

    /**
     * Verifies getNodes returns empty iterator on RepositoryException.
     */
    @Test
    @DisplayName("getNodes returns empty iterator on RepositoryException")
    void testGetNodesException() throws RepositoryException {
        QueryResult result = mock(QueryResult.class);
        doThrow(new RepositoryException("failure")).when(result).getNodes();
        NodesResult nodesResult = new NodesResult(result);
        java.util.Iterator<Node> iterator = nodesResult.getNodes();
        assertNotNull(iterator);
        assertFalse(iterator.hasNext());
    }

    /**
     * Verifies getNodeList returns empty list when underlying iterator has no elements.
     */
    @Test
    @DisplayName("getNodeList returns empty list when iterator empty")
    void testGetNodeListEmpty() throws RepositoryException {
        QueryResult result = mockQueryResult("test", Query.JCR_SQL2, "SELECT * FROM [nt:base]");
        NodesResult nodesResult = new NodesResult(result);
        List<Node> collected = nodesResult.getNodeList();
        assertNotNull(collected);
        assertEquals(0, collected.size());
        verify(result).getNodes();
    }
}
