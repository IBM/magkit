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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A wrapper for javax.jcr.query.QueryResult to separate Row and Node queries.
 * Provides methods to access Node results.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-21
 */
public class NodesResult extends ResultWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RowsResult.class);

    public NodesResult(QueryResult result) {
        super(result);
    }

    /**
     * Returns an iterator over the <code>Node</code>s of the result table. The
     * nodes are returned according to the ordering specified in the query.
     *
     * @return a <code>Iterator&lt;Node&gt;</code> or an empty Iterator if an exception occurs.
     */
    public Iterator<Node> getNodes() {
        Iterator<Node> nodes = Collections.emptyIterator();
        try {
            nodes = getResult().getNodes();
        } catch (RepositoryException e) {
            LOGGER.warn("Failed to get query result rows.", e);
        }
        return nodes;
    }

    public List<Node> getNodeList() {
        Iterator<Node> iterator = getNodes();
        List<Node> rows = new ArrayList<>();
        while (iterator.hasNext()) {
            rows.add(iterator.next());
        }
        return rows;
    }
}
