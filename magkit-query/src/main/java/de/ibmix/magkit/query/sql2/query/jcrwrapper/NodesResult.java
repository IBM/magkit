package de.ibmix.magkit.query.sql2.query.jcrwrapper;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Specialized {@link ResultWrapper} exposing node-oriented access to a {@link javax.jcr.query.QueryResult}.
 * <p>Purpose: Provide a focused API for iterating {@link javax.jcr.Node} results returned directly by a JCR-SQL2 or
 * QOM query (as opposed to row-based access). This complements {@link RowsResult}.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Safe iteration over result nodes with graceful degradation (empty iterator if errors occur).</li>
 *   <li>Convenience conversion of the iterator to a {@link List} while preserving iteration order.</li>
 *   <li>Consistent non-null return contracts.</li>
 * </ul>
 * <p>Null & error handling: All accessors return non-null collections (possibly empty). Repository access issues are
 * caught and logged at WARN level.</p>
 * <p>Thread-safety: Instances are NOT thread-safe. Consume in a single thread; do not share concurrently without
 * external synchronization.</p>
 * <p>Side effects: Only logging; underlying {@code QueryResult} is read-only.</p>
 * <p>Usage example:</p>
 * <pre>{@code List<Node> nodes = new NodesQuery(query).execute().getNodeList();}</pre>
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-21
 */
public class NodesResult extends ResultWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodesResult.class);

    /**
     * Create a new node-oriented result wrapper.
     * @param result underlying JCR query result
     */
    public NodesResult(QueryResult result) {
        super(result);
    }

    /**
     * Obtain an iterator over all {@link Node} objects in their query-defined order.
     * Returns an empty iterator if the underlying JCR call fails.
     * @return non-null iterator of nodes (possibly empty)
     */
    @SuppressWarnings("unchecked")
    public Iterator<Node> getNodes() {
        Iterator<Node> nodes = Collections.emptyIterator();
        try {
            nodes = getResult().getNodes();
        } catch (RepositoryException e) {
            LOGGER.warn("Failed to get query result rows.", e);
        }
        return nodes;
    }

    /**
     * Collect all {@link Node} objects into a {@link List} preserving iteration order.
     * @return non-null list of nodes (possibly empty)
     */
    public List<Node> getNodeList() {
        Iterator<Node> iterator = getNodes();
        List<Node> rows = new ArrayList<>();
        while (iterator.hasNext()) {
            rows.add(iterator.next());
        }
        return rows;
    }
}
