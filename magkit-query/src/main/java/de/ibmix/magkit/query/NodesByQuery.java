package de.ibmix.magkit.query;

/*-
 * #%L
 * magkit-query
 * %%
 * Copyright (C) 2023 - 2024 IBM iX
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

import de.ibmix.magkit.query.sql2.Sql2;
import de.ibmix.magkit.query.sql2.condition.Sql2StringCondition;
import de.ibmix.magkit.query.sql2.statement.Sql2Builder;
import de.ibmix.magkit.query.sql2.statement.Sql2Statement;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;
import java.util.List;
import java.util.function.Function;

/**
 * Provides a functional adapter to retrieve JCR {@link Node}s by executing a SQL2 query that matches a single property value.
 * <p>
 * Purpose:
 * Builds a JCR-SQL2 select statement for the configured node type and workspace, restricting results to nodes whose
 * property (configured at construction) equals the provided value. Designed for concise, re-usable lookups.
 * <p>
 * Main Features:
 * - Immutable configuration (workspace, node type, property name).
 * - Exact match constraint for a single String property value.
 * - Result ordering by Magnolia last modified property ({@code NodeTypes.LastModified.NAME}).
 * - Applies a fixed limit of 1 (returns at most one node) while still exposing a {@link List} result.
 * <p>
 * Usage Preconditions:
 * - The workspace and node type must exist and be readable in the current Magnolia/JCR context.
 * - The property should exist on target nodes; if not, the query yields an empty list.
 * <p>
 * Null and Error Handling:
 * - Passing {@code null} as value is discouraged; underlying builder may treat it as a literal and return no results.
 * - Repository access issues may surface as unchecked exceptions originating from underlying query utilities.
 * <p>
 * Side Effects:
 * - Read-only: executes a repository query without modifying persistent state.
 * <p>
 * Thread-Safety:
 * - Instances are immutable and therefore safe for concurrent use; the method {@link #apply(String)} performs no mutation.
 * <p>
 * Usage Example:
 * <pre>{@code
 * NodesByQuery byTitle = new NodesByQuery("website", "mgnl:page", "title");
 * List<Node> homeNodes = byTitle.apply("Home");
 * if (!homeNodes.isEmpty()) {
 *     Node home = homeNodes.get(0);
 *     // further processing
 * }
 * }</pre>
 *
 * @author frank.sommer
 * @since 02.02.2024
 */
public class NodesByQuery implements Function<String, List<Node>> {

    private final String _workspaceName;
    private final String _nodeType;
    private final String _propertyName;

    public NodesByQuery(String workspaceName, String nodeType, String propertyName) {
        _workspaceName = workspaceName;
        _nodeType = nodeType;
        _propertyName = propertyName;
    }

    /**
     * Executes the configured SQL2 query for an exact match on the configured property and returns matching nodes.
     * The result list will contain at most one {@link Node} due to the internal limit applied.
     *
     * @param value the property value to match (should be non-null)
     * @return a list containing zero or one matching node, ordered by last modified timestamp
     */
    @Override
    public List<Node> apply(String value) {
        final Sql2Builder sql2Builder = Sql2Statement.select().from(_nodeType)
            .whereAll(Sql2StringCondition.property(_propertyName).equalsAny().values(value))
            .orderBy(NodeTypes.LastModified.NAME);
        return Sql2.Query.nodesFrom(_workspaceName).withStatement(sql2Builder).withLimit(1).getResultNodes();
    }
}
