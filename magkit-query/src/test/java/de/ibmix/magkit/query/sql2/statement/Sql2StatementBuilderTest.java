package de.ibmix.magkit.query.sql2.statement;

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

import de.ibmix.magkit.query.sql2.condition.Sql2PathCondition;
import de.ibmix.magkit.query.sql2.condition.Sql2PathJoinCondition;
import de.ibmix.magkit.query.sql2.condition.Sql2StringCondition;
import org.apache.jackrabbit.JcrConstants;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Sql2Statement builder.
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-06
 */
public class Sql2StatementBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(Sql2StatementBuilderTest.class);

    @Test
    public void selectAll() {
        assertEquals("SELECT * FROM [nt:base]", Sql2Statement.select().build());
    }

    @Test
    public void selectAttributes() {
        assertEquals("SELECT * FROM [nt:base]", Sql2Statement.select().build());
        assertEquals("SELECT [test] FROM [nt:base]", Sql2Statement.select("test").build());
        assertEquals("SELECT [test],[other] FROM [nt:base]", Sql2Statement.select("test", "other").build());
    }

    @Test
    public void from() {
        assertEquals("SELECT * FROM [nt:base]", Sql2Statement.select().from(null).build());
        assertEquals("SELECT * FROM [nt:base]", Sql2Statement.select().from("").build());
        assertEquals("SELECT * FROM [mgnl:page]", Sql2Statement.select().from("mgnl:page").build());
    }

    @Test
    public void selectAs() {
        assertEquals("SELECT * FROM [nt:base] AS s WHERE (isdescendantnode(s, '/some/root/path') AND s.[title] = 'test')",
            Sql2Statement.select().as("s").whereAll(
                Sql2PathCondition.is().descendant("/some/root/path"),
                Sql2StringCondition.property("title").equalsAny().values("test")
            ).build()
        );
    }

    @Test
    public void innerJoin() {
        assertEquals("SELECT s.*,j.* FROM [nt:base] AS s INNER JOIN [nt:base] AS j ON isdescendantnode(j,s) WHERE (s.[mgnl:template] = 'selected.template.id' AND j.[mgnl:template] = 'joined.template.id')",
            Sql2Statement.select().as("s")
                .innerJoin("nt:base").joinAs("j").on(Sql2PathJoinCondition.isJoinedDescendantOfSelected())
                .whereAll(
                    Sql2StringCondition.template().equalsAll().values("selected.template.id"),
                    Sql2StringCondition.template().equalsAll().values("joined.template.id").forJoin()
                ).build()
        );
    }

    @Test
    public void whereAll() {
        assertEquals("SELECT * FROM [nt:base] WHERE (isdescendantnode('/some/path') AND [test] = 'value')",
            Sql2Statement.select().whereAll(
                Sql2PathCondition.is().descendant("/some/path"),
                Sql2StringCondition.property("test").equalsAny().values("value")
            ).build());
    }

    @Test
    public void whereAny() {
        assertEquals("SELECT * FROM [nt:base] WHERE (isdescendantnode('/some/path') OR [test] = 'value')",
            Sql2Statement.select().whereAny(
                Sql2PathCondition.is().descendant("/some/path"),
                Sql2StringCondition.property("test").equalsAny().values("value")
            ).build());
    }

    @Test
    public void orderDescBy() {
        assertEquals("SELECT * FROM [nt:base] WHERE isdescendantnode('/some/path') ORDER BY [test] DESC",
            Sql2Statement.select().whereAll(
                Sql2PathCondition.is().descendant("/some/path")
            ).orderBy("test").toString());

        assertEquals("SELECT * FROM [nt:base] ORDER BY [test1] DESC, [test2] DESC, [test3] DESC",
            Sql2Statement.select().orderBy("test1", "test2", "test3").toString());
    }

    @Test
    public void orderDescByScore() {
        assertEquals("SELECT * FROM [nt:base] WHERE isdescendantnode('/some/path') ORDER BY [jcr:score] DESC",
            Sql2Statement.select().whereAll(
                Sql2PathCondition.is().descendant("/some/path")
            ).orderByScore().toString());
    }

    @Test
    public void descending() {
        assertEquals("SELECT * FROM [nt:base] WHERE isdescendantnode('/some/path') ORDER BY [test] DESC",
            Sql2Statement.select().whereAny(
                Sql2PathCondition.is().descendant("/some/path")
            ).orderBy("test").descending().build());
    }

    @Test
    public void ascending() {
        assertEquals("SELECT * FROM [nt:base] AS p ORDER BY [test1] ASC, [test2] ASC",
            Sql2Statement.select().as("p").orderBy("test1", "test2").ascending().build());
    }

    @Test
    public void performance() {
        String type = JcrConstants.NT_BASE;
        String path = "/some/path";
        String property = "test";
        String value = "value";
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            Sql2Statement.select().whereAny(
                Sql2PathCondition.is().descendant(path),
                Sql2StringCondition.property(property).equalsAny().values(value + i)
            ).build();
        }
        long time = System.currentTimeMillis() - start;
        LOG.info("Statement builder: " + time + " ms");

        start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            String result = "SELECT * FROM " + '[' + type + ']' + " WHERE (isdescendantnode('" + path + "') OR " + '[' + property + ']' + " = " + '\'' + value + i + '\'' + ")";
        }
        time = System.currentTimeMillis() - start;
        LOG.info("String concatenation: " + time + " ms");

        final String template = "SELECT * FROM [%s] WHERE (isdescendantnode('%s') OR [%s] = '%s')";
        start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            String.format(template, type, path, property, value + i);
        }
        time = System.currentTimeMillis() - start;
        LOG.info("String.format : " + time + " ms");
    }
}
