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
 * Base builder interface for all constraints inside the SQL2 query builder DSL.
 * <p>
 * A {@code Sql2Constraint} is any element that can render itself into a valid JCR-SQL2 constraint fragment and
 * report whether it actually contains user supplied values (see {@link #isNotEmpty()}). Empty constraints are
 * ignored by composite builders (e.g. {@code Sql2ConstraintGroup}) to avoid producing syntactically broken queries
 * or superfluous parenthesis blocks.
 * </p>
 * <p>Typical usage pattern:</p>
 * <pre>{@code
 * String constraint = Sql2StringCondition.property("title").equalsAny().values("Hello").asString();
 * // -> "[title] = 'Hello'"
 * }
 * </pre>
 * <p>
 * Thread-safety: Implementations are NOT thread safe and are intended for short lived, single-threaded builder
 * usage only.
 * </p>
 * <p>
 * Null handling: Methods should defensively ignore null or empty values and return empty constraint fragments.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-02-28
 */
public interface Sql2Constraint {
    String SQL2_OP_OR = " OR ";
    String SQL2_OP_AND = " AND ";

    /**
     * Append the SQL2 fragment for this constraint to the given {@link StringBuilder}.
     * Implementations MUST NOT append anything when {@link #isNotEmpty()} is {@code false}.
     *
     * @param sql2 the accumulating query buffer (never null)
     * @param selectorNames provider for the current from / join selector names (may be null)
     */
    void appendTo(StringBuilder sql2, Sql2SelectorNames selectorNames);

    /**
     * Indicates whether this constraint holds any effective (non-empty) state and therefore would output
     * something when appended. Returning {@code false} allows surrounding composite constraints to skip it.
     *
     * @return {@code true} if this constraint will produce output, otherwise {@code false}
     */
    boolean isNotEmpty();

    /**
     * Render this constraint using no selector context. Convenience for {@link #asString(String, String)}.
     *
     * @return the SQL2 fragment or empty string
     */
    default String asString() {
        return asString(null, null);
    }

    /**
     * Render this constraint into a stand-alone SQL2 fragment string using the provided selector names.
     *
     * @param fromSelector the name of the main (FROM) selector or {@code null}
     * @param joinSelector the name of the joined selector (if any) or {@code null}
     * @return the resulting SQL2 constraint fragment (never null, may be empty)
     */
    default String asString(final String fromSelector, final String joinSelector) {
        StringBuilder result = new StringBuilder();
        appendTo(result, new Sql2SelectorNames() {
            @Override
            public String getFromSelectorName() {
                return fromSelector;
            }

            @Override
            public String getJoinSelectorName() {
                return joinSelector;
            }
        });
        return result.toString();
    }
}
