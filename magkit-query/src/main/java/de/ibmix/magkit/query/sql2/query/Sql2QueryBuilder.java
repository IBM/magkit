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

import de.ibmix.magkit.query.sql2.statement.Sql2Builder;
import info.magnolia.context.MgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

/**
 * Abstract base class for node and row query builders providing shared configuration and query creation logic.
 * <p>Purpose: Encapsulates common state (workspace, statement, limit, offset) and translates it into a JCR
 * {@link Query} object that downstream wrappers ({@code NodesQuery}/{@code RowsQuery}) can execute.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Centralized limit/offset sanitization (negative values coerced to zero).</li>
 *   <li>Flexible statement sourcing via {@link Sql2Builder} functional interface.</li>
 *   <li>Factory methods {@link #forNodes()} and {@link #forRows()} initiating fluent build chains.</li>
 * </ul>
 * <p>Usage example:</p>
 * <pre>{@code Sql2QueryBuilder.forNodes().fromWebsite().withStatement("SELECT * FROM [mgnl:page]")
 *     .withLimit(10).getResultNodes();}</pre>
 * <p>Preconditions: A workspace and statement must be set prior to calling terminal build/execute methods. The Magnolia
 * context must supply a valid JCR session.</p>
 * <p>Null and error handling: {@link #getQuery()} returns {@code null} on repository errors; callers must guard against
 * this when constructing wrappers or executing queries. Errors are logged at ERROR level.</p>
 * <p>Thread-safety: NOT thread-safe. Each instance should be used within a single thread.</p>
 * @author wolf.bubenik@ibmix.de
 * @since 2020-02-28
 **/
public abstract class Sql2QueryBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(Sql2QueryBuilder.class);

    private String _workspace;
    private long _limit = 0;
    private long _offset = 0;
    private Sql2Builder _statementBuilder;

    protected Sql2QueryBuilder(){}

    /**
     * Factory entry point for building node-oriented queries.
     * @return fluent workspace selection step
     */
    public static QueryWorkspace<QueryNodesStatement<NodesQueryBuilder>> forNodes() {
        return new Sql2NodesQueryBuilder();
    }

    /**
     * Factory entry point for building row-oriented queries.
     * @return fluent workspace selection step
     */
    public static QueryWorkspace<QueryRowsStatement<RowsQueryBuilder>> forRows() {
        return new Sql2RowsQueryBuilder();
    }

    protected void setWorkspace(String workspace) {
        _workspace = workspace;
    }

    protected void setLimit(long limit) {
        _limit = Math.max(limit, 0);
    }

    protected void setOffset(long offset) {
        _offset = Math.max(offset, 0);
    }

    /**
     * Provide the statement builder used to generate the final SQL2 string.
     * @param statementBuilder functional builder (must not be null for successful query creation)
     */
    public void setStatementBuilder(Sql2Builder statementBuilder) {
        _statementBuilder = statementBuilder;
    }

    /**
     * Create the underlying JCR {@link Query} object applying limit and offset if configured.
     * May return {@code null} on repository errors which are logged.
     * @return {@link Query} or {@code null} if creation failed
     */
    protected Query getQuery() {
        Query query = null;
        try {
            final Session jcrSession = MgnlContext.getJCRSession(_workspace);
            final QueryManager queryManager = jcrSession.getWorkspace().getQueryManager();
            query = queryManager.createQuery(_statementBuilder.build(), Query.JCR_SQL2);
            if (_limit > 0) {
                query.setLimit(_limit);
            }
            if (_offset > 0) {
                query.setOffset(_offset);
            }
        } catch (RepositoryException e) {
            LOG.error("Could not create query object. Return NULL", e);
        }
        return query;
    }
}
