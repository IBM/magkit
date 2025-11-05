package de.ibmix.magkit.query.sql2.query;

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

import de.ibmix.magkit.query.sql2.query.jcrwrapper.RowsQuery;
import de.ibmix.magkit.query.sql2.statement.Sql2Builder;
import de.ibmix.magkit.test.jcr.query.QueryMockUtils;
import de.ibmix.magkit.test.jcr.query.QueryStubbingOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import static de.ibmix.magkit.query.sql2.statement.Sql2Statement.select;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQuery;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test Sql2RowsQueryBuilder.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-29
 */
public class Sql2RowsQueryBuilderTest {

    private Query _query;
    private final Sql2Builder _statement = select("test", "other").from("mgnl:Page").orderByScore();

    @BeforeEach
    public void setUp() throws Exception {
        _query = mockQuery("website", Query.JCR_SQL2, _statement.build());
    }

    @AfterEach
    public void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void forRows() {
        assertInstanceOf(QueryWorkspace.class, Sql2QueryBuilder.forRows());
    }

    @Test
    public void buildRowsQuery() {
        assertInstanceOf(RowsQuery.class, Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement("test-statement")
                .buildRowsQuery());
        verify(_query, times(0)).setOffset(anyLong());
        verify(_query, times(0)).setLimit(anyLong());
    }

    @Test
    public void getResultRows() throws RepositoryException {
        Node result1 = mockPageNode("test1");
        Node result2 = mockPageNode("test2");
        QueryResult result = QueryMockUtils.mockQueryResult(result1, result2);
        String statement = select("test", "other").from("mgnl:Page").orderByScore().build();
        _query = mockQuery("website", Query.JCR_SQL2, statement);
        QueryStubbingOperation.stubResult(result).of(_query);
        assertEquals(2, Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement(statement)
                .withLimit(5)
                .withOffset(5)
                .getResultRows().size());
        verify(_query, times(1)).setOffset(5);
        verify(_query, times(1)).setLimit(5);
    }

    @Test
    public void negativeLimitOffsetAreSanitized() throws RepositoryException {
        String statement = _statement.build();
        _query = mockQuery("website", Query.JCR_SQL2, statement);
        Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement(statement)
                .withLimit(-5)
                .withOffset(-10)
                .buildRowsQuery();
        verify(_query, times(0)).setLimit(anyLong());
        verify(_query, times(0)).setOffset(anyLong());
    }

    @Test
    public void zeroLimitOffsetNoCalls() throws RepositoryException {
        String statement = _statement.build();
        _query = mockQuery("website", Query.JCR_SQL2, statement);
        Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement(statement)
                .withLimit(0)
                .withOffset(0)
                .buildRowsQuery();
        verify(_query, times(0)).setLimit(anyLong());
        verify(_query, times(0)).setOffset(anyLong());
    }

    @Test
    public void onlyOffsetApplied() throws RepositoryException {
        Node result1 = mockPageNode("test1");
        QueryResult result = QueryMockUtils.mockQueryResult(result1);
        String statement = _statement.build();
        _query = mockQuery("website", Query.JCR_SQL2, statement);
        QueryStubbingOperation.stubResult(result).of(_query);
        Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement(statement)
                .withOffset(7)
                .getResultRows();
        verify(_query, times(1)).setOffset(7);
        verify(_query, times(0)).setLimit(anyLong());
    }

    @Test
    public void onlyLimitApplied() throws RepositoryException {
        Node result1 = mockPageNode("test1");
        QueryResult result = QueryMockUtils.mockQueryResult(result1);
        String statement = _statement.build();
        _query = mockQuery("website", Query.JCR_SQL2, statement);
        QueryStubbingOperation.stubResult(result).of(_query);
        Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement(statement)
                .withLimit(11)
                .getResultRows();
        verify(_query, times(0)).setOffset(anyLong());
        verify(_query, times(1)).setLimit(11);
    }

    @Test
    public void hasResultRowsReturnsTrue() throws RepositoryException {
        Node result1 = mockPageNode("test1");
        QueryResult result = QueryMockUtils.mockQueryResult(result1);
        String statement = _statement.build();
        _query = mockQuery("website", Query.JCR_SQL2, statement);
        QueryStubbingOperation.stubResult(result).of(_query);
        assertTrue(Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement(statement)
                .hasResultRows());
    }

    @Test
    public void hasResultRowsReturnsFalseOnEmptyResult() throws RepositoryException {
        QueryResult result = QueryMockUtils.mockQueryResult();
        String statement = _statement.build();
        _query = mockQuery("website", Query.JCR_SQL2, statement);
        QueryStubbingOperation.stubResult(result).of(_query);
        assertFalse(Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement(statement)
                .hasResultRows());
    }

    @Test
    public void hasResultRowsReturnsFalseOnRepositoryException() throws RepositoryException {
        String statement = _statement.build();
        _query = mockQuery("website", Query.JCR_SQL2, statement);
        when(_query.execute()).thenThrow(new RepositoryException("test"));
        assertFalse(Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement(statement)
                .hasResultRows());
    }

    @Test
    public void getResultRowsReturnsEmptyListOnRepositoryException() throws RepositoryException {
        String statement = _statement.build();
        _query = mockQuery("website", Query.JCR_SQL2, statement);
        when(_query.execute()).thenThrow(new RepositoryException("test"));
        assertEquals(0, Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement(statement)
                .getResultRows().size());
    }

    @Test
    public void withStatementBuilderOverload() throws RepositoryException {
        String statement = _statement.build();
        _query = mockQuery("website", Query.JCR_SQL2, statement);
        assertInstanceOf(RowsQuery.class, Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement(_statement)
                .buildRowsQuery());
        verify(_query, times(0)).setLimit(anyLong());
        verify(_query, times(0)).setOffset(anyLong());
    }
}
