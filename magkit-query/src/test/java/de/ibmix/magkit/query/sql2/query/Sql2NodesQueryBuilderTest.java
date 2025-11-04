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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import java.util.List;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQuery;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.query.QueryStubbingOperation.stubResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Sql2NodesQueryBuilder} covering negative limit/offset handling, raw string statement usage and
 * graceful error handling on {@link RepositoryException} during query execution.
 *
 * Focus areas:
 * <ul>
 *   <li>Negative limit and offset values are coerced to zero and not applied to underlying {@link Query}.</li>
 *   <li>withStatement(String) overload produces expected statement value.</li>
 *   <li>getResultNodes() returns empty list when underlying query execution throws {@link RepositoryException}.</li>
 * </ul>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-29
 */
public class Sql2NodesQueryBuilderTest {

    private static final String SELECT_ALL = "SELECT * FROM [nt:base]";

    @AfterEach
    public void tearDown() throws Exception {
        cleanContext();
    }

    /**
     * Ensures negative limit and offset values are sanitized to zero resulting in no setLimit/setOffset invocation.
     */
    @Test
    public void negativeLimitAndOffsetAreIgnored() throws RepositoryException {
        Query query = mockQuery("website", Query.JCR_SQL2, SELECT_ALL);
        Sql2NodesQueryBuilder builder = new Sql2NodesQueryBuilder();
        builder.fromWebsite();
        builder.withStatement(SELECT_ALL);
        builder.withLimit(-5);
        builder.withOffset(-10);
        builder.buildNodesQuery();
        verify(query, never()).setLimit(anyLong());
        verify(query, never()).setOffset(anyLong());
        assertEquals(SELECT_ALL, builder.buildNodesQuery().getStatement());
    }

    /**
     * Verifies getResultNodes returns empty list when query.execute throws RepositoryException, without propagating the exception.
     */
    @Test
    public void getResultNodesReturnsEmptyListOnRepositoryException() throws RepositoryException {
        Node n1 = mockNode("website", "/node1");
        Query brokenQuery = mockQuery("website", Query.JCR_SQL2, SELECT_ALL, stubResult(n1));
        when(brokenQuery.execute()).thenThrow(new RepositoryException("boom"));
        Sql2NodesQueryBuilder builder = new Sql2NodesQueryBuilder();
        builder.fromWebsite();
        builder.withStatement(SELECT_ALL);
        List<?> result = builder.getResultNodes();
        assertTrue(result.isEmpty());
    }
}
