package com.aperto.magkit.query.sql2.statement;

import com.aperto.magkit.query.sql2.condition.Sql2PathCondition;
import com.aperto.magkit.query.sql2.condition.Sql2PathJoinCondition;
import com.aperto.magkit.query.sql2.condition.Sql2StringCondition;
import org.apache.jackrabbit.JcrConstants;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test Sql2StatementBuilder.
 *
 * @author wolf.bubenik@aperto.com
 * @since 06.04.2020
 */
public class Sql2StatementBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(Sql2StatementBuilderTest.class);

    @Test
    public void selectAll() {
        assertThat(Sql2Statement.select().build(), is("SELECT * FROM [nt:base]"));
    }

    @Test
    public void selectAttributes() {
        assertThat(Sql2Statement.select().build(), is("SELECT * FROM [nt:base]"));
        assertThat(Sql2Statement.select("test").build(), is("SELECT [test] FROM [nt:base]"));
        assertThat(Sql2Statement.select("test", "other").build(), is("SELECT [test],[other] FROM [nt:base]"));
    }

    @Test
    public void from() {
        assertThat(Sql2Statement.select().from(null).build(), is("SELECT * FROM [nt:base]"));
        assertThat(Sql2Statement.select().from("").build(), is("SELECT * FROM [nt:base]"));
        assertThat(Sql2Statement.select().from("mgnl:page").build(), is("SELECT * FROM [mgnl:page]"));
    }

    @Test
    public void selectAs() {
        // DO not prefix select attributes by selector name if we have only one selector:
        assertThat(Sql2Statement.select().as("s").whereAll(
            Sql2PathCondition.is().descendant("/some/root/path"),
            Sql2StringCondition.property("title").equalsAny().values("test")
        ).build(),
            is("SELECT * FROM [nt:base] AS s WHERE (isdescendantnode(s, '/some/root/path') AND s.[title] = 'test')")
        );
    }

    @Test
    public void innerJoin() {
        assertThat(Sql2Statement.select().as("s")
            .innerJoin("nt:base").joinAs("j").on(Sql2PathJoinCondition.isJoinedDescendantOfSelected())
            .whereAll(
                Sql2StringCondition.template().equalsAll().values("selected.template.id"),
                Sql2StringCondition.template().equalsAll().values("joined.template.id").forJoin()
            ).build(),
            is("SELECT s.*,j.* FROM [nt:base] AS s INNER JOIN [nt:base] AS j ON isdescendantnode(j,s) WHERE (s.[mgnl:template] = 'selected.template.id' AND j.[mgnl:template] = 'joined.template.id')")
        );
    }

    @Test
    public void whereAll() {
        assertThat(Sql2Statement.select().whereAll(
            Sql2PathCondition.is().descendant("/some/path"),
            Sql2StringCondition.property("test").equalsAny().values("value")
        ).build(), is("SELECT * FROM [nt:base] WHERE (isdescendantnode('/some/path') AND [test] = 'value')"));
    }

    @Test
    public void whereAny() {
        assertThat(Sql2Statement.select().whereAny(
            Sql2PathCondition.is().descendant("/some/path"),
            Sql2StringCondition.property("test").equalsAny().values("value")
        ).build(), is("SELECT * FROM [nt:base] WHERE (isdescendantnode('/some/path') OR [test] = 'value')"));
    }

    @Test
    public void orderDescBy() {
        assertThat(Sql2Statement.select().whereAll(
            Sql2PathCondition.is().descendant("/some/path")
        ).orderBy("test").toString(), is("SELECT * FROM [nt:base] WHERE isdescendantnode('/some/path') ORDER BY [test] DESC"));

        assertThat(Sql2Statement.select().orderBy("test1", "test2", "test3").toString(), is("SELECT * FROM [nt:base] ORDER BY [test1] DESC, [test2] DESC, [test3] DESC"));
    }

    @Test
    public void orderDescByScore() {
        assertThat(Sql2Statement.select().whereAll(
                Sql2PathCondition.is().descendant("/some/path")
        ).orderByScore().toString(), is("SELECT * FROM [nt:base] WHERE isdescendantnode('/some/path') ORDER BY [jcr:score] DESC"));
    }

    @Test
    public void descending() {
        assertThat(Sql2Statement.select().whereAny(
                Sql2PathCondition.is().descendant("/some/path")
            ).orderBy("test").descending().build(),
            is("SELECT * FROM [nt:base] WHERE isdescendantnode('/some/path') ORDER BY [test] DESC")
        );
    }

    @Test
    public void ascending() {
        assertThat(Sql2Statement.select().orderBy("test1", "test2").ascending().build(),
            is("SELECT * FROM [nt:base] ORDER BY [test1] ASC, [test2] ASC")
        );
    }

    @Test
    public void performance() {
        String type = JcrConstants.NT_BASE;
        String path = "/some/path";
        String property = "test";
        String value = "value";
        String result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            result = Sql2Statement.select().whereAny(
                Sql2PathCondition.is().descendant(path),
                Sql2StringCondition.property(property).equalsAny().values(value + i)
            ).build();
        }
        long time = System.currentTimeMillis() - start;
        LOG.info("Time: " + time + " ms");

        start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            result = "SELECT * FROM " + '[' + type + ']' + " WHERE (isdescendantnode('" + path + "') OR " + '[' + property + ']' + " = " + '\'' + value + i + '\'' + ")";
        }
        time = System.currentTimeMillis() - start;
        LOG.info("Time: " + time + " ms");

        final String template = "SELECT * FROM [%s] WHERE (isdescendantnode('%s') OR [%s] = '%s')";
        start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            result = String.format(template, type, path, property, value + i);
        }
        time = System.currentTimeMillis() - start;
        LOG.info("Time: " + time + " ms");
    }
}