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

import javax.jcr.Node;
import java.util.List;

/**
 * Final builder step interface for creating and executing node-oriented SQL2 queries.
 * <p>Purpose: Defines terminal operations to build a {@link NodesQuery} and obtain the resulting {@link Node} list.
 * It complements the row-oriented {@link RowsQueryBuilder} and separates construction concerns from execution.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Build a type-safe {@link NodesQuery} wrapper from the configured state.</li>
 *   <li>Execute the query and collect resulting JCR {@link Node} instances.</li>
 * </ul>
 * <p>Null and error handling: Implementations should never return {@code null}; on repository access issues they
 * typically return an empty list and log a warning.</p>
 * <p>Thread-safety: Implementations are NOT thread-safe; use per request / per operation.</p>
 * <p>Usage example:</p>
 * <pre>{@code List<Node> nodes = Sql2QueryBuilder.forNodes().fromWebsite().withStatement("SELECT * FROM [mgnl:page]")
 *     .withLimit(50).buildNodesQuery().execute().getNodeList();}</pre>
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-27
 */
public interface NodesQueryBuilder extends QueryLimit<NodesQueryBuilder> {
    /**
     * Build a {@link NodesQuery} instance from the accumulated builder configuration (workspace, statement, limit).
     * @return non-null {@link NodesQuery} ready for execution
     */
    NodesQuery buildNodesQuery();

    /**
     * Execute the query and return all resulting {@link Node} objects.
     * Implementations return an empty list if execution fails.
     * @return non-null list of nodes (possibly empty)
     */
    List<Node> getResultNodes();
}
