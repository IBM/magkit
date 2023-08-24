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
 * Base wrapper wrapper for javax.jcr.query.Query to separate Row and Node queries.
 * Provides methods to bind values to statement variables.
 *
 * @param <T> The type of QueryWrapper to be returned by builder methods
 * @author wolf.bubenik@aperto.com
 * @since (21.8.2020)
 */
public abstract class QueryWrapper<T extends QueryWrapper> {
    private final Query _query;

    protected QueryWrapper(Query query) {
        _query = query;
    }

    protected Query getQuery() {
        return _query;
    }

    /**
     * Binds the given <code>value</code> to the variable named
     * <code>varName</code>.
     *
     * @param varName name of variable in query
     * @param value   value to bind
     * @throws IllegalArgumentException      if <code>varName</code> is not a valid
     *                                       variable in this query.
     * @throws javax.jcr.RepositoryException if an error occurs.
     * @since JCR 2.0
     */
    public T bindValue(String varName, Value value) throws RepositoryException {
        _query.bindValue(varName, value);
        return me();
    }

    public T bindString(final String varName, final String value) throws RepositoryException {
        return bindValue(varName, new StringValue(value));
    }

    public T bindLong(final String varName, final Long value) throws RepositoryException {
        return bindValue(varName, new LongValue(value));
    }

    public T bindDouble(final String varName, final Double value) throws RepositoryException {
        return bindValue(varName, new DoubleValue(value));
    }

    public T bindDate(final String varName, final Calendar value) throws RepositoryException {
        return bindValue(varName, new DateValue(value));
    }

    public T bindBoolean(final String varName, final Boolean value) throws RepositoryException {
        return bindValue(varName, new BooleanValue(value));
    }

    /**
     * Returns the names of the bind variables in this query. If this query does
     * not contains any bind variables then an empty array is returned.
     *
     * @return the names of the bind variables in this query.
     * @throws RepositoryException if an error occurs.
     * @since JCR 2.0
     */
    public String[] getBindVariableNames() throws RepositoryException {
        return _query.getBindVariableNames();
    }

    /**
     * Returns the statement defined for this query.
     * <p>
     * If the language of this query is JCR-SQL2 or another string-based
     * language, this method will return the statement that was used to create
     * this query.
     * <p>
     * If the language of this query is JCR-JQOM, this method will return the
     * JCR-SQL2 equivalent of the JCR-JQOM object tree. This is the standard
     * serialization of JCR-JQOM and is also the string stored in the
     * <code>jcr:statement</code> property if the query is persisted.
     *
     * @return the query statement.
     */
    public String getStatement() {
        return _query.getStatement();
    }

    abstract T me();

}
