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
 * Generic fluent comparison step API for property conditions AFTER a preceding {@code not()} call.
 * Excludes the {@code not()} method to guide DSL users: once {@code not()} is invoked, only comparison
 * operators remain. Implementations return further operand selection steps allowing single or multi-value
 * or bind-variable input.
 * <p>Typical flow:</p>
 * <pre>{@code
 * Sql2PropertyCondition.property("views").not().greaterThan().value(100L);
 * }</pre>
 * <p>All methods only prepare state; rendering happens via {@code appendTo()} in the concrete implementation.</p>
 *
 * @param <V> the type of the property (String, Long, Double, Calendar)
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-07
 */
public interface Sql2Compare<V> {
    /** Start a strictly lower-than comparison.
     * @return single-value operand step */
    Sql2StaticOperandSingle<V> lowerThan();
    /** Start a lower-or-equal comparison.
     * @return single-value operand step */
    Sql2StaticOperandSingle<V> lowerOrEqualThan();
    /** Expect any of the provided values (OR).
     * @return multi-value operand step */
    Sql2StaticOperandMultiple<V> equalsAny();
    /** Expect all of the provided values (AND).
     * @return multi-value operand step */
    Sql2StaticOperandMultiple<V> equalsAll();
    /** Start a greater-or-equal comparison.
     * @return single-value operand step */
    Sql2StaticOperandSingle<V> greaterOrEqualThan();
    /** Start a strictly greater-than comparison.
     * @return single-value operand step */
    Sql2StaticOperandSingle<V> greaterThan();
    /** Exclude any of the provided values (OR NOT EQUAL).
     * @return multi-value operand step */
    Sql2StaticOperandMultiple<V> excludeAny();
    /** Exclude all of the provided values (AND NOT EQUAL).
     * @return multi-value operand step */
    Sql2StaticOperandMultiple<V> excludeAll();
}
