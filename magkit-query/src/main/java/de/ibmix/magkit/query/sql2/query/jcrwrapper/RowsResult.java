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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * A wrapper for javax.jcr.query.QueryResult to separate Row and Node queries.
 * Provides methods to access Row results.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-21
 */
public class RowsResult extends ResultWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RowsResult.class);

    public RowsResult(QueryResult result) {
        super(result);
    }

    /**
     * Returns an iterator over the <code>Row</code>s of the result table. The
     * rows are returned according to the ordering specified in the query.
     *
     * @return a <code>Iterator&lt;Row&gt;</code>, never null
     */
    public Iterator<Row> getRows() {
        Iterator<Row> rows = Collections.emptyIterator();
        try {
            rows = getResult().getRows();
        } catch (RepositoryException e) {
            LOGGER.warn("Failed to get query result rows.", e);
        }
        return rows;
    }

    /**
     * Accessor for the query result as List&lt;Node&gt;.
     *
     * @return the query result as javax.jcr.Node list, never null
     */
    public List<Row> getRowList() {
        Iterator<Row> iterator = getRows();
        List<Row> rows = new ArrayList<>();
        while (iterator.hasNext()) {
            rows.add(iterator.next());
        }
        return rows;
    }

    /**
     * Accessor for the 'left' query result as List&lt;Node&gt;.
     * Provides the Nodes for the first selector name given by the query result.
     * Note that 'left' is the normal select name and comes as first name from the query result.
     *
     * @return the query result as javax.jcr.Node list or an empty list, never null
     */
    public List<Node> getLeftRowNodeList() {
        String[] selectors = getSelectorNames();
        return selectors != null && selectors.length > 0 ? getRowNodeListFor(selectors[0]) : Collections.emptyList();
    }

    /**
     * Accessor for the 'right' query result as List&lt;Node&gt;.
     * Provides the Nodes for the second selector name given by the query result.
     * Note that 'right' is the join select name and comes as second name from the query result.
     *
     * @return the query result as javax.jcr.Node list or an empty list, never null
     */
    public List<Node> getRightRowNodeList() {
        String[] selectors = getSelectorNames();
        return selectors != null && selectors.length > 1 ? getRowNodeListFor(selectors[1]) : Collections.emptyList();
    }

    /**
     * Accessor for the query result as List&lt;Node&gt;.
     * Provides the Nodes for the given selector name.
     *
     * @param selector selector
     * @return the query result as javax.jcr.Node list or an empty list, never null
     */
    public List<Node> getRowNodeListFor(String selector) {
        Iterator<Row> iterator = getRows();
        List<Node> result = new ArrayList<>();
        if (isNotBlank(selector)) {
            while (iterator.hasNext()) {
                try {
                    result.add(iterator.next().getNode(selector));
                } catch (RepositoryException e) {
                    LOGGER.warn("Failed to get node for selector " + selector + " from result row.", e);
                }
            }
        }
        return result;
    }
}
