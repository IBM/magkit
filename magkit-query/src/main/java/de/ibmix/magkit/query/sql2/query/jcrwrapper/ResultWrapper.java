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

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

/**
 * Base wrapper for javax.jcr.query.QueryResult to separate Row and Node queries.
 * Provides methods to access column and selector names.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-21
 */
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
     * @return a <code>String</code> array holding the selector names or an empty array when an exception occurs.
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
