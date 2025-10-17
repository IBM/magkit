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

import de.ibmix.magkit.query.sql2.query.jcrwrapper.NodesQuery;
import de.ibmix.magkit.query.sql2.statement.Sql2Builder;
import info.magnolia.repository.RepositoryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;

/**
 * Concrete builder implementation for constructing and executing node-focused JCR-SQL2 queries.
 * <p>Purpose: Aggregates workspace, statement, limit and offset configuration to produce a {@link NodesQuery} capable
 * of executing and returning {@link Node} results directly (node iterator semantics vs row-based).</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Fluent workspace selection (explicit or {@link #fromWebsite()}).</li>
 *   <li>Supports raw SQL2 strings and {@link Sql2Builder} instances.</li>
 *   <li>Optional limit/offset for paging.</li>
 *   <li>Graceful error handling returning empty lists on failure.</li>
 * </ul>
 * <p>Null & error handling: Methods never return {@code null}. Repository exceptions during execution are caught and
 * logged at WARN level; empty collections are returned.</p>
 * <p>Thread-safety: NOT thread-safe. Use a new instance per logical query construction.</p>
 * <p>Usage example:</p>
 * <pre>{@code List<Node> nodes = Sql2QueryBuilder.forNodes().fromWebsite().withStatement("SELECT * FROM [mgnl:page]")
 *     .withLimit(25).getResultNodes();}</pre>
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-21
 */
public class Sql2NodesQueryBuilder extends Sql2QueryBuilder implements QueryWorkspace<QueryNodesStatement<NodesQueryBuilder>>, QueryNodesStatement<NodesQueryBuilder>, NodesQueryBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(Sql2NodesQueryBuilder.class);

    /**
     * Create a new empty builder. Workspace and statement must be configured before building/executing.
     */
    public Sql2NodesQueryBuilder() {
        super();
    }

    /**
     * Convenience selection of the Magnolia "website" workspace.
     * @return next builder step for statement specification
     */
    public QueryNodesStatement<NodesQueryBuilder> fromWebsite() {
        return fromWorkspace(RepositoryConstants.WEBSITE);
    }

    /**
     * Select the workspace by name.
     * @param workspace JCR workspace name
     * @return next builder step for statement specification
     */
    public QueryNodesStatement<NodesQueryBuilder> fromWorkspace(String workspace) {
        setWorkspace(workspace);
        return me();
    }

    /**
     * Specify a maximum number of returned nodes.
     * @param limit non-negative number of nodes (negative coerced to 0)
     * @return this builder
     */
    public NodesQueryBuilder withLimit(long limit) {
        setLimit(limit);
        return me();
    }

    /**
     * Specify an offset for paging (number of initial nodes to skip).
     * @param offset non-negative offset (negative coerced to 0)
     * @return this builder
     */
    public NodesQueryBuilder withOffset(long offset) {
        setOffset(offset);
        return me();
    }

    /**
     * Provide a programmatic statement builder.
     * @param statementBuilder builder generating final SQL2 statement
     * @return this builder
     */
    public NodesQueryBuilder withStatement(Sql2Builder statementBuilder) {
        setStatementBuilder(statementBuilder);
        return me();
    }

    /**
     * Provide a raw SQL2 statement string.
     * @param sql2 SQL2 statement
     * @return this builder
     */
    public NodesQueryBuilder withStatement(final String sql2) {
        return withStatement(() -> sql2);
    }

    /**
     * Build a {@link NodesQuery} from current builder state.
     * @return non-null {@link NodesQuery}
     */
    public NodesQuery buildNodesQuery() {
        return new NodesQuery(getQuery());
    }

    /**
     * Execute the query returning all {@link Node} instances.
     * @return non-null list of nodes (empty on error)
     */
    public List<Node> getResultNodes() {
        List<Node> nodes = Collections.emptyList();
        try {
            nodes = buildNodesQuery().execute().getNodeList();
        } catch (RepositoryException e) {
            LOG.warn("Failed to get query result nodes. Returning empty list.", e);
        }
        return nodes;
    }

    /**
     * Self-type accessor for fluent chaining.
     * @return this builder instance
     */
    public Sql2NodesQueryBuilder me() {
        return this;
    }
}
