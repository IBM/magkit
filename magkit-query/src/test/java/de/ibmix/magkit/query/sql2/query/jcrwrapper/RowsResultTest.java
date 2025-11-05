package de.ibmix.magkit.query.sql2.query.jcrwrapper;

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

import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import java.util.Iterator;
import java.util.List;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubNode;
import static de.ibmix.magkit.test.jcr.query.QueryMockUtils.mockQueryResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

/**
 * Unit tests for {@link RowsResult} covering all branches and error handling scenarios.
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-29
 */
public class RowsResultTest {

    /**
     * Verifies that getRows() returns an empty iterator when the underlying QueryResult throws a RepositoryException.
     */
    @Test
    public void shouldReturnEmptyIteratorWhenRepositoryExceptionThrownInGetRows() throws RepositoryException {
        QueryResult failingResult = mockQueryResult("testRepository", Query.JCR_SQL2, "SELECT * FROM [nt:base]");
        doThrow(new RepositoryException("failure")).when(failingResult).getRows();
        RowsResult rowsResult = new RowsResult(failingResult);
        Iterator<Row> rows = rowsResult.getRows();
        assertNotNull(rows);
        assertFalse(rows.hasNext());
    }

    /**
     * Verifies that getRowList() collects all rows provided by the underlying result.
     */
    @Test
    public void shouldCollectAllRowsInGetRowList() throws RepositoryException {
        Node n1 = mockNode("testRepository", "/node1");
        Node n2 = mockNode("testRepository", "/node2");
        Node n3 = mockNode("testRepository", "/node3");
        QueryResult result = mockQueryResult("testRepository", Query.JCR_SQL2, "SELECT * FROM [nt:base]", n1, n2, n3);
        RowsResult rowsResult = new RowsResult(result);
        List<Row> collected = rowsResult.getRowList();
        assertEquals(3, collected.size());
    }

    /**
     * Verifies that getLeftRowNodeList() returns an empty list when there are no selector names.
     */
    @Test
    public void shouldReturnEmptyListForLeftSelectorWhenNoSelectors() throws RepositoryException {
        Node n1 = mockNode("testRepository", "/node1", stubNode("leftSelector"), stubNode("rightSelector"));
        Node n2 = mockNode("testRepository", "/node2", stubNode("leftSelector"));
        Node n3 = mockNode("testRepository", "/node2", stubNode("rightSelector"));
        QueryResult result = mockQueryResult("testRepository", Query.JCR_SQL2, "SELECT * FROM [nt:base]", n1, n2, n3);
        RowsResult rowsResult = new RowsResult(result);
        List<Node> nodes = rowsResult.getLeftRowNodeList();
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty());
    }

    /**
     * Verifies that getLeftRowNodeList() resolves nodes for the first selector.
     */
    @Test
    public void shouldReturnLeftNodesForFirstSelector() throws RepositoryException {
        Node n1 = mockNode("testRepository", "/node1", stubNode("leftSelector"), stubNode("rightSelector"));
        Node n2 = mockNode("testRepository", "/node2", stubNode("leftSelector"));
        Node n3 = mockNode("testRepository", "/node3", stubNode("rightSelector"));
        QueryResult result = mockQueryResult("testRepository", Query.JCR_SQL2, "SELECT * FROM [nt:base]", n1, n2, n3);
        doReturn(new String[]{"leftSelector"}).when(result).getSelectorNames();
        RowsResult rowsResult = new RowsResult(result);
        List<Node> nodes = rowsResult.getLeftRowNodeList();
        assertEquals(2, nodes.size());
        assertEquals("/node1/leftSelector", nodes.get(0).getPath());
        assertEquals("/node2/leftSelector", nodes.get(1).getPath());
    }

    /**
     * Verifies that getRightRowNodeList() returns an empty list when there is fewer than two selectors.
     */
    @Test
    public void shouldReturnEmptyListForRightSelectorWhenLessThanTwoSelectors() throws RepositoryException {
        Node n1 = mockNode("testRepository", "/node1", stubNode("leftSelector"), stubNode("rightSelector"));
        Node n2 = mockNode("testRepository", "/node2", stubNode("leftSelector"));
        Node n3 = mockNode("testRepository", "/node3", stubNode("rightSelector"));
        QueryResult result = mockQueryResult("testRepository", Query.JCR_SQL2, "SELECT * FROM [nt:base]", n1, n2, n3);
        doReturn(new String[]{"leftSelector"}).when(result).getSelectorNames();
        RowsResult rowsResult = new RowsResult(result);
        List<Node> nodes = rowsResult.getRightRowNodeList();
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty());
    }

    /**
     * Verifies that getRightRowNodeList() resolves nodes for the second selector.
     */
    @Test
    public void shouldReturnRightNodesForSecondSelector() throws RepositoryException {
        Node n1 = mockNode("testRepository", "/node1", stubNode("leftSelector"), stubNode("rightSelector"));
        Node n2 = mockNode("testRepository", "/node2", stubNode("leftSelector"));
        Node n3 = mockNode("testRepository", "/node3", stubNode("rightSelector"));
        QueryResult result = mockQueryResult("testRepository", Query.JCR_SQL2, "SELECT * FROM [nt:base]", n1, n2, n3);
        doReturn(new String[]{"leftSelector", "rightSelector"}).when(result).getSelectorNames();
        RowsResult rowsResult = new RowsResult(result);
        List<Node> nodes = rowsResult.getRightRowNodeList();
        assertEquals(2, nodes.size());
        assertEquals("/node1/rightSelector", nodes.get(0).getPath());
        assertEquals("/node3/rightSelector", nodes.get(1).getPath());
    }

    /**
     * Verifies that getRowNodeListFor() returns an empty list for blank selector arguments.
     */
    @Test
    public void shouldReturnEmptyListForBlankSelector() throws RepositoryException {
        Node n1 = mockNode("testRepository", "/node1", stubNode("leftSelector"), stubNode("rightSelector"));
        Node n2 = mockNode("testRepository", "/node2", stubNode("leftSelector"));
        Node n3 = mockNode("testRepository", "/node3", stubNode("rightSelector"));
        QueryResult result = mockQueryResult("testRepository", Query.JCR_SQL2, "SELECT * FROM [nt:base]", n1, n2, n3);
        doReturn(new String[]{"leftSelector", "rightSelector"}).when(result).getSelectorNames();
        RowsResult rowsResult = new RowsResult(result);
        assertTrue(rowsResult.getRowNodeListFor(null).isEmpty());
        assertTrue(rowsResult.getRowNodeListFor("").isEmpty());
        assertTrue(rowsResult.getRowNodeListFor("   ").isEmpty());
        assertTrue(rowsResult.getRowNodeListFor("unknown").isEmpty());
    }

    /**
     * Verifies that getRowNodeListFor() omits nodes from rows that throw RepositoryException while still collecting others.
     */
    @Test
    public void shouldReturnOnlySuccessfulNodesWhenSomeRowsThrowRepositoryException() throws RepositoryException {
        Node n1 = mockNode("testRepository", "/node1", stubNode("leftSelector"), stubNode("rightSelector"));
        Node n2 = mockNode("testRepository", "/node2", stubNode("leftSelector"));
        Node n3 = mockNode("testRepository", "/node3", stubNode("rightSelector"));
        doThrow(new RepositoryException("failure")).when(n1).getNode("leftSelector");
        QueryResult result = mockQueryResult("testRepository", Query.JCR_SQL2, "SELECT * FROM [nt:base]", n1, n2, n3);
        doReturn(new String[]{"leftSelector", "rightSelector"}).when(result).getSelectorNames();
        RowsResult rowsResult = new RowsResult(result);
        List<Node> nodes = rowsResult.getRowNodeListFor("leftSelector");
        assertEquals(1, nodes.size());
        assertEquals("/node2/leftSelector", nodes.get(0).getPath());
    }

    /**
     * Verifies left selector accessor returns empty list when selector array is empty.
     */
    @Test
    public void shouldReturnEmptyLeftListWhenSelectorArrayEmpty() throws RepositoryException {
        Node n1 = mockNode("testRepository", "/node1", stubNode("leftSelector"), stubNode("rightSelector"));
        Node n2 = mockNode("testRepository", "/node2", stubNode("leftSelector"));
        Node n3 = mockNode("testRepository", "/node3", stubNode("rightSelector"));
        QueryResult result = mockQueryResult("testRepository", Query.JCR_SQL2, "SELECT * FROM [nt:base]", n1, n2, n3);
        doReturn(new String[0]).when(result).getSelectorNames();
        RowsResult rowsResult = new RowsResult(result);
        List<Node> nodes = rowsResult.getLeftRowNodeList();
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty());
    }

    /**
     * Verifies getRowList returns empty list when underlying getRows fails.
     */
    @Test
    public void shouldReturnEmptyRowListWhenUnderlyingGetRowsFails() throws RepositoryException {
        Node n1 = mockNode("testRepository", "/node1", stubNode("leftSelector"), stubNode("rightSelector"));
        Node n2 = mockNode("testRepository", "/node2", stubNode("leftSelector"));
        Node n3 = mockNode("testRepository", "/node3", stubNode("rightSelector"));
        QueryResult result = mockQueryResult("testRepository", Query.JCR_SQL2, "SELECT * FROM [nt:base]", n1, n2, n3);
        doThrow(new RepositoryException()).when(result).getRows();
        RowsResult rowsResult = new RowsResult(result);
        List<Row> rows = rowsResult.getRowList();
        assertNotNull(rows);
        assertTrue(rows.isEmpty());
    }
}
