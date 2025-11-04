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

/**
 * Builder step interface declaring methods for query result limiting and paging.
 * <p>Purpose: Adds optional limit (maximum number of results) and offset (skip count for paging) configuration to a
 * fluent SQL2 query builder chain. Implementations enforce non-negative values; negative input is coerced to 0.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Fluent limit and offset specification.</li>
 *   <li>Type parameter {@code <T>} preserves concrete builder type for chaining without casts.</li>
 * </ul>
 * <p>Null and error handling: Methods never return {@code null}; they return the concrete builder. Invalid (negative)
 * values are sanitized (treated as 0) by typical implementations.</p>
 * <p>Thread-safety: Not inherently thread-safe; builder instances should be confined to a single thread.</p>
 * <p>Usage example:</p>
 * <pre>{@code Sql2QueryBuilder.forRows().fromWebsite().withStatement("SELECT * FROM [mgnl:page]")
 *     .withOffset(20).withLimit(10).buildRowsQuery().execute();}</pre>
 * @param <T> the concrete builder type enabling fluent chaining
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-28
 */
public interface QueryLimit<T>  {
    /**
     * Set an upper bound on the number of returned results.
     * @param limit maximum result size (negative values treated as 0 by implementations)
     * @return fluent builder instance
     */
    T withLimit(long limit);

    /**
     * Set a result offset for paging (number of initial results to skip).
     * @param offset number of results to skip (negative values treated as 0)
     * @return fluent builder instance
     */
    T withOffset(long offset);
}
