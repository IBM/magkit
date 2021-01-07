package com.aperto.magkit.query.sql2.condition;

import com.aperto.magkit.query.sql2.statement.Sql2SelectorNames;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
        _containsCondition.all(" first", "second ").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'first second')"));

        sql2.setLength(0);
        _containsCondition.all(2, true, "boosted fuzzy").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'first second \"boosted fuzzy\"~^2')"));
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
        _containsCondition.any(3, true, "boosted fuzzy").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, 'first OR second OR \"boosted fuzzy\"~^3')"));
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
    public void illegalXpathChars() {
        StringBuilder sql2 = new StringBuilder();
        _containsCondition.any("what's up?").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, '\"what''s up\\?\"')"));

        sql2.setLength(0);
        _containsCondition.any("(?:^[]{}!").appendTo(sql2, SELECTOR_NAMES);
        assertThat(sql2.toString(), is("contains(from.*, '\"what''s up\\?\" OR (?:^[]{}\\!')"));
    }
}