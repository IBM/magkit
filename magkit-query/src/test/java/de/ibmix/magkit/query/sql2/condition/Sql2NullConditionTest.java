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
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Sql2NullCondition.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-06-17
 */
public class Sql2NullConditionTest {

    @Test
    public void isNull() {
        assertEquals(EMPTY, Sql2NullCondition.isNull(null).asString());
        assertEquals(EMPTY, Sql2NullCondition.isNull(EMPTY).asString());
        assertEquals("[test] IS NULL", Sql2NullCondition.isNull("test").asString());
    }

    @Test
    public void isNotNull() {
        assertEquals(EMPTY, Sql2NullCondition.isNotNull(null).asString());
        assertEquals(EMPTY, Sql2NullCondition.isNotNull(EMPTY).asString());
        assertEquals("[test] IS NOT NULL", Sql2NullCondition.isNotNull("test").asString());
    }

    @Test
    public void testSelectors() {
        Sql2JoinConstraint condition = Sql2NullCondition.isNull("test");
        assertEquals("SELECT [a],[b] FROM [aperto:test] WHERE [test] IS NULL", Sql2Statement.select("a", "b").from("aperto:test").whereAny(condition).build());
    }
}
