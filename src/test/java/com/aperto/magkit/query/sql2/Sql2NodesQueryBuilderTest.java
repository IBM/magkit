package com.aperto.magkit.query.sql2;

import com.aperto.magkit.mockito.ContextMockUtils;
import com.aperto.magkit.mockito.jcr.QueryMockUtils;
import com.aperto.magkit.query.sql2.jcrwrapper.NodesQuery;
import com.aperto.magkit.query.sql2.statement.Sql2Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.UUID;

import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubIdentifier;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertThat;

/**
 * Test Sql2NodesQueryBuilder.
 *
 * @author wolf.bubenik@aperto.com
 * @since (27.04.2020)
 */
public class Sql2NodesQueryBuilderTest {

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @After
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void buildNodesQuery() throws RepositoryException {
        Node result1 = mockPageNode("test1", stubIdentifier(UUID.randomUUID().toString()));
        QueryMockUtils.mockQuery("website", Query.JCR_SQL2, "SELECT * FROM [nt:base]", result1);

        NodesQueryBuilder builder = Sql2.Query.nodesFromWebsite().withStatement(Sql2Statement.selectAll());
        assertThat(builder.buildNodesQuery(), isA(NodesQuery.class));
        assertThat(builder.buildNodesQuery().getStatement(), is("SELECT * FROM [nt:base]"));
        assertThat(builder.getResultNodes().size(), is(1));
        assertThat(builder.getResultNodes().get(0), is(result1));
    }

    @Test
    public void getResultNodes() throws RepositoryException {
        Node result1 = mockPageNode("test1", stubIdentifier(UUID.randomUUID().toString()));
        Node result2 = mockPageNode("test2", stubIdentifier(UUID.randomUUID().toString()));
        QueryMockUtils.mockQuery("website", Query.JCR_SQL2, "SELECT * FROM [nt:base]", result1, result2);

        NodesQueryBuilder builder = Sql2.Query.nodesFromWebsite().withStatement(Sql2Statement.selectAll()).withLimit(10).withOffset(5);
        assertThat(builder.getResultNodes().size(), is(2));
        assertThat(builder.getResultNodes().get(0), is(result1));
        assertThat(builder.getResultNodes().get(1), is(result2));
    }
}