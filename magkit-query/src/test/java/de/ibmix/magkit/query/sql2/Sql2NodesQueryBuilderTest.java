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
import de.ibmix.magkit.query.sql2.query.jcrwrapper.NodesQuery;
import de.ibmix.magkit.query.sql2.statement.Sql2Statement;
import org.junit.After;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.UUID;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQuery;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.test.jcr.query.QueryStubbingOperation.stubResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;

/**
 * Test Sql2NodesQueryBuilder.
 *
 * @author wolf.bubenik@aperto.com
 * @since (27.04.2020)
 */
public class Sql2NodesQueryBuilderTest {

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
}
