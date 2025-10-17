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
 * Specialised comparison step API for String properties exposing LIKE variations (starts/ends/contains).
 * Only available after choosing a String property and (optionally) applying {@code not()}.
 * Implementations provide multi-value variants to allow combining several patterns in one step.
 *
 * Thread-safety: Not thread safe.
 * Null handling: Null/empty values are ignored during rendering.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-26
 */
public interface Sql2CompareString extends Sql2Compare<String> {
    /** Prepare a LIKE comparison matching any value at the beginning.
     * @return multi-value operand step */
    Sql2StaticOperandMultiple<String> startsWithAny();
    /** Prepare a LIKE comparison matching any value at the end.
     * @return multi-value operand step */
    Sql2StaticOperandMultiple<String> endsWithAny();
    /** Prepare a LIKE comparison matching any value anywhere (OR semantics).
     * @return multi-value operand step */
    Sql2StaticOperandMultiple<String> likeAny();
    /** Prepare a LIKE comparison matching all values (AND semantics).
     * @return multi-value operand step */
    Sql2StaticOperandMultiple<String> likeAll();
}
