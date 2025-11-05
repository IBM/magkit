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

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Builder for numeric (long) property comparisons. Supports all comparison operators defined in
 * {@link Sql2PropertyCondition}. Example:
 * <pre>{@code
 * String constraint = Sql2LongCondition.property("views")
 *     .greaterThan().value(100L)
 *     .asString();
 * }</pre>
 * Thread-safety: Not thread safe.
 * Null handling: Null values are ignored (no output produced).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-08
 */
public final class Sql2LongCondition extends Sql2PropertyCondition<Sql2LongCondition, Long> {

    private Sql2LongCondition(final String property) {
        super(property);
    }

    /**
     * Start a condition on the given property.
     *
     * @param name property name
     * @return comparison API
     */
    public static Sql2CompareNot<Long> property(final String name) {
        return new Sql2LongCondition(name);
    }

    @Override
    protected Sql2LongCondition me() {
        return this;
    }

    @Override
    protected void appendValueConstraint(StringBuilder sql2, final String selectorName, String name, Long value) {
        if (value != null) {
            if (isNotBlank(selectorName)) {
                sql2.append(selectorName).append('.');
            }
            sql2.append('[').append(name).append(']').append(getCompareOperator()).append(value);
        }
    }
}
