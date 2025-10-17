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
 * Comparison step API for node name conditions AFTER invoking a potential negation (name() specific variant).
 * Supports a subset of string comparisons suitable for JCR name() operand (no LIKE, length, multi NOT).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-11-11
 */
public interface Sql2NameCompare {
    /** Strictly lower than comparison.
     * @return single-value step */
    Sql2NameOperandSingle lowerThan();
    /** Lower or equal comparison.
     * @return single-value step */
    Sql2NameOperandSingle lowerOrEqualThan();
    /** Equals any of supplied names (OR).
     * @return multi-value step */
    Sql2NameOperandMultiple equalsAny();
    /** Greater or equal comparison.
     * @return single-value step */
    Sql2NameOperandSingle greaterOrEqualThan();
    /** Strictly greater than comparison.
     * @return single-value step */
    Sql2NameOperandSingle greaterThan();
    /** Not equal comparison against any supplied name (OR).
     * @return multi-value step */
    Sql2NameOperandMultiple excludeAny();
}
