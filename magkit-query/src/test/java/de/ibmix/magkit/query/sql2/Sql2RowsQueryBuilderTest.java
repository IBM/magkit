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

import de.ibmix.magkit.query.sql2.query.QueryWorkspace;
import de.ibmix.magkit.query.sql2.query.Sql2QueryBuilder;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test Sql2RowsQueryBuilder.
 *
 * @author wolf.bubenik@ibmix.de
 * @since (29.04.2020)
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
}
