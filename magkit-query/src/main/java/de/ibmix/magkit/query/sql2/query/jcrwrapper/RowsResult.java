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
import javax.jcr.query.Row;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Specialized {@link ResultWrapper} exposing row-oriented access to a {@link javax.jcr.query.QueryResult}.
 * <p>Purpose: Provide convenience methods to iterate and collect {@link javax.jcr.query.Row} objects as well as
 * resolve {@link javax.jcr.Node} instances from individual row selectors ("left" / "right" join sides or arbitrary
 * selector names).</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Graceful error handling: JCR {@link javax.jcr.RepositoryException} is caught and logged; empty iterators/lists
 *       are returned instead of {@code null}.</li>
 *   <li>Utility accessors for common join scenarios (left/right selector convenience).</li>
 *   <li>Selector-based node extraction from result rows.</li>
 * </ul>
 * <p>Null & error handling: All public accessors return non-null collections (possibly empty). Invalid selector names
 * or repository access issues are logged at WARN level.</p>
 * <p>Thread-safety: Instances are NOT thread-safe. Consume in the creating thread; do not share concurrently without
 * external synchronization.</p>
 * <p>Side effects: Only logging; underlying result is read-only.</p>
 * <p>Usage example:</p>
 * <pre>{@code List<Node> leftNodes = new RowsQuery(query).execute().getLeftRowNodeList();}</pre>
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-21
 */
public class RowsResult extends ResultWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RowsResult.class);

    /**
     * Create a new row-oriented result wrapper.
     * @param result underlying JCR query result
     */
    public RowsResult(QueryResult result) {
        super(result);
    }

    /**
     * Obtain an iterator over all {@link Row} objects in their query-defined order.
     * Returns an empty iterator if the underlying JCR call fails.
     * @return non-null iterator of rows (possibly empty)
     */
    @SuppressWarnings("unchecked")
    public Iterator<Row> getRows() {
        Iterator<Row> rows = Collections.emptyIterator();
        try {
            rows = getResult().getRows();
        } catch (RepositoryException e) {
            LOGGER.warn("Failed to get query result rows.", e);
        }
        return rows;
    }

    /**
     * Collects all {@link Row} objects into a {@link List} preserving iteration order.
     * @return non-null list of rows (possibly empty)
     */
    public List<Row> getRowList() {
        Iterator<Row> iterator = getRows();
        List<Row> rows = new ArrayList<>();
        while (iterator.hasNext()) {
            rows.add(iterator.next());
        }
        return rows;
    }

    /**
     * Convenience accessor for nodes belonging to the first selector name ("left" side of a join).
     * @return non-null list of nodes (possibly empty)
     */
    public List<Node> getLeftRowNodeList() {
        String[] selectors = getSelectorNames();
        return selectors != null && selectors.length > 0 ? getRowNodeListFor(selectors[0]) : Collections.emptyList();
    }

    /**
     * Convenience accessor for nodes belonging to the second selector name ("right" side of a join).
     * @return non-null list of nodes (possibly empty)
     */
    public List<Node> getRightRowNodeList() {
        String[] selectors = getSelectorNames();
        return selectors != null && selectors.length > 1 ? getRowNodeListFor(selectors[1]) : Collections.emptyList();
    }

    /**
     * Resolve nodes for a given selector name across all result rows.
     * If the selector is blank, an empty list is returned.
     * @param selector selector name (must match query selector)
     * @return non-null list of nodes (possibly empty)
     */
    public List<Node> getRowNodeListFor(String selector) {
        Iterator<Row> iterator = getRows();
        List<Node> result = new ArrayList<>();
        if (isNotBlank(selector)) {
            while (iterator.hasNext()) {
                try {
                    result.add(iterator.next().getNode(selector));
                } catch (RepositoryException e) {
                    LOGGER.warn("Failed to get node for selector " + selector + " from result row.", e);
                }
            }
        }
        return result;
    }
}
