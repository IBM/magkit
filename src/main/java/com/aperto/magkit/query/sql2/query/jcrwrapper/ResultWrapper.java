package com.aperto.magkit.query.sql2.query.jcrwrapper;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

public abstract class ResultWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(RowsResult.class);

    private final QueryResult _result;

    protected ResultWrapper(QueryResult result) {
        _result = result;
    }

    protected QueryResult getResult() {
        return _result;
    }

    /**
     * Returns an array of all the column names in the table view of this result
     * set.
     *
     * @return a <code>String</code> array holding the column names or empty array when an exception occurs.
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
     * Returns an array of all the selector names that were used in the query
     * that created this result. If the query did not have a selector name then
     * an empty array is returned.
     *
     * @return a <code>String</code> array holding the selector es or empty array when an exception occurs.
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
