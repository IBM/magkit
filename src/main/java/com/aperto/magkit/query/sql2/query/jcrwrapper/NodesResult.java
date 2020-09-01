package com.aperto.magkit.query.sql2.query.jcrwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A wrapper for javax.jcr.query.QueryResult to separate Row and Node queries.
 * Provides methods to access Node results.
 *
 * @author wolf.bubenik@aperto.com
 * @since (21.8.2020)
 */
public class NodesResult extends ResultWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(RowsResult.class);

    public NodesResult(QueryResult result) {
        super(result);
    }

    /**
     * Returns an iterator over the <code>Row</code>s of the result table. The
     * rows are returned according to the ordering specified in the query.
     *
     * @return a <code>RowIterator</code> or null if an exception occurs.
     */
    public Iterator<Node> getNodes() {
        Iterator<Node> nodes = Collections.emptyIterator();
        try {
            nodes = getResult().getNodes();
        } catch (RepositoryException e) {
            LOG.warn("Failed to get query result rows.", e);
        }
        return nodes;
    };

    public List<Node> getNodeList() {
        Iterator<Node> iterator = getNodes();
        List<Node> rows = new ArrayList<>();
        while (iterator.hasNext()) {
            rows.add(iterator.next());
        }
        return rows;
    };
}
