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
 * SQL2 statement builder interface for fluent API: Optional ordering step.
 * <p>Allows specifying one or multiple attributes for ordering or the JCR full text relevance score.
 * Subsequent calls to {@link #orderBy(String...)} override previously set order attributes. The direction defaults
 * to descending unless changed via {@link Sql2OrderDirection#ascending()}.</p>
 * <p>Null/empty handling: Passing null or an empty array results in an empty ORDER BY clause (ignored).</p>
 * <p>Thread-safety: Implementations are not thread-safe.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-15
 */
public interface Sql2Order extends Sql2Builder {
    /**
     * Define the attributes used for ordering the result set.
     * @param attribute one or more JCR property names
     * @return next step allowing setting the order direction
     */
    Sql2OrderDirection orderBy(String... attribute);
    /**
     * Convenience method ordering by the JCR search score attribute.
     * @return next step allowing setting the order direction
     */
    Sql2OrderDirection orderByScore();
}
