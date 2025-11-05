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

import de.ibmix.magkit.query.sql2.condition.Sql2JoinCondition;

/**
 * Fluent API step for specifying the ON condition of a previously declared JOIN.
 * <p>After choosing a join method and assigning a selector name ({@link Sql2JoinAs}), this step
 * accepts a {@link Sql2JoinCondition} describing how the two selectors relate (e.g. descendant, child or equality).
 * The provided condition is validated only when the final SQL2 string is built; passing null will cause an NPE
 * during build.</p>
 * <p>Side effects: Stores the join condition in the mutable builder instance.</p>
 * <p>Null handling: The implementation expects a non-null condition.</p>
 * <p>Thread-safety: Not thread-safe; use a separate builder per thread.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-18
 */
public interface Sql2JoinOn {
    /**
     * Define the join relationship between the primary and the joined selector.
     * @param onCondition the condition describing the join (must be non-null)
     * @return next step allowing adding WHERE and ORDER BY clauses
     */
    Sql2Where on(Sql2JoinCondition onCondition);
}
