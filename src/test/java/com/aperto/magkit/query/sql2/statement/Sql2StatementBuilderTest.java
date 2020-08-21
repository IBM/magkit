package com.aperto.magkit.query.sql2.statement;

import com.aperto.magkit.query.sql2.condition.Sql2PathCondition;
import com.aperto.magkit.query.sql2.condition.Sql2PathJoinCondition;
import com.aperto.magkit.query.sql2.condition.Sql2StringCondition;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test Sql2StatementBuilder.
 *
 * @author wolf.bubenik@aperto.com
 * @since 06.04.2020
 */
public class Sql2StatementBuilderTest {

    @Test
    public void selectAll() {
        assertThat(Sql2Statement.selectAll().build(), is("SELECT * FROM [nt:base]"));
    }

    @Test
    public void selectAttributes() {
        assertThat(Sql2Statement.selectAttributes().build(), is("SELECT * FROM [nt:base]"));
        assertThat(Sql2Statement.selectAttributes("test").build(), is("SELECT [test] FROM [nt:base]"));
        assertThat(Sql2Statement.selectAttributes("test", "other").build(), is("SELECT [test],[other] FROM [nt:base]"));
    }

    @Test
    public void from() {
        assertThat(Sql2Statement.selectAll().from(null).build(), is("SELECT * FROM [nt:base]"));
        assertThat(Sql2Statement.selectAll().from("").build(), is("SELECT * FROM [nt:base]"));
        assertThat(Sql2Statement.selectAll().from("mgnl:page").build(), is("SELECT * FROM [mgnl:page]"));
    }

    @Test
    public void selectAs() {
        assertThat(Sql2Statement.selectAll().selectAs("s").whereAll(
            Sql2PathCondition.is().descendant("/some/root/path"),
            Sql2StringCondition.property("title").equalsAny().values("test")
        ).build(),
            is("SELECT s.* FROM [nt:base] AS s WHERE (isdescendantnode(s, '/some/root/path') AND s.[title] = 'test')")
        );
    }

    @Test
    public void innerJoin() {
        assertThat(Sql2Statement.selectAll().selectAs("s")
            .innerJoin("nt:base").joinAs("j").on(Sql2PathJoinCondition.isJoinedDescendantOfSelected())
            .whereAll(
                Sql2StringCondition.template().equalsAll().values("selected.template.id"),
                Sql2StringCondition.template().equalsAll().values("joined.template.id").forJoin()
            ).build(),
            is("SELECT s.*,j.* FROM [nt:base] AS s INNER JOIN [nt:base] AS j ON isdescendantnode(s,j) WHERE (s.[mgnl:template] = 'selected.template.id' AND j.[mgnl:template] = 'joined.template.id')")
        );
    }

    @Test
    public void whereAll() {
        assertThat(Sql2Statement.selectAll().whereAll(
            Sql2PathCondition.is().descendant("/some/path"),
            Sql2StringCondition.property("test").equalsAny().values("value")
        ).build(), is("SELECT * FROM [nt:base] WHERE (isdescendantnode('/some/path') AND [test] = 'value')"));
    }

    @Test
    public void whereAny() {
        assertThat(Sql2Statement.selectAll().whereAny(
            Sql2PathCondition.is().descendant("/some/path"),
            Sql2StringCondition.property("test").equalsAny().values("value")
        ).build(), is("SELECT * FROM [nt:base] WHERE (isdescendantnode('/some/path') OR [test] = 'value')"));
    }

    @Test
    public void orderDescBy() {
        assertThat(Sql2Statement.selectAll().whereAll(
            Sql2PathCondition.is().descendant("/some/path")
        ).orderBy("test").toString(), is("SELECT * FROM [nt:base] WHERE isdescendantnode('/some/path') ORDER BY [test] DESC"));
    }

    @Test
    public void orderDescByScore() {
        assertThat(Sql2Statement.selectAll().whereAll(
                Sql2PathCondition.is().descendant("/some/path")
        ).orderByScore().toString(), is("SELECT * FROM [nt:base] WHERE isdescendantnode('/some/path') ORDER BY [jcr:score] DESC"));
    }

    @Test
    public void descending() {
        assertThat(Sql2Statement.selectAll().whereAny(
            Sql2PathCondition.is().descendant("/some/path")
        ).orderBy("test")
        .descending().build(), is("SELECT * FROM [nt:base] WHERE isdescendantnode('/some/path') ORDER BY [test] DESC"));
    }

    @Test
    public void ascending() {
        assertThat(Sql2Statement.selectAll().whereAny(
            Sql2PathCondition.is().descendant("/some/path")
        ).orderBy("test")
        .ascending().build(), is("SELECT * FROM [nt:base] WHERE isdescendantnode('/some/path') ORDER BY [test] ASC"));
    }
}