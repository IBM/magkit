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
 * Concrete builder implementation for constructing and executing row-focused JCR-SQL2 queries.
 * <p>Purpose: Aggregates workspace, statement, limit and offset configuration to produce a {@link RowsQuery} capable
 * of executing and returning {@link Row} results (row iterator semantics with selector access).</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Fluent workspace selection (explicit or {@link #fromWebsite()}).</li>
 *   <li>Supports raw SQL2 strings and {@link Sql2Builder} instances.</li>
 *   <li>Optional limit/offset for paging.</li>
 *   <li>Graceful error handling returning empty collections or false on failure.</li>
 * </ul>
 * <p>Null and error handling: Methods never return {@code null}. Repository exceptions during execution are caught and
 * logged at WARN level; empty collections or {@code false} are returned.</p>
 * <p>Thread-safety: NOT thread-safe. Use a new instance per logical query construction.</p>
 * <p>Usage example:</p>
 * <pre>{@code boolean hasRows = Sql2QueryBuilder.forRows().fromWebsite().withStatement("SELECT * FROM [mgnl:page]")
 *     .withLimit(10).hasResultRows();}</pre>
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-21
 */
public class Sql2RowsQueryBuilder extends Sql2QueryBuilder implements QueryWorkspace<QueryRowsStatement<RowsQueryBuilder>>, QueryRowsStatement<RowsQueryBuilder>, RowsQueryBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(Sql2RowsQueryBuilder.class);

    /**
     * Create a new empty builder. Workspace and statement must be configured before building/executing.
     */
    public Sql2RowsQueryBuilder() {
        super();
    }

    /**
     * Convenience selection of the Magnolia "website" workspace.
     * @return next builder step for statement specification
     */
    public QueryRowsStatement<RowsQueryBuilder> fromWebsite() {
        return fromWorkspace(RepositoryConstants.WEBSITE);
    }

    /**
     * Select the workspace by name.
     * @param workspace JCR workspace name
     * @return next builder step for statement specification
     */
    public QueryRowsStatement<RowsQueryBuilder> fromWorkspace(String workspace) {
        setWorkspace(workspace);
        return me();
    }

    /**
     * Specify a maximum number of returned rows.
     * @param limit non-negative number of rows (negative coerced to 0)
     * @return this builder
     */
    public RowsQueryBuilder withLimit(long limit) {
        setLimit(limit);
        return me();
    }

    /**
     * Specify an offset for paging (number of initial rows to skip).
     * @param offset non-negative offset (negative coerced to 0)
     * @return this builder
     */
    public RowsQueryBuilder withOffset(long offset) {
        setOffset(offset);
        return me();
    }

    /**
     * Provide a programmatic statement builder.
     * @param statementBuilder builder generating final SQL2 statement
     * @return this builder
     */
    public RowsQueryBuilder withStatement(Sql2Builder statementBuilder) {
        setStatementBuilder(statementBuilder);
        return me();
    }

    /**
     * Provide a raw SQL2 statement string.
     * @param sql2 SQL2 statement
     * @return this builder
     */
    public RowsQueryBuilder withStatement(final String sql2) {
        return withStatement(() -> sql2);
    }

    /**
     * Build a {@link RowsQuery} from current builder state.
     * @return non-null {@link RowsQuery}
     */
    public RowsQuery buildRowsQuery() {
        return new RowsQuery(getQuery());
    }

    /**
     * Execute the query returning all {@link Row} instances.
     * @return non-null list of rows (empty on error)
     */
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
     * @return {@code true} if at least one row exists; {@code false} otherwise or on error
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

    /**
     * Self-type accessor for fluent chaining.
     * @return this builder instance
     */
    public Sql2RowsQueryBuilder me() {
        return this;
    }
}
