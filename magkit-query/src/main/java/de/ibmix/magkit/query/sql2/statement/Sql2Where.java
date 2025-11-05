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

import de.ibmix.magkit.query.sql2.condition.Sql2JoinConstraint;

/**
 * SQL2 statement builder interface for fluent API: Optional where step.
 * <p>Allows adding constraint groups combined by logical AND (all must match) or logical OR (any may match).
 * Constraints can refer to properties, paths or templates. Passing an empty array results in no WHERE clause.</p>
 * <p>Thread-safety: Implementations are not thread-safe.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-15
 */
public interface Sql2Where extends Sql2Order {
    /**
     * Add a WHERE clause matching all provided constraints (logical AND).
     * @param constraints one or more constraints (ignored if empty)
     * @return next step allowing ordering
     */
    Sql2Order whereAll(Sql2JoinConstraint... constraints);
    /**
     * Add a WHERE clause matching any provided constraints (logical OR).
     * @param constraints one or more constraints (ignored if empty)
     * @return next step allowing ordering
     */
    Sql2Order whereAny(Sql2JoinConstraint... constraints);
}
