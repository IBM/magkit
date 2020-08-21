package com.aperto.magkit.query.sql2.jcrwrapper;

import org.apache.jackrabbit.value.BooleanValue;
import org.apache.jackrabbit.value.DateValue;
import org.apache.jackrabbit.value.DoubleValue;
import org.apache.jackrabbit.value.LongValue;
import org.apache.jackrabbit.value.StringValue;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import java.util.Calendar;

public abstract class QueryWrapper {
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
    public void bindValue(String varName, Value value) throws RepositoryException {
        _query.bindValue(varName, value);
    }

    public void bindString(final String varName, final String value) throws RepositoryException {
        bindValue(varName, new StringValue(value));
    }

    public void bindLong(final String varName, final Long value) throws RepositoryException {
        bindValue(varName, new LongValue(value));
    }

    public void bindDouble(final String varName, final Double value) throws RepositoryException {
        bindValue(varName, new DoubleValue(value));
    }

    public void bindDate(final String varName, final Calendar value) throws RepositoryException {
        bindValue(varName, new DateValue(value));
    }

    public void bindBoolean(final String varName, final Boolean value) throws RepositoryException {
        bindValue(varName, new BooleanValue(value));
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

}
