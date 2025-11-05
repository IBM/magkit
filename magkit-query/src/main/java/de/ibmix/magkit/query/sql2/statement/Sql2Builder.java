package de.ibmix.magkit.query.sql2.statement;

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
 * The interface for the last step of query statement building: Build the query string.
 * <p>Calling {@link #build()} renders the accumulated builder state into a JCR SQL2 statement.
 * Multiple invocations typically return identical results unless the underlying mutable builder changed.</p>
 * <p>Thread-safety: Not thread-safe.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-02-28
 */
public interface Sql2Builder {
    /**
     * Render and return the final SQL2 query string.
     * @return the SQL2 statement (never null)
     */
    String build();
}
