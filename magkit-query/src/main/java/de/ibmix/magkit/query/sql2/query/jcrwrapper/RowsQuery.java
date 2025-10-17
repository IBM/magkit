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

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;

/**
 * Fluent wrapper around a JCR {@link Query} producing {@link RowsResult} for row-oriented access.
 * <p>Purpose: Provides convenience methods to bind typed variables and execute the query yielding a row-centric
 * result abstraction. Complements {@link NodesQuery} for node-based consumption.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Typed fluent bind methods inherited from {@link QueryWrapper}.</li>
 *   <li>Single-step {@link #execute()} returning a {@link RowsResult} with iteration utilities.</li>
 * </ul>
 * <p>Thread-safety: NOT thread-safe. Confine to a single thread.</p>
 * <p>Usage example:</p>
 * <pre>{@code RowsResult result = new RowsQuery(query).bindString("title", "Welcome").execute();}</pre>
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-21
 */
public class RowsQuery extends QueryWrapper<RowsQuery> {

    public RowsQuery(Query query) {
        super(query);
    }

    @Override
    RowsQuery me() {
        return this;
    }

    /**
     * Execute the underlying JCR query returning a {@link RowsResult} for row-based traversal.
     * Variables must have been bound prior to invocation; otherwise an {@link InvalidQueryException} is thrown.
     * @return non-null {@link RowsResult}
     * @throws InvalidQueryException if an unbound variable exists
     * @throws RepositoryException for other repository access issues
     */
    public RowsResult execute() throws RepositoryException {
        return new RowsResult(getQuery().execute());
    }
}
