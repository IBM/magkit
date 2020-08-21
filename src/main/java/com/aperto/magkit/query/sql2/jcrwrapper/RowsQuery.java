package com.aperto.magkit.query.sql2.jcrwrapper;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;

public class RowsQuery extends QueryWrapper {

    public RowsQuery(Query query) {
        super(query);
    }

    /**
     * Executes this query and returns a <code>{@link RowsResult}</code>
     * object.
     * <p>
     * If this <code>Query</code> contains a variable (see {@link
     * javax.jcr.query.qom.BindVariableValue BindVariableValue}) which has not
     * been bound to a value (see {@link Query#bindValue}) then this method
     * throws an <code>InvalidQueryException</code>.
     *
     * @return a <code>RowsResult</code> object
     * @throws InvalidQueryException if the query contains an unbound variable.
     * @throws RepositoryException   if another error occurs.
     */
    public RowsResult execute() throws InvalidQueryException, RepositoryException {
        return new RowsResult(getQuery().execute());
    };
}
