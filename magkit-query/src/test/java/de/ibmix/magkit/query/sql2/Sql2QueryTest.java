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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.List;
import java.util.UUID;

import static de.ibmix.magkit.query.sql2.statement.Sql2Statement.select;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQuery;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQueryManager;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQueryResult;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.test.jcr.query.QueryStubbingOperation.stubResult;
import static org.junit.jupiter.api.Assertions.*;
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

    @BeforeEach
    public void setUp() throws Exception {
        _query = mockQuery("website", Query.JCR_SQL2, _statement.build());
    }

    @AfterEach
    public void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void buildNodesQuery() throws RepositoryException {
        Node result1 = mockPageNode("test1", stubIdentifier(UUID.randomUUID().toString()));
        mockQuery("website", Query.JCR_SQL2, "SELECT * FROM [nt:base]", stubResult(result1));

        NodesQueryBuilder builder = Sql2.Query.nodesFromWebsite().withStatement(Sql2Statement.select());
        assertInstanceOf(NodesQuery.class, builder.buildNodesQuery());
        assertEquals("SELECT * FROM [nt:base]", builder.buildNodesQuery().getStatement());
        assertEquals(1, builder.getResultNodes().size());
        assertEquals(result1, builder.getResultNodes().get(0));
    }

    @Test
    public void getResultNodes() throws RepositoryException {
        Node result1 = mockPageNode("test1", stubIdentifier(UUID.randomUUID().toString()));
        Node result2 = mockPageNode("test2", stubIdentifier(UUID.randomUUID().toString()));
        mockQuery("my-workspace", Query.JCR_SQL2, "SELECT * FROM [nt:base]", stubResult(result1, result2));

        NodesQueryBuilder builder = Sql2.Query.nodesFrom("my-workspace").withStatement(Sql2.Statement.select()).withLimit(10).withOffset(5);
        assertEquals(2, builder.getResultNodes().size());
        assertEquals(result1, builder.getResultNodes().get(0));
        assertEquals(result2, builder.getResultNodes().get(1));
    }

    @Test
    public void nodesByIdentifiers() throws RepositoryException {
        mockQueryManager("test");
        List<Node> result = Sql2.Query.nodesByIdentifiers("test");
        // don't execute query when no ids are given
        verify(MgnlContext.getJCRSession("test").getWorkspace().getQueryManager(), never()).createQuery(anyString(), anyString());
        // just return empty result
        assertTrue(result.isEmpty());

        result = Sql2.Query.nodesByIdentifiers("test", "123");
        verify(MgnlContext.getJCRSession("test").getWorkspace().getQueryManager(), times(1))
            .createQuery("SELECT * FROM [nt:base] WHERE [jcr:uuid] = '123'", Query.JCR_SQL2);
        assertEquals(0, result.size());

        mockQueryResult("test", Query.JCR_SQL2,
            "SELECT * FROM [nt:base] WHERE ([jcr:uuid] = '123' OR [jcr:uuid] = '456')",
            mockPageNode("first"), mockPageNode("second")
        );
        result = Sql2.Query.nodesByIdentifiers("test", "123", "456");
        verify(MgnlContext.getJCRSession("test").getWorkspace().getQueryManager(), times(1))
            .createQuery("SELECT * FROM [nt:base] WHERE ([jcr:uuid] = '123' OR [jcr:uuid] = '456')", Query.JCR_SQL2);
        assertEquals(2, result.size());
    }

    @Test
    public void nodesByTemplates() throws RepositoryException {
        mockQueryManager("website");
        List<Node> result = Sql2.Query.nodesByTemplates("/root");
        // don't execute query when no ids are given
        verify(MgnlContext.getJCRSession("website").getWorkspace().getQueryManager(), never()).createQuery(anyString(), anyString());
        // just return empty result
        assertTrue(result.isEmpty());

        result = Sql2.Query.nodesByTemplates("/root", "temp1");
        verify(MgnlContext.getJCRSession("website").getWorkspace().getQueryManager(), times(1))
            .createQuery("SELECT * FROM [nt:base] WHERE (isdescendantnode('/root') AND [mgnl:template] = 'temp1')", Query.JCR_SQL2);
        assertEquals(0, result.size());

        mockQueryResult("website", Query.JCR_SQL2,
            "SELECT * FROM [nt:base] WHERE (isdescendantnode('/root') AND ([mgnl:template] = 'temp1' OR [mgnl:template] = 'temp2'))",
            mockPageNode("first"), mockPageNode("second")
        );
        result = Sql2.Query.nodesByTemplates("/root", "temp1", "temp2");
        verify(MgnlContext.getJCRSession("website").getWorkspace().getQueryManager(), times(1))
            .createQuery("SELECT * FROM [nt:base] WHERE (isdescendantnode('/root') AND ([mgnl:template] = 'temp1' OR [mgnl:template] = 'temp2'))", Query.JCR_SQL2);
        assertEquals(2, result.size());
    }

    @Test
    public void fromWebsite() {
        assertInstanceOf(QueryRowsStatement.class, Sql2.Query.rowsFromWebsite());
    }

    @Test
    public void fromWorkspace() {
        assertInstanceOf(QueryRowsStatement.class, Sql2.Query.rowsFrom("test"));
    }

    @Test
    public void withStatement() {
        assertInstanceOf(RowsQuery.class, Sql2.Query.rowsFromWebsite().withStatement(_statement).buildRowsQuery());
        verify(_query, times(0)).setLimit(anyLong());
        verify(_query, times(0)).setOffset(anyLong());
    }

    @Test
    public void withLimit() {
        assertInstanceOf(RowsQuery.class, Sql2.Query.rowsFromWebsite().withStatement(_statement).withLimit(5).buildRowsQuery());
        verify(_query, times(1)).setLimit(5L);
        verify(_query, times(0)).setOffset(anyLong());
    }

    @Test
    public void withOffset() {
        assertInstanceOf(RowsQuery.class, Sql2.Query.rowsFromWebsite()
                .withStatement(_statement)
                .withLimit(5)
                .withOffset(5).buildRowsQuery());
        verify(_query, times(1)).setLimit(5L);
        verify(_query, times(1)).setOffset(5L);
    }
}
