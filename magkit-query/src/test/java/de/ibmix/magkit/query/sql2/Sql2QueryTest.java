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

import de.ibmix.magkit.query.sql2.query.NodesQueryBuilder;
import de.ibmix.magkit.query.sql2.query.QueryRowsStatement;
import de.ibmix.magkit.query.sql2.query.jcrwrapper.NodesQuery;
import de.ibmix.magkit.query.sql2.query.jcrwrapper.RowsQuery;
import de.ibmix.magkit.query.sql2.statement.Sql2Builder;
import de.ibmix.magkit.query.sql2.statement.Sql2Statement;
import info.magnolia.context.MgnlContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.List;
import java.util.UUID;

import static de.ibmix.magkit.query.sql2.statement.Sql2Statement.select;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQuery;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQueryManager;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.test.jcr.query.QueryStubbingOperation.stubResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test Sql2.Query methods.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-27
 */
public class Sql2QueryTest {

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
    public void buildNodesQuery() throws RepositoryException {
        Node result1 = mockPageNode("test1", stubIdentifier(UUID.randomUUID().toString()));
        mockQuery("website", Query.JCR_SQL2, "SELECT * FROM [nt:base]", stubResult(result1));

        NodesQueryBuilder builder = Sql2.Query.nodesFromWebsite().withStatement(Sql2Statement.select());
        assertThat(builder.buildNodesQuery(), isA(NodesQuery.class));
        assertThat(builder.buildNodesQuery().getStatement(), is("SELECT * FROM [nt:base]"));
        assertThat(builder.getResultNodes().size(), is(1));
        assertThat(builder.getResultNodes().get(0), is(result1));
    }

    @Test
    public void getResultNodes() throws RepositoryException {
        Node result1 = mockPageNode("test1", stubIdentifier(UUID.randomUUID().toString()));
        Node result2 = mockPageNode("test2", stubIdentifier(UUID.randomUUID().toString()));
        mockQuery("my-workspace", Query.JCR_SQL2, "SELECT * FROM [nt:base]", stubResult(result1, result2));

        NodesQueryBuilder builder = Sql2.Query.nodesFrom("my-workspace").withStatement(Sql2.Statement.select()).withLimit(10).withOffset(5);
        assertThat(builder.getResultNodes().size(), is(2));
        assertThat(builder.getResultNodes().get(0), is(result1));
        assertThat(builder.getResultNodes().get(1), is(result2));
    }

    @Test
    public void nodesByIdentifiers() throws RepositoryException {
//        Query query = mockQuery("test", Query.JCR_SQL2, "SELECT * FROM [nt:base] WHERE ", stubResult());
        mockQueryManager("test");
        List<Node> result = Sql2.Query.nodesByIdentifiers("test");
        // don't execute query when no ids are given
        verify(MgnlContext.getJCRSession("test").getWorkspace().getQueryManager(), never()).createQuery(anyString(), anyString());
        // just return empty result
        assertThat(result.isEmpty(), is(true));

        result = Sql2.Query.nodesByIdentifiers("test", "123");

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
}
