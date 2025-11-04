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

import org.apache.jackrabbit.value.BooleanValue;
import org.apache.jackrabbit.value.DateValue;
import org.apache.jackrabbit.value.DoubleValue;
import org.apache.jackrabbit.value.LongValue;
import org.apache.jackrabbit.value.StringValue;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import java.util.Calendar;

/**
 * Base wrapper for a {@link Query} to provide a fluent and type-safe API for binding variables
 * and retrieving metadata from a JCR query object. Concrete subclasses specialize execution
 * semantics for row ({@link RowsQuery}) and node ({@link NodesQuery}) oriented result handling.
 * <p>Key functionalities:</p>
 * <ul>
 *   <li>Fluent binding of typed values (String, Long, Double, Calendar, Boolean) to named bind variables.</li>
 *   <li>Preserves original JCR statement for inspection or logging.</li>
 *   <li>Provides direct access to declared bind variable names.</li>
 * </ul>
 * <p>Usage example:</p>
 * <pre>{@code RowsResult result = new RowsQuery(jcrQuery).bindString("title", "Welcome").bindBoolean("active", true).execute();}</pre>
 * <p>Preconditions: The provided {@link Query} instance must be properly constructed in the associated JCR session.
 * Bind variable names must match those declared in the query.</p>
 * <p>Null and error handling: Binding methods pass values directly; repository-related issues surface as
 * {@link RepositoryException}. Convenience methods do not accept {@code null} for variable names; underlying JCR
 * implementation may throw {@link IllegalArgumentException} for unknown variable names.</p>
 * <p>Thread-safety: NOT thread-safe. Instances should be used and executed within a single thread. Sharing across
 * threads requires external synchronization.</p>
 * <p>Side effects: Binds values into the underlying query; no mutation beyond JCR query state.</p>
 * @param <T> concrete fluent subtype
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-21
 */
public abstract class QueryWrapper<T extends QueryWrapper<T>> {
    private final Query _query;

    /**
     * Create a new wrapper around a JCR {@link Query}.
     * @param query underlying JCR query (must not be null)
     */
    protected QueryWrapper(Query query) {
        _query = query;
    }

    protected Query getQuery() {
        return _query;
    }

    /**
     * Binds the given {@code value} to the variable named {@code varName}.
     * @param varName name of the variable inside the query (must not be null)
     * @param value JCR {@link Value} to bind (may be null depending on repository support)
     * @return this wrapper instance for fluent chaining
     * @throws IllegalArgumentException if {@code varName} is not a valid variable in this query
     * @throws RepositoryException if the repository reports an error during binding
     */
    public T bindValue(String varName, Value value) throws RepositoryException {
        _query.bindValue(varName, value);
        return me();
    }

    /**
     * Convenience method binding a {@link String} value.
     * @param varName bind variable name
     * @param value string value to set
     * @return this wrapper instance
     * @throws RepositoryException on repository access errors
     */
    public T bindString(final String varName, final String value) throws RepositoryException {
        return bindValue(varName, new StringValue(value));
    }

    /**
     * Convenience method binding a {@link Long} value.
     * @param varName bind variable name
     * @param value long value to set
     * @return this wrapper instance
     * @throws RepositoryException on repository access errors
     */
    public T bindLong(final String varName, final Long value) throws RepositoryException {
        return bindValue(varName, new LongValue(value));
    }

    /**
     * Convenience method binding a {@link Double} value.
     * @param varName bind variable name
     * @param value double value to set
     * @return this wrapper instance
     * @throws RepositoryException on repository access errors
     */
    public T bindDouble(final String varName, final Double value) throws RepositoryException {
        return bindValue(varName, new DoubleValue(value));
    }

    /**
     * Convenience method binding a {@link Calendar} value.
     * @param varName bind variable name
     * @param value calendar value to set
     * @return this wrapper instance
     * @throws RepositoryException on repository access errors
     */
    public T bindDate(final String varName, final Calendar value) throws RepositoryException {
        return bindValue(varName, new DateValue(value));
    }

    /**
     * Convenience method binding a {@link Boolean} value.
     * @param varName bind variable name
     * @param value boolean value to set
     * @return this wrapper instance
     * @throws RepositoryException on repository access errors
     */
    public T bindBoolean(final String varName, final Boolean value) throws RepositoryException {
        return bindValue(varName, new BooleanValue(value));
    }

    /**
     * Returns names of bind variables defined in this query. Empty array if none defined.
     * @return non-null array of bind variable names (possibly empty)
     * @throws RepositoryException on repository access errors
     */
    public String[] getBindVariableNames() throws RepositoryException {
        return _query.getBindVariableNames();
    }

    /**
     * Returns the serialized statement of this query (original JCR-SQL2 or generated equivalent for JQOM queries).
     * @return non-null JCR query statement string
     */
    public String getStatement() {
        return _query.getStatement();
    }

    /**
     * Internal hook returning the concrete fluent subtype (covariant self-type pattern).
     * @return this instance typed as subtype
     */
    abstract T me();

}
