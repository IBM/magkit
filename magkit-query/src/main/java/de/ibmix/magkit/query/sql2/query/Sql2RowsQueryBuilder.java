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
import info.magnolia.repository.RepositoryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.Row;
import java.util.Collections;
import java.util.List;

/**
 * The builder class for Row queries.
 *
 * @author wolf.bubenik@aperto.com
 * @since (21.8.2020)
 */
public class Sql2RowsQueryBuilder extends Sql2QueryBuilder implements QueryWorkspace<QueryRowsStatement<RowsQueryBuilder>>, QueryRowsStatement<RowsQueryBuilder>, RowsQueryBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(Sql2RowsQueryBuilder.class);

    public Sql2RowsQueryBuilder() {
        super();
    }

    public QueryRowsStatement<RowsQueryBuilder> fromWebsite() {
        return fromWorkspace(RepositoryConstants.WEBSITE);
    }

    public QueryRowsStatement<RowsQueryBuilder> fromWorkspace(String workspace) {
        setWorkspace(workspace);
        return me();
    }

    public RowsQueryBuilder withLimit(long limit) {
        setLimit(limit);
        return me();
    }

    public RowsQueryBuilder withOffset(long offset) {
        setOffset(offset);
        return me();
    }

    public RowsQueryBuilder withStatement(Sql2Builder statementBuilder) {
        setStatementBuilder(statementBuilder);
        return me();
    }

    public RowsQueryBuilder withStatement(final String sql2) {
        return withStatement(() -> sql2);
    }

    public RowsQuery buildRowsQuery() {
        return new RowsQuery(getQuery());
    }

    public List<Row> getResultRows() {
        List<Row> rows = Collections.emptyList();
        try {
            rows = buildRowsQuery().execute().getRowList();
        } catch (RepositoryException e) {
            LOG.warn("Failed to get query result rows. Returning empty list.", e);
        }
        return rows;
    }

    /**
     * Execute the query and check if the result is not empty.
     *
     * @return true, if the query result RowIterator contains rows, false otherwise.
     */
    public boolean hasResultRows() {
        boolean result = false;
        try {
            result = buildRowsQuery().execute().getRows().hasNext();
        } catch (RepositoryException e) {
            LOG.warn("Failed to get query result rows. Returning empty list.", e);
        }
        return result;
    }

    public Sql2RowsQueryBuilder me() {
        return this;
    }
}
