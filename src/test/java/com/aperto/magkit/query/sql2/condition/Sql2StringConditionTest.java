package com.aperto.magkit.query.sql2.condition;

import org.junit.Test;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test Sql2StringCondition.
 *
 * @author wolf.bubenik@aperto.com
 * @since 02.04.2020
 */
public class Sql2StringConditionTest {

    @Test
    public void property() {
        assertThat(Sql2StringCondition.property("test").excludeAll().values().asString(), is(EMPTY));
        assertThat(Sql2StringCondition.property("test").endsWithAny().values("end").asString(), is("[test] LIKE '%end'"));
        assertThat(Sql2StringCondition.property("test").not().endsWithAny().values("end").asString(), is("not([test] LIKE '%end')"));
    }

    @Test
    public void template() {
        assertThat(Sql2StringCondition.template().equalsAny().values().asString(), is(EMPTY));
        assertThat(Sql2StringCondition.template().startsWithAny().values("start").asString(), is("[mgnl:template] LIKE 'start%'"));
        assertThat(Sql2StringCondition.template().not().startsWithAny().values("start").asString(), is("not([mgnl:template] LIKE 'start%')"));
    }

    @Test
    public void identifier() {
        assertThat(Sql2StringCondition.identifier().equalsAll().values().asString(), is(EMPTY));
        assertThat(Sql2StringCondition.identifier().likeAll().values("one", "other").asString(), is("([jcr:uuid] LIKE '%one%' AND [jcr:uuid] LIKE '%other%')"));
        assertThat(Sql2StringCondition.identifier().not().likeAll().values("one", "other").asString(), is("not([jcr:uuid] LIKE '%one%' AND [jcr:uuid] LIKE '%other%')"));
    }

    @Test
    public void equalsAny() {
        assertThat(Sql2StringCondition.property("key").equalsAny().values().asString(), is(EMPTY));
        assertThat(Sql2StringCondition.property("key").equalsAny().values("").asString(), is("[key] = ''"));
        assertThat(Sql2StringCondition.property("key").equalsAny().values("value'_10%").asString(), is("[key] = 'value''_10%'"));
        assertThat(Sql2StringCondition.property("key").not().equalsAny().values("value'_10%").asString(), is("not([key] = 'value''_10%')"));
        assertThat(Sql2StringCondition.property("key").equalsAny().values("value'_10%", "other").asString(), is("([key] = 'value''_10%' OR [key] = 'other')"));
        assertThat(Sql2StringCondition.property("key").not().equalsAny().values("value'_10%", "other").asString(), is("not([key] = 'value''_10%' OR [key] = 'other')"));
    }

    @Test
    public void startsWithAny() {
        assertThat(Sql2StringCondition.template().startsWithAny().values().asString(), is(EMPTY));
        assertThat(Sql2StringCondition.template().startsWithAny().values(" ").asString(), is("[mgnl:template] LIKE ' %'"));
        assertThat(Sql2StringCondition.template().startsWithAny().values("value'_10%").asString(), is("[mgnl:template] LIKE 'value''\\_10\\%%'"));
        assertThat(Sql2StringCondition.template().not().startsWithAny().values("value'_10%").asString(), is("not([mgnl:template] LIKE 'value''\\_10\\%%')"));
        assertThat(Sql2StringCondition.template().startsWithAny().values("value'_10%", "other").asString(), is("([mgnl:template] LIKE 'value''\\_10\\%%' OR [mgnl:template] LIKE 'other%')"));
        assertThat(Sql2StringCondition.template().not().startsWithAny().values("value'_10%", "other").asString(), is("not([mgnl:template] LIKE 'value''\\_10\\%%' OR [mgnl:template] LIKE 'other%')"));
    }

    @Test
    public void endsWithAny() {
        assertThat(Sql2StringCondition.identifier().endsWithAny().values().asString(), is(EMPTY));
        assertThat(Sql2StringCondition.identifier().endsWithAny().values("").asString(), is(EMPTY));
        assertThat(Sql2StringCondition.identifier().endsWithAny().values(" ").asString(), is("[jcr:uuid] LIKE '% '"));
        assertThat(Sql2StringCondition.identifier().endsWithAny().values("value'_10%").asString(), is("[jcr:uuid] LIKE '%value''\\_10\\%'"));
        assertThat(Sql2StringCondition.identifier().not().endsWithAny().values("value'_10%").asString(), is("not([jcr:uuid] LIKE '%value''\\_10\\%')"));
        assertThat(Sql2StringCondition.identifier().endsWithAny().values("value'_10%", "other").asString(), is("([jcr:uuid] LIKE '%value''\\_10\\%' OR [jcr:uuid] LIKE '%other')"));
        assertThat(Sql2StringCondition.identifier().not().endsWithAny().values("value'_10%", "other").asString(), is("not([jcr:uuid] LIKE '%value''\\_10\\%' OR [jcr:uuid] LIKE '%other')"));
    }

    @Test
    public void likeAny() {
        assertThat(Sql2StringCondition.template().likeAny().values().asString(), is(EMPTY));
        assertThat(Sql2StringCondition.template().likeAny().values("").asString(), is(EMPTY));
        assertThat(Sql2StringCondition.template().likeAny().values(" ").asString(), is("[mgnl:template] LIKE '% %'"));
        assertThat(Sql2StringCondition.template().likeAny().values("value'_10%").asString(), is("[mgnl:template] LIKE '%value''\\_10\\%%'"));
        assertThat(Sql2StringCondition.template().not().likeAny().values("value'_10%").asString(), is("not([mgnl:template] LIKE '%value''\\_10\\%%')"));
        assertThat(Sql2StringCondition.template().likeAny().values("value'_10%", "other").asString(), is("([mgnl:template] LIKE '%value''\\_10\\%%' OR [mgnl:template] LIKE '%other%')"));
        assertThat(Sql2StringCondition.template().not().likeAny().values("value'_10%", "other").asString(), is("not([mgnl:template] LIKE '%value''\\_10\\%%' OR [mgnl:template] LIKE '%other%')"));
    }

    @Test
    public void likeAll() {
        assertThat(Sql2StringCondition.template().likeAll().values().asString(), is(EMPTY));
        assertThat(Sql2StringCondition.template().likeAll().values("").asString(), is(EMPTY));
        assertThat(Sql2StringCondition.template().likeAll().values(" ").asString(), is("[mgnl:template] LIKE '% %'"));
        assertThat(Sql2StringCondition.template().likeAll().values("value'_10%").asString(), is("[mgnl:template] LIKE '%value''\\_10\\%%'"));
        assertThat(Sql2StringCondition.template().not().likeAll().values("value'_10%").asString(), is("not([mgnl:template] LIKE '%value''\\_10\\%%')"));
        assertThat(Sql2StringCondition.template().likeAll().values("value'_10%", "other").asString(), is("([mgnl:template] LIKE '%value''\\_10\\%%' AND [mgnl:template] LIKE '%other%')"));
        assertThat(Sql2StringCondition.template().not().likeAll().values("value'_10%", "other").asString(), is("not([mgnl:template] LIKE '%value''\\_10\\%%' AND [mgnl:template] LIKE '%other%')"));
    }

    @Test
    public void length() {

    }

    @Test
    public void lowerCase() {
        assertThat(Sql2StringCondition.property("test").lowerCase().equalsAny().values("value").asString(), is("lower([test]) = 'value'"));
        assertThat(Sql2StringCondition.property("test").lowerCase().not().equalsAny().values("value").asString(), is("not(lower([test]) = 'value')"));
    }

    @Test
    public void upperCase() {
        assertThat(Sql2StringCondition.property("test").upperCase().equalsAny().values("value").asString(), is("upper([test]) = 'value'"));
        assertThat(Sql2StringCondition.property("test").upperCase().not().equalsAny().values("value").asString(), is("not(upper([test]) = 'value')"));
    }
}