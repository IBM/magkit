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

import de.ibmix.magkit.query.sql2.Sql2;
import info.magnolia.jcr.util.NodeTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Usage examples for Sql2 statement builder.
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-21
 */
public class Sql2StatementExamplesTest {

    @Test
    public void selectAll() {
        assertEquals("SELECT * FROM [nt:base]", Sql2.Statement.select().build());
    }

    @Test
    public void selectComponents() {
        assertEquals("SELECT * FROM [mgnl:component]", Sql2.Statement.selectComponents().build());
    }

    @Test
    public void selectComponentsWithExistingProperty() {
        assertEquals(
            "SELECT * FROM [mgnl:component] WHERE [title] IS NOT NULL",
            Sql2.Statement.selectComponents().whereAll(Sql2.Condition.isNotNull("title")).build()
        );

        assertEquals(
            "SELECT * FROM [mgnl:component] AS t WHERE t.[title] IS NOT NULL",
            Sql2.Statement.selectComponents().as("t").whereAll(Sql2.Condition.isNotNull("title")).build()
        );
    }

    @Test
    public void selectComponentsWithMissingProperty() {
        assertEquals(
            "SELECT * FROM [mgnl:component] WHERE [title] IS NULL",
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.isNull("title")
            ).build()
        );
    }

    @Test
    public void selectComponentsWithPropertyValue() {
        assertEquals(
            "SELECT * FROM [mgnl:component] WHERE [title] = 'Test'",
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.String.property("title").equalsAny().values("Test")
            ).build()
        );
    }

    @Test
    public void selectComponentsWithPropertyContains() {
        assertEquals(
            "SELECT * FROM [mgnl:component] WHERE [title] LIKE '%Test%'",
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.String.property("title").likeAny().values("Test")
            ).build()
        );
    }

    @Test
    public void selectComponentsWithPropertyStartsWithIgnoreCase() {
        assertEquals(
            "SELECT * FROM [mgnl:component] WHERE (lower([title]) LIKE 'test%' OR lower([title]) LIKE 'title%')",
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.String.property("title").lowerCase().startsWithAny().values("test", "title")
            ).build()
        );
    }

    @Test
    public void selectComponentsPublishedBeforeDate() {
        Calendar cal = getCalendar(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        assertEquals(
            "SELECT * FROM [mgnl:component] WHERE [mgnl:lastActivated] < cast('2020-01-01T00:00:00.000+01:00' as date)",
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.Date.lastActivated().lowerThan().value(cal)
            ).build()
        );
    }

    @Test
    public void selectComponentsCreatedAfterDate() {
        Calendar cal = getCalendar(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        assertEquals(
            "SELECT * FROM [mgnl:component] WHERE [mgnl:created] > cast('2020-01-01T00:00:00.000+01:00' as date)",
            Sql2.Statement.selectComponents().whereAll(Sql2.Condition.Date.createdAfter(cal)).build()
        );
    }

    @Test
    public void selectComponentsCreatedAfterBindValue() {
        assertEquals(
            "SELECT * FROM [mgnl:component] WHERE [mgnl:created] >= $date",
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.Date.created().greaterOrEqualThan().bindVariable("date")
            ).build()
        );
    }

    @Test
    public void selectComponentsWithinDateRange() {
        assertEquals(
            "SELECT * FROM [mgnl:component] WHERE ([date] >= $from AND [date] < $to)",
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.Date.property("date").greaterOrEqualThan().bindVariable("from"),
                Sql2.Condition.Date.property("date").lowerThan().bindVariable("to")
            ).build()
        );
    }

    @Test
    public void selectComponentsBelowPath() {
        assertEquals(
            "SELECT * FROM [mgnl:component] WHERE isdescendantnode('/root/path')",
            Sql2.Statement.selectComponents().whereAll(Sql2.Condition.Path.isDescendant("/root/path")).build()
        );

        assertEquals(
            "SELECT * FROM [mgnl:component] AS t WHERE isdescendantnode(t, '/root/path')",
            Sql2.Statement.selectComponents().as("t").whereAll(Sql2.Condition.Path.isDescendant("/root/path")).build()
        );
    }

    @Test
    public void selectNotChildComponents() {
        assertEquals(
            "SELECT * FROM [mgnl:component] WHERE not(ischildnode('/root/path'))",
            Sql2.Statement.selectComponents().whereAll(Sql2.Condition.Path.is().not().child("/root/path")).build()
        );
    }

    @Test
    public void selectNodeByPath() {
        assertEquals(
            "SELECT * FROM [nt:base] WHERE issamenode('/root/path')",
            Sql2.Statement.select().whereAll(Sql2.Condition.Path.is().same("/root/path")).build()
        );

        assertEquals(
            "SELECT * FROM [nt:base] AS t WHERE issamenode(t, '/root/path')",
            Sql2.Statement.select().as("t").whereAll(Sql2.Condition.Path.is().same("/root/path")).build()
        );
    }

    @Test
    public void selectByComplexQuery() {
        assertEquals("SELECT * FROM [nt:base] WHERE ((ischildnode('/migros/de') AND ([mgnl:template] = 'myModule:my/component-1' OR [mgnl:template] = 'myModule:my/component-2')) OR not(ischildnode('/migros/de') AND [mgnl:template] = 'myModule:my/other/component'))",
            Sql2.Statement.select().whereAny(
                Sql2.Condition.and().matches(
                    Sql2.Condition.Path.isChild("migros/de"),
                    Sql2.Condition.String.templateEquals("myModule:my/component-1", "myModule:my/component-2")
                ),
                Sql2.Condition.and().not().matches(
                    Sql2.Condition.Path.isChild("migros/de"),
                    Sql2.Condition.String.templateEquals("myModule:my/other/component")
                )
            ).build()
        );
    }

    @Test
    public void selectOrderedByScore() {
        assertEquals("SELECT * FROM [nt:base] WHERE (lower([title]) LIKE '%test%' OR lower([title]) LIKE '%toast%') ORDER BY [jcr:score] DESC",
            Sql2.Statement.select().whereAll(
                Sql2.Condition.String.property("title").lowerCase().likeAny().values("test", "toast")
            ).orderByScore().descending().build()
        );
    }

    @Test
    public void selectUuidsFromPagesOrdered() {
        assertEquals("SELECT [title],[date],[jcr:uuid] FROM [mgnl:page] WHERE ([title] LIKE 'Test%' OR [title] LIKE 'Toast%') ORDER BY [date] ASC",
            Sql2.Statement.select("title", "date", "jcr:uuid").from(NodeTypes.Page.NAME).whereAll(
                Sql2.Condition.String.property("title").startsWithAny().values("Test", "Toast")
            ).orderBy("date").ascending().build()
        );
    }

    @Test
    public void selectChildrenOfParentWithTemplate() {
        assertEquals("SELECT content.*,container.* FROM [mgnl:component] AS content INNER JOIN [mgnl:component] AS container ON isdescendantnode(content,container) WHERE (content.[mgnl:template] = 'm5-tk-core-components:components/content/standardImageTextTeaser' AND container.[mgnl:template] = 'm5-relaunch-my-migros:pages/myCumulusPage')",
            Sql2.Statement.selectComponents().as("content")
                .innerJoin(NodeTypes.Component.NAME).joinAs("container").on(
                    Sql2.JoinOn.selectedDescendantOfJoined()
                )
                .whereAll(
                    Sql2.Condition.String.templateEquals("m5-tk-core-components:components/content/standardImageTextTeaser"),
                    Sql2.Condition.String.templateEquals("m5-relaunch-my-migros:pages/myCumulusPage").forJoin()
                ).build()
        );
    }

    @Test
    public void selectNodesByName() {
        assertEquals(
            "SELECT * FROM [mgnl:contentNode] WHERE name() = '00'",
            Sql2.Statement.selectContentNodes().whereAll(Sql2.Condition.nameEquals("00")).build()
        );

        assertEquals(
            "SELECT * FROM [mgnl:contentNode] AS t WHERE name(t) = '00'",
            Sql2.Statement.selectContentNodes().as("t").whereAll(Sql2.Condition.nameEquals("00")).build()
        );
    }

    @Test
    public void selectNodesWithNameNotEqualAnyCaseInsensitive() {
        assertEquals(
            "SELECT * FROM [mgnl:contentNode] WHERE (lower(name()) <> '0' OR lower(name()) <> 'test')",
            Sql2.Statement.selectContentNodes().whereAll(
                Sql2.Condition.name().lowerCase().excludeAny().values("0", "test")
            ).build()
        );

        assertEquals(
            "SELECT * FROM [mgnl:contentNode] AS t WHERE (lower(name(t)) <> '0' OR lower(name(t)) <> 'test')",
            Sql2.Statement.selectContentNodes().as("t").whereAll(
                Sql2.Condition.name().lowerCase().excludeAny().values("0", "test")
            ).build()
        );
    }

    @Test
    public void fullTextSearchForMandatoryWordsUsingWildcards() {
        assertEquals(
            "SELECT * FROM [nt:base] AS s WHERE contains(s.*, 'te?t hallo*') ORDER BY [jcr:score] DESC",
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains().all("te?t", "hallo*")
            ).orderByScore().build()
        );
    }

    @Test
    public void fullTextSearchForOptionalWordsInTitle() {
        assertEquals(
            "SELECT * FROM [nt:base] AS s WHERE contains(s.title, 'test OR \"other text\"')",
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains("title").any("test", "other text")
            ).build()
        );
    }

    @Test
    public void fullTextSearchForExcludedWords() {
        assertEquals(
            "SELECT * FROM [nt:base] AS s WHERE contains(s.*, '-test OR -\"other test\"')",
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains().excludeAny("test", "other test")
            ).build()
        );
    }

    @Test
    public void fuzzyFullTextSearchForWords() {
        assertEquals(
            "SELECT * FROM [nt:base] AS s WHERE contains(s.*, 'test~ \"other test\"')",
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains().all(1, true, "test", "other test")
            ).build()
        );
    }

    @Test
    public void complexFullTextSearchWithBoosting() {
        assertEquals(
            "SELECT * FROM [mgnl:component] AS s WHERE contains(s.*, 'test? \"boosted test\"^2 -\"not this phrase!\"^3')",
            Sql2.Statement.selectComponents().as("s").whereAll(
                Sql2.Condition.FullText.contains()
                    .all(1, false, "test?")
                    .all(2, false, "boosted test")
                    .excludeAll(3, true, "not this phrase!")
            ).build()
        );
    }

    @Test
    public void fullTextProximitySearch() {
        assertEquals(
            "SELECT * FROM [nt:base] AS s WHERE contains(s.*, '\"text within 5 words\"~5^2')",
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains().addTerm(2, false, false, false, 5, false, "text within 5 words")
            ).build()
        );
    }

    @Test
    public void fullTextRangeQueryInclusive() {
        assertEquals(
            "SELECT * FROM [nt:base] AS s WHERE contains(s.title, '[alpha TO omega]')",
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains("title").range(true, "alpha", "omega")
            ).build()
        );
    }

    @Test
    public void fullTextRangeQueryExclusive() {
        assertEquals(
            "SELECT * FROM [nt:base] AS s WHERE contains(s.title, '{alpha TO omega}')",
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains("title").range(false, "alpha", "omega")
            ).build()
        );
    }

    private Calendar getCalendar(int year, int month, int date, int hour, int minute, int second) {
        Calendar result = Calendar.getInstance();
        result.set(year, month, date, hour, minute, second);
        result.set(Calendar.MILLISECOND, 0);
        return result;
    }

    @AfterEach
    public void tearDown() throws Exception {
        cleanContext();
    }

}
