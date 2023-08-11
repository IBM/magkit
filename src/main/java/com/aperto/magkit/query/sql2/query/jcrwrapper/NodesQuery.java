package com.aperto.magkit.query.sql2.query.jcrwrapper;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;

/**
 * A wrapper for javax.jcr.query.Query to separate Row and Node queries.
 *
 * @author wolf.bubenik@aperto.com
 * @since (21.8.2020)
 */
public class NodesQuery extends QueryWrapper<NodesQuery> {

    public NodesQuery(Query query) {
        super(query);
    }

    @Override
    NodesQuery me() {
        return this;
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
    public NodesResult execute() throws RepositoryException {
        return new NodesResult(getQuery().execute());
    }
}
