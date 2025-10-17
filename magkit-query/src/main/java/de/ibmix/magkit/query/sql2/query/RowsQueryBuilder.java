package de.ibmix.magkit.query.sql2.query;

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

import de.ibmix.magkit.query.sql2.query.jcrwrapper.RowsQuery;

import javax.jcr.query.Row;
import java.util.List;

/**
 * Final builder step interface for creating and executing row-oriented SQL2 queries.
 * <p>Purpose: Defines terminal operations to build a {@link RowsQuery} and obtain row results, optionally checking for
 * emptiness. Complements {@link NodesQueryBuilder} for node-centric retrieval.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Create a type-safe {@link RowsQuery} wrapper from accumulated builder state.</li>
 *   <li>Retrieve all {@link Row} instances or quickly test for existence via {@link #hasResultRows()}.</li>
 * </ul>
 * <p>Null & error handling: Implementations return empty collections or {@code false} instead of throwing on
 * repository errors, logging warnings instead.</p>
 * <p>Thread-safety: NOT thread-safe; use per request / operation.</p>
 * <p>Usage example:</p>
 * <pre>{@code List<Row> rows = Sql2QueryBuilder.forRows().fromWebsite().withStatement("SELECT * FROM [mgnl:page]")
 *     .withLimit(100).buildRowsQuery().execute().getRowList();}</pre>
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-27
 */
public interface RowsQueryBuilder extends QueryLimit<RowsQueryBuilder> {
    /**
     * Build a {@link RowsQuery} instance from the accumulated builder configuration.
     * @return non-null {@link RowsQuery} ready for execution
     */
    RowsQuery buildRowsQuery();

    /**
     * Execute the query and return all resulting {@link Row} objects.
     * Implementations return an empty list if execution fails.
     * @return non-null list of rows (possibly empty)
     */
    List<Row> getResultRows();

    /**
     * Execute the query and check if any {@link Row} exists.
     * @return {@code true} if at least one row is present; {@code false} otherwise or on error
     */
    boolean hasResultRows();
}
