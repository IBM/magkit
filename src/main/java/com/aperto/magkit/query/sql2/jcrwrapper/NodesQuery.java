package com.aperto.magkit.query.sql2.jcrwrapper;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;

public class NodesQuery extends QueryWrapper {

    public NodesQuery(Query query) {
        super(query);
    }

    /**
     * Executes this query and returns a <code>{@link NodesResult}</code>
     * object.
     * <p>
     * If this <code>Query</code> contains a variable (see {@link
     * javax.jcr.query.qom.BindVariableValue BindVariableValue}) which has not
     * been bound to a value (see {@link Query#bindValue}) then this method
     * throws an <code>InvalidQueryException</code>.
     *
     * @return a <code>QueryResult</code> object
     * @throws InvalidQueryException if the query contains an unbound variable.
     * @throws RepositoryException   if another error occurs.
     */
    public NodesResult execute() throws InvalidQueryException, RepositoryException {
        return new NodesResult(getQuery().execute());
    };
}
