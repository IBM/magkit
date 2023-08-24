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
 * Base builder interface for all constraints.
 *
 * @author wolf.bubenik@aperto.com
 * @since 28.02.20
 **/
public interface Sql2Constraint {
    String SQL2_OP_OR = " OR ";
    String SQL2_OP_AND = " AND ";

    void appendTo(StringBuilder sql2, Sql2SelectorNames selectorNames);

    boolean isNotEmpty();

    default String asString() {
        return asString(null, null);
    }

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
