package de.ibmix.magkit.query.sql2;

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

import de.ibmix.magkit.query.sql2.query.QueryRowsStatement;
import de.ibmix.magkit.query.sql2.query.QueryWorkspace;
import de.ibmix.magkit.query.sql2.query.Sql2QueryBuilder;
import de.ibmix.magkit.query.sql2.query.jcrwrapper.RowsQuery;
import de.ibmix.magkit.query.sql2.statement.Sql2Builder;
import de.ibmix.magkit.test.jcr.QueryMockUtils;
import de.ibmix.magkit.test.jcr.QueryStubbingOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import static de.ibmix.magkit.query.sql2.statement.Sql2Statement.select;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQuery;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test Sql2RowsQueryBuilder.
 *
 * @author wolf.bubenik@aperto.com
 * @since (29.04.2020)
 */
public class Sql2RowsQueryBuilderTest {

    private Query _query;
    private final Sql2Builder _statement = select("test", "other").from("mgnl:Page").orderByScore();

    @Before
    public void setUp() throws Exception {
        _query = mockQuery("website", Query.JCR_SQL2, _statement.build());
    }

    @After
    public void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void forRows() {
        assertThat(Sql2QueryBuilder.forRows(), isA(QueryWorkspace.class));
    }

    @Test
    public void fromWebsite() {
        assertThat(Sql2.Query.rowsFromWebsite(), isA(QueryRowsStatement.class));
    }

    @Test
    public void fromWorkspace() {
        assertThat(Sql2.Query.rowsFrom("test"), isA(QueryRowsStatement.class));
    }

    @Test
    public void withStatement() {
        assertThat(Sql2.Query.rowsFromWebsite().withStatement(_statement).buildRowsQuery(), isA(RowsQuery.class));
        verify(_query, times(0)).setLimit(anyLong());
        verify(_query, times(0)).setOffset(anyLong());
    }

    @Test
    public void withLimit() {
        assertThat(Sql2.Query.rowsFromWebsite().withStatement(_statement).withLimit(5).buildRowsQuery(), isA(RowsQuery.class));
        verify(_query, times(1)).setLimit(5L);
        verify(_query, times(0)).setOffset(anyLong());
    }

    @Test
    public void withOffset() {
        assertThat(Sql2.Query.rowsFromWebsite()
                .withStatement(_statement)
                .withLimit(5)
                .withOffset(5).buildRowsQuery(),
            isA(RowsQuery.class)
        );
        verify(_query, times(1)).setLimit(5L);
        verify(_query, times(1)).setOffset(5L);
    }

    @Test
    public void buildRowsQuery() {
        assertThat(Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement("test-statement")
                .buildRowsQuery(),
            isA(RowsQuery.class)
        );
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
        assertThat(Sql2QueryBuilder.forRows()
                .fromWebsite()
                .withStatement(statement)
                .withLimit(5)
                .withOffset(5)
                .getResultRows().size(),
            is(2)
        );
        verify(_query, times(1)).setOffset(5);
        verify(_query, times(1)).setLimit(5);
    }
}
