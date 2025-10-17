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

/**
 * Generic interface for all property conditions (single value step). Declares methods to supply exactly
 * one static value or a bind variable name.
 * Thread-safety: Implementations not thread safe.
 * Null handling: Null value results in an empty condition.
 *
 * @param <V> the type of the value
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-20
 */
public interface Sql2StaticOperandSingle<V> {
    /**
     * Provide a single literal value for comparison.
     * @param value literal value (may be null)
     * @return next step allowing join selector decision
     */
    Sql2JoinConstraint value(V value);
    /**
     * Provide a bind variable (replaces any literal value).
     * @param name variable name (may be null/blank -> ignored)
     * @return next step allowing join selector decision
     */
    Sql2JoinConstraint bindVariable(String name);
}
