package com.aperto.magkit.query.sql2.condition;

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

import com.aperto.magkit.query.sql2.statement.Sql2SelectorNames;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test Sql2ContainsCondition.
 *
 * @author wolf.bubenik@aperto.com
 * @since 3.5.2 (05.01.21)
 */
public class Sql2ContainsConditionTest {
    private static final Sql2SelectorNames SELECTOR_NAMES = new Sql2SelectorNames() {
        @Override
        public String getFromSelectorName() {
            return "from";
        }

        @Override
        public String getJoinSelectorName() {
            return "join";
        }
    };

    private Sql2ContainsCondition _containsCondition;

    @Before
    public void setUp() throws Exception {
        _containsCondition = new Sql2ContainsCondition();
    }

    @Test
    public void empty() {
        StringBuilder sql2 = new StringBuilder();
        assertThat(_containsCondition.isNotEmpty(), is(false));
        _containsCondition.appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is(""));

        _containsCondition.all("").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is(""));

        _containsCondition.all("   ").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is(""));
    }

    @Test
    public void all() {
        StringBuilder sql2 = new StringBuilder();
        _containsCondition.all(" first", "second ")
            .all(0, false, "ignoreInRating")
            .all(1, true, "fuzzy")
            .appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'first second ignoreInRating^0 fuzzy~')"));

        sql2.setLength(0);
        _containsCondition.all(2, true, "ignore fuzzy on phrase").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'first second ignoreInRating^0 fuzzy~ \"ignore fuzzy on phrase\"^2')"));
    }

    @Test
    public void excludeAll() {
        StringBuilder sql2 = new StringBuilder();
        _containsCondition.excludeAll("first", "second").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, '-first -second')"));

        sql2.setLength(0);
        _containsCondition.excludeAll(2, true, "boosted").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, '-first -second -boosted~^2')"));
    }

    @Test
    public void any() {
        StringBuilder sql2 = new StringBuilder();
        _containsCondition.any("first", "second").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'first OR second')"));

        sql2.setLength(0);
        _containsCondition.any(3, true, "ignore fuzzy on phrase").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'first OR second OR \"ignore fuzzy on phrase\"^3')"));
    }

    @Test
    public void excludeAny() {
        StringBuilder sql2 = new StringBuilder();
        _containsCondition.excludeAny("first", "second").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, '-first OR -second')"));

        sql2.setLength(0);
        _containsCondition.excludeAny(3, true, "boosted").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, '-first OR -second OR -boosted~^3')"));
    }

    @Test
    public void boost() {
        StringBuilder sql2 = new StringBuilder();
        _containsCondition
            .all(0, false, "ignoreInRating")
            .all(1, false, "noBoost")
            .all(2, false, "boost").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'ignoreInRating^0 noBoost boost^2')"));
    }

    @Test
    public void proximitySearchWithPhrase() {
        StringBuilder sql2 = new StringBuilder();
        _containsCondition.addTerm(1, false, false, false, 10, true, "this and that?").appendTo(sql2, SELECTOR_NAMES);
        // Note that in phrases the question mark will never be escaped by \
        assertThat(sql2.toString(), is("contains(from.*, '\"this and that?\"~10')"));

        sql2.setLength(0);
        _containsCondition.addTerm(1, false, false, false, 0, true, "ignore zero distance!").appendTo(sql2, SELECTOR_NAMES);
        // Note that in phrases the question mark will never be escaped by \
        assertThat(sql2.toString(), is("contains(from.*, '\"this and that?\"~10 \"ignore zero distance!\"')"));
    }

    @Test
    public void ignoreProximityOnSimpleTerm() {
        StringBuilder sql2 = new StringBuilder();
        _containsCondition.addTerm(1, false, false, false, 10, false, "this").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'this')"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void startWithQuestionMark() {
        _containsCondition.excludeAny("?first");
    }

    @Test(expected = IllegalArgumentException.class)
    public void startWithStar() {
        _containsCondition.excludeAny("*first");
    }

    @Test(expected = IllegalStateException.class)
    public void missingSelector() {
        _containsCondition.any("first").asString();
    }

    @Test
    public void excludeForJoin() {
        StringBuilder sql2 = new StringBuilder();
        _containsCondition.excludeAny("first").forJoin().appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(join.*, '-first')"));
    }

    @Test
    public void escape() {
        // (:^[]{}! and ? if specified
        StringBuilder sql2 = new StringBuilder();
        _containsCondition.any("test(").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'test\\(')"));

        sql2.setLength(0);
        new Sql2ContainsCondition().any("test:").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'test\\:')"));

        sql2.setLength(0);
        new Sql2ContainsCondition().any("test[").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'test\\[')"));

        sql2.setLength(0);
        new Sql2ContainsCondition().any("test]").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'test\\]')"));

        sql2.setLength(0);
        new Sql2ContainsCondition().any("test{").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'test\\{')"));

        sql2.setLength(0);
        new Sql2ContainsCondition().any("test}").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'test\\}')"));

        sql2.setLength(0);
        new Sql2ContainsCondition().any("test'").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'test''')"));

        sql2.setLength(0);
        new Sql2ContainsCondition().any("test?").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'test?')"));

        sql2.setLength(0);
        new Sql2ContainsCondition().addTerm(1, false, false, false, 0, true, "test?").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'test\\?')"));
    }

    @Test
    public void rangeInclusive() {
        StringBuilder sql2 = new StringBuilder();
        _containsCondition.range(true, "alpha", "omega").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, '[alpha TO omega]')"));
    }

    @Test
    public void rangeExclusive() {
        StringBuilder sql2 = new StringBuilder();
        _containsCondition.range(false, "alpha", "omega").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, '{alpha TO omega}')"));
    }
}
