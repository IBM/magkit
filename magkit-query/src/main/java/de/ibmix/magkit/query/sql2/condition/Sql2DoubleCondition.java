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
 * Builder class for double property conditions.
 *
 * @author wolf.bubenik@aperto.com
 * @since 08.04.2020
 */
public final class Sql2DoubleCondition extends Sql2PropertyCondition<Sql2DoubleCondition, Double> {

    private Sql2DoubleCondition(final String property) {
        super(property);
    }

    public static Sql2CompareNot<Double> property(final String name) {
        return new Sql2DoubleCondition(name);
    }

    @Override
    protected Sql2DoubleCondition me() {
        return this;
    }

    @Override
    protected void appendValueConstraint(StringBuilder sql2, final String selectorName, String name, Double value) {
        if (value != null) {
            if (isNotBlank(selectorName)) {
                sql2.append(selectorName).append('.');
            }
            sql2.append('[').append(name).append(']').append(getCompareOperator()).append(value);
        }
    }
}
