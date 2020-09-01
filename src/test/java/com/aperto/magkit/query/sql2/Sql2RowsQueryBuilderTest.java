package com.aperto.magkit.query.sql2;

import com.aperto.magkit.mockito.ContextMockUtils;
import com.aperto.magkit.mockito.jcr.QueryMockUtils;
import com.aperto.magkit.mockito.jcr.QueryStubbingOperation;
import com.aperto.magkit.query.sql2.query.jcrwrapper.RowsQuery;
import com.aperto.magkit.query.sql2.query.QueryRowsStatement;
import com.aperto.magkit.query.sql2.query.QueryWorkspace;
import com.aperto.magkit.query.sql2.query.Sql2QueryBuilder;
import com.aperto.magkit.query.sql2.statement.Sql2Builder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import static com.aperto.magkit.mockito.ContextMockUtils.mockQuery;
import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockPageNode;
import static com.aperto.magkit.query.sql2.statement.Sql2Statement.selectAttributes;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;

/**
 * Test Sql2RowsQueryBuilder.
 *
 * @author wolf.bubenik@aperto.com
 * @since (29.04.2020)
 */
public class Sql2RowsQueryBuilderTest {

    private Query _query;
    private Sql2Builder _statement = selectAttributes("test", "other").from("mgnl:Page").orderByScore();

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _query = mockQuery("website", Query.JCR_SQL2, _statement.build());
    }

    @After
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
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
        Mockito.verify(_query, Mockito.times(0)).setLimit(anyLong());
        Mockito.verify(_query, Mockito.times(0)).setOffset(anyLong());
    }

    @Test
    public void withLimit() {
        assertThat(Sql2.Query.rowsFromWebsite().withStatement(_statement).withLimit(5).buildRowsQuery(), isA(RowsQuery.class));
        Mockito.verify(_query, Mockito.times(1)).setLimit(5L);
        Mockito.verify(_query, Mockito.times(0)).setOffset(anyLong());
    }

    @Test
    public void withOffset() {
        assertThat(Sql2.Query.rowsFromWebsite()
                .withStatement(_statement)
                .withLimit(5)
                .withOffset(5).buildRowsQuery(),
                isA(RowsQuery.class)
        );
        Mockito.verify(_query, Mockito.times(1)).setLimit(5L);
        Mockito.verify(_query, Mockito.times(1)).setOffset(5L);
    }

    @Test
    public void buildRowsQuery() {
        assertThat(Sql2QueryBuilder.forRows()
                        .fromWebsite()
                        .withStatement("test-statement")
                .buildRowsQuery(),
                isA(RowsQuery.class)
        );
        Mockito.verify(_query, Mockito.times(0)).setOffset(anyLong());
        Mockito.verify(_query, Mockito.times(0)).setLimit(anyLong());
    }

    @Test
    public void getResultRows() throws RepositoryException {
        Node result1 = mockPageNode("test1");
        Node result2 = mockPageNode("test2");
        QueryResult result = QueryMockUtils.mockQueryResult(result1, result2);
        String statement = selectAttributes("test", "other").from("mgnl:Page").orderByScore().build();
        _query = ContextMockUtils.mockQuery("website", Query.JCR_SQL2, statement);
        QueryStubbingOperation.stubbResult(result).of(_query);
        assertThat(Sql2QueryBuilder.forRows()
                        .fromWebsite()
                        .withStatement(
                            statement
                        )
                        .withLimit(5)
                        .withOffset(5)
                        .getResultRows().size(),
                is(2)
        );
        Mockito.verify(_query, Mockito.times(1)).setOffset(5);
        Mockito.verify(_query, Mockito.times(1)).setLimit(5);
    }
}