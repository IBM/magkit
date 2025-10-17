package de.ibmix.magkit.query.sql2.condition;

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

import de.ibmix.magkit.query.sql2.statement.Sql2SelectorNames;

/**
 * Base interface for join conditions (JCR-SQL2 {@code ON} clause fragments) comparing aspects of the selected
 * and the joined selector. Implementations render themselves via {@link #appendTo(StringBuilder, Sql2SelectorNames)}.
 * Empty implementations should append nothing and are skipped by higher-level builders.
 *
 * Thread-safety: Implementations are not expected to be thread safe.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-18
 */
public interface Sql2JoinCondition {
    /**
     * Append this join condition fragment to the accumulating SQL2 buffer.
     *
     * @param sql2 accumulator (never null)
     * @param selectorNames provider for selector names
     */
    void appendTo(StringBuilder sql2, Sql2SelectorNames selectorNames);
}
