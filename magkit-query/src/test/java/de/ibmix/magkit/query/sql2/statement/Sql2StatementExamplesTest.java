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
import org.junit.After;
import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Tests showing examples on how to use the Sql2 query facade.
 *
 * @author wolf.bubenik@ibmix.de
 * @since (21.8.2020)
 */
public class Sql2StatementExamplesTest {

    @Test
    public void selectAll() {
        assertThat(Sql2.Statement.select().build(), is("SELECT * FROM [nt:base]"));
    }

    @Test
    public void selectComponents() {
        assertThat(Sql2.Statement.selectComponents().build(), is("SELECT * FROM [mgnl:component]"));
    }

    @Test
    public void selectComponentsWithExistingProperty() {
        assertThat(
            Sql2.Statement.selectComponents().whereAll(Sql2.Condition.isNotNull("title")).build(),
            is("SELECT * FROM [mgnl:component] WHERE [title] IS NOT NULL")
        );

        assertThat(
            Sql2.Statement.selectComponents().as("t").whereAll(Sql2.Condition.isNotNull("title")).build(),
            is("SELECT * FROM [mgnl:component] AS t WHERE t.[title] IS NOT NULL")
        );
    }

    @Test
    public void selectComponentsWithMissingProperty() {
        assertThat(
            // TODO: whereAll() allows JoinCondition without having selector names -> should allow simple conditions only!
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.isNull("title")
            ).build(),
            is("SELECT * FROM [mgnl:component] WHERE [title] IS NULL")
        );
    }

    @Test
    public void selectComponentsWithPropertyValue() {
        assertThat(
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.String.property("title").equalsAny().values("Test")
            ).build(),
            is("SELECT * FROM [mgnl:component] WHERE [title] = 'Test'")
        );
    }

    @Test
    public void selectComponentsWithPropertyContains() {
        assertThat(
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.String.property("title").likeAny().values("Test")
            ).build(),
            is("SELECT * FROM [mgnl:component] WHERE [title] LIKE '%Test%'")
        );
    }

    @Test
    public void selectComponentsWithPropertyStartsWithIgnoreCase() {
        assertThat(
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.String.property("title").lowerCase().startsWithAny().values("test", "title")
            ).build(),
            is("SELECT * FROM [mgnl:component] WHERE (lower([title]) LIKE 'test%' OR lower([title]) LIKE 'title%')")
        );
    }

    @Test
    public void selectComponentsPublishedBeforeDate() {
        Calendar cal = getCalendar(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        assertThat(
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.Date.lastActivated().lowerThan().value(cal)
            ).build(),
            is("SELECT * FROM [mgnl:component] WHERE [mgnl:lastActivated] < cast('2020-01-01T00:00:00.000+01:00' as date)")
        );
    }

    @Test
    public void selectComponentsCreatedAfterDate() {
        Calendar cal = getCalendar(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        assertThat(
            Sql2.Statement.selectComponents().whereAll(Sql2.Condition.Date.createdAfter(cal)).build(),
            is("SELECT * FROM [mgnl:component] WHERE [mgnl:created] > cast('2020-01-01T00:00:00.000+01:00' as date)")
        );
    }

    @Test
    public void selectComponentsCreatedAfterBindValue() {
        assertThat(
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.Date.created().greaterOrEqualThan().bindVariable("date")
            ).build(),
            is("SELECT * FROM [mgnl:component] WHERE [mgnl:created] >= $date")
        );
    }

    @Test
    public void selectComponentsWithinDateRange() {
        assertThat(
            Sql2.Statement.selectComponents().whereAll(
                Sql2.Condition.Date.property("date").greaterOrEqualThan().bindVariable("from"),
                Sql2.Condition.Date.property("date").lowerThan().bindVariable("to")
            ).build(),
            // TODO: Check if this query really works
            is("SELECT * FROM [mgnl:component] WHERE ([date] >= $from AND [date] < $to)")
        );
    }

    @Test
    public void selectComponentsBelowPath() {
        assertThat(
            Sql2.Statement.selectComponents().whereAll(Sql2.Condition.Path.isDescendant("/root/path")).build(),
            is("SELECT * FROM [mgnl:component] WHERE isdescendantnode('/root/path')")
        );

        assertThat(
            Sql2.Statement.selectComponents().as("t").whereAll(Sql2.Condition.Path.isDescendant("/root/path")).build(),
            is("SELECT * FROM [mgnl:component] AS t WHERE isdescendantnode(t, '/root/path')")
        );
    }

    @Test
    public void selectNotChildComponents() {
        assertThat(
            Sql2.Statement.selectComponents().whereAll(Sql2.Condition.Path.is().not().child("/root/path")).build(),
            is("SELECT * FROM [mgnl:component] WHERE not(ischildnode('/root/path'))")
        );
    }

    @Test
    public void selectNodeByPath() {
        assertThat(
            Sql2.Statement.select().whereAll(Sql2.Condition.Path.is().same("/root/path")).build(),
            is("SELECT * FROM [nt:base] WHERE issamenode('/root/path')")
        );

        assertThat(
            Sql2.Statement.select().as("t").whereAll(Sql2.Condition.Path.is().same("/root/path")).build(),
            is("SELECT * FROM [nt:base] AS t WHERE issamenode(t, '/root/path')")
        );
    }

    @Test
    public void selectByComplexQuery() {
        assertThat(Sql2.Statement.select().whereAny(
                Sql2.Condition.and().matches(
                    Sql2.Condition.Path.isChild("migros/de"),
                    Sql2.Condition.String.templateEquals("myModule:my/component-1", "myModule:my/component-2")
                ),
                Sql2.Condition.and().not().matches(
                    Sql2.Condition.Path.isChild("migros/de"),
                    Sql2.Condition.String.templateEquals("myModule:my/other/component")
                )
            ).build(),
            is("SELECT * FROM [nt:base] WHERE ((ischildnode('/migros/de') AND ([mgnl:template] = 'myModule:my/component-1' OR [mgnl:template] = 'myModule:my/component-2')) OR not(ischildnode('/migros/de') AND [mgnl:template] = 'myModule:my/other/component'))")
        );
    }

    @Test
    public void selectOrderedByScore() {
        assertThat(Sql2.Statement.select().whereAll(
                Sql2.Condition.String.property("title").lowerCase().likeAny().values("test", "toast")
            ).orderByScore().descending().build(),
            is("SELECT * FROM [nt:base] WHERE (lower([title]) LIKE '%test%' OR lower([title]) LIKE '%toast%') ORDER BY [jcr:score] DESC")
        );
    }

    @Test
    public void selectUuidsFromPagesOrdered() {
        assertThat(Sql2.Statement.select("title", "date", "jcr:uuid").from(NodeTypes.Page.NAME).whereAll(
                Sql2.Condition.String.property("title").startsWithAny().values("Test", "Toast")
            ).orderBy("date").ascending().build(),
            is("SELECT [title],[date],[jcr:uuid] FROM [mgnl:page] WHERE ([title] LIKE 'Test%' OR [title] LIKE 'Toast%') ORDER BY [date] ASC")
        );
    }

    // Joins only with row queries, because: javax.jcr.RepositoryException: This query result contains more than one selector
    @Test
    public void selectChildrenOfParentWithTemplate() {
        assertThat(Sql2.Statement.selectComponents().as("content")
                .innerJoin(NodeTypes.Component.NAME).joinAs("container").on(
                    Sql2.JoinOn.selectedDescendantOfJoined()
                )
                .whereAll(
                    Sql2.Condition.String.templateEquals("m5-tk-core-components:components/content/standardImageTextTeaser"),
                    Sql2.Condition.String.templateEquals("m5-relaunch-my-migros:pages/myCumulusPage").forJoin()
                ).build(),
            is("SELECT content.*,container.* FROM [mgnl:component] AS content INNER JOIN [mgnl:component] AS container ON isdescendantnode(content,container) WHERE (content.[mgnl:template] = 'm5-tk-core-components:components/content/standardImageTextTeaser' AND container.[mgnl:template] = 'm5-relaunch-my-migros:pages/myCumulusPage')")
        );
    }

    @Test
    public void selectNodesByName() {
        assertThat(
            Sql2.Statement.selectContentNodes().whereAll(Sql2.Condition.nameEquals("00")).build(),
            is("SELECT * FROM [mgnl:contentNode] WHERE name() = '00'")
        );

        assertThat(
            Sql2.Statement.selectContentNodes().as("t").whereAll(Sql2.Condition.nameEquals("00")).build(),
            is("SELECT * FROM [mgnl:contentNode] AS t WHERE name(t) = '00'")
        );
    }

    @Test
    public void selectNodesWithNameNotEqualAnyCaseInsensitive() {
        assertThat(
            Sql2.Statement.selectContentNodes().whereAll(
                Sql2.Condition.name().lowerCase().excludeAny().values("0", "test")
            ).build(),
            is("SELECT * FROM [mgnl:contentNode] WHERE (lower(name()) <> '0' OR lower(name()) <> 'test')")
        );

        assertThat(
            Sql2.Statement.selectContentNodes().as("t").whereAll(
                Sql2.Condition.name().lowerCase().excludeAny().values("0", "test")
            ).build(),
            is("SELECT * FROM [mgnl:contentNode] AS t WHERE (lower(name(t)) <> '0' OR lower(name(t)) <> 'test')")
        );
    }

    @Test
    public void fullTextSearchForMandatoryWordsUsingWildcards() {
        assertThat(
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains().all("te?t", "hallo*")
            ).orderByScore().build(),
            is("SELECT * FROM [nt:base] AS s WHERE contains(s.*, 'te?t hallo*') ORDER BY [jcr:score] DESC")
        );
    }

    @Test
    public void fullTextSearchForOptionalWordsInTitle() {
        assertThat(
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains("title").any("test", "other text")
            ).build(),
            is("SELECT * FROM [nt:base] AS s WHERE contains(s.title, 'test OR \"other text\"')")
        );
    }

    @Test
    public void fullTextSearchForExcludedWords() {
        assertThat(
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains().excludeAny("test", "other test")
            ).build(),
            is("SELECT * FROM [nt:base] AS s WHERE contains(s.*, '-test OR -\"other test\"')")
        );
    }

    @Test
    public void fuzzyFullTextSearchForWords() {
        assertThat(
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains().all(1, true, "test", "other test")
            ).build(),
            is("SELECT * FROM [nt:base] AS s WHERE contains(s.*, 'test~ \"other test\"')")
        );
    }

    @Test
    public void complexFullTextSearchWithBoosting() {
        assertThat(
            Sql2.Statement.selectComponents().as("s").whereAll(
                Sql2.Condition.FullText.contains()
                    .all(1, false, "test?")
                    .all(2, false, "boosted test")
                    .excludeAll(3, true, "not this phrase!")
            ).build(),
            is("SELECT * FROM [mgnl:component] AS s WHERE contains(s.*, 'test? \"boosted test\"^2 -\"not this phrase!\"^3')")
        );
    }

    @Test
    public void fullTextProximitySearch() {
        assertThat(
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains().addTerm(2, false, false, false, 5, false, "text within 5 words")
            ).build(),
            is("SELECT * FROM [nt:base] AS s WHERE contains(s.*, '\"text within 5 words\"~5^2')")
        );
    }

    @Test
    public void fullTextRangeQueryInclusive() {
        assertThat(
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains("title").range(true, "alpha", "omega")
            ).build(),
            is("SELECT * FROM [nt:base] AS s WHERE contains(s.title, '[alpha TO omega]')")
        );
    }

    @Test
    public void fullTextRangeQueryExclusive() {
        assertThat(
            Sql2.Statement.select().as("s").whereAll(
                Sql2.Condition.FullText.contains("title").range(false, "alpha", "omega")
            ).build(),
            is("SELECT * FROM [nt:base] AS s WHERE contains(s.title, '{alpha TO omega}')")
        );
    }

    private Calendar getCalendar(int year, int month, int date, int hour, int minute, int second) {
        Calendar result = Calendar.getInstance();
        result.set(year, month, date, hour, minute, second);
        result.set(Calendar.MILLISECOND, 0);
        return result;
    }

    @After
    public void tearDown() throws Exception {
        cleanContext();
    }

}
