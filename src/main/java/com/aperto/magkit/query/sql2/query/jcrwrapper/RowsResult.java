package com.aperto.magkit.query.sql2.query.jcrwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A wrapper for javax.jcr.query.QueryResult to separate Row and Node queries.
 * Provides methods to access Row results.
 *
 * @author wolf.bubenik@aperto.com
 * @since (21.8.2020)
 */
public class RowsResult extends ResultWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(RowsResult.class);

    public RowsResult(QueryResult result) {
        super(result);
    }

    /**
     * Returns an iterator over the <code>Row</code>s of the result table. The
     * rows are returned according to the ordering specified in the query.
     *
     * @return a <code>Iterator<Row></code>.
     */
    public Iterator<Row> getRows() {
        Iterator<Row> rows = Collections.emptyIterator();
        try {
            rows = getResult().getRows();
        } catch (RepositoryException e) {
            LOG.warn("Failed to get query result rows.", e);
        }
        return rows;
    };

    public List<Row> getRowList() {
        Iterator<Row> iterator = getRows();
        List<Row> rows = new ArrayList<>();
        while (iterator.hasNext()) {
            rows.add(iterator.next());
        }
        return rows;
    };
}
