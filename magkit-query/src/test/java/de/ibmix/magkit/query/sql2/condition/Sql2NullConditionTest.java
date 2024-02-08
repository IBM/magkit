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

import de.ibmix.magkit.query.sql2.statement.Sql2Statement;
import org.junit.Test;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test Sql2NullCondition.
 *
 * @author wolf.bubenik@ibmix.de
 * @since (17.06.20)
 */
public class Sql2NullConditionTest {

    @Test
    public void isNull() {
        assertThat(Sql2NullCondition.isNull(null).asString(), is(EMPTY));
        assertThat(Sql2NullCondition.isNull(EMPTY).asString(), is(EMPTY));
        assertThat(Sql2NullCondition.isNull("test").asString(), is("[test] IS NULL"));
    }

    @Test
    public void isNotNull() {
        assertThat(Sql2NullCondition.isNotNull(null).asString(), is(EMPTY));
        assertThat(Sql2NullCondition.isNotNull(EMPTY).asString(), is(EMPTY));
        assertThat(Sql2NullCondition.isNotNull("test").asString(), is("[test] IS NOT NULL"));
    }

    @Test
    public void testSelectors() {
        Sql2JoinConstraint condition = Sql2NullCondition.isNull("test");
        assertThat(Sql2Statement.select("a", "b").from("aperto:test").whereAny(condition).build(), is("SELECT [a],[b] FROM [aperto:test] WHERE [test] IS NULL"));
    }
}
