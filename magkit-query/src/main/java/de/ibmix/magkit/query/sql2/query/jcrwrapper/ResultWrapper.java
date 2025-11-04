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

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

/**
 * Base abstraction wrapping a {@link javax.jcr.query.QueryResult} in order to clearly separate
 * result handling for row-oriented ({@link RowsResult}) and node-oriented ({@link NodesResult}) queries.
 * <p>Purpose: Unifies shared convenience accessors (column and selector names) and error handling strategy used by
 * specialized result wrappers.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Safe accessor methods for column and selector names with graceful error handling (exceptions are caught and logged).</li>
 *   <li>Uniform API surface to be extended by concrete result wrappers.</li>
 * </ul>
 * <p>Null and error handling: All accessor methods return an empty array instead of {@code null}; repository exceptions
 * are logged at WARN level to avoid disrupting calling code.</p>
 * <p>Thread-safety: Instances are NOT thread-safe. A {@code QueryResult} is typically consumed once; do not share a
 * single instance across threads without external synchronization.</p>
 * <p>Usage example:</p>
 * <pre>{@code String[] selectors = new RowsResult(query.execute()).getSelectorNames();}</pre>
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-21
 */
public abstract class ResultWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(ResultWrapper.class);

    private final QueryResult _result;

    /**
     * Create a new wrapper around a JCR {@link QueryResult}.
     * @param result underlying query result (must not be null for successful accessors)
     */
    protected ResultWrapper(QueryResult result) {
        _result = result;
    }

    /**
     * Access the underlying {@link QueryResult} for subclass operations.
     * @return non-null query result reference (as provided in constructor)
     */
    protected QueryResult getResult() {
        return _result;
    }

    /**
     * Returns an array of all column names present in the tabular view of this query result.
     * The method never returns {@code null}; in case of a repository access problem an empty array is returned and the
     * exception is logged.
     * @return non-null (possibly empty) array of column names
     */
    public String[] getColumnNames() {
        String[] names = ArrayUtils.EMPTY_STRING_ARRAY;
        try {
            names = _result.getColumnNames();
        } catch (RepositoryException e) {
            LOG.warn("Failed to get query result column names.", e);
        }
        return names;
    }

    /**
     * Returns all selector names used in the underlying query. If the query did not define explicit selector names or
     * if an error occurs, an empty array is returned and the exception is logged.
     * @return non-null (possibly empty) array of selector names
     */
    public String[] getSelectorNames() {
        String[] names = ArrayUtils.EMPTY_STRING_ARRAY;
        try {
            names = _result.getSelectorNames();
        } catch (RepositoryException e) {
            LOG.warn("Failed to get query result selector names.", e);
        }
        return names;
    }
}
