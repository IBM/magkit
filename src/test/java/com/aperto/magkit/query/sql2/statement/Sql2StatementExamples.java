package com.aperto.magkit.query.sql2.statement;

import com.aperto.magkit.mockito.ContextMockUtils;
import com.aperto.magkit.query.sql2.Sql2;
import info.magnolia.jcr.util.NodeTypes;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests showing examples on how to use the Sql2 query facade.
 */
public class Sql2StatementExamples {
    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void selectAll() {
        assertThat(Sql2.Statement.selectAll().build(), is("SELECT * FROM [nt:base]"));
    }

    @Test
    public void selectComponents() {
        assertThat(Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).build(), is("SELECT * FROM [mgnl:component]"));
    }

    @Test
    public void selectComponentsWithExistingProperty() {
        assertThat(
            Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).whereAll(Sql2.Condition.isNotNull("title")).build(),
            is("SELECT * FROM [mgnl:component] WHERE [title] IS NOT NULL")
        );
    }

    @Test
    public void selectComponentsWithMissingProperty() {
        assertThat(
            // TODO: whereAll() allows JoinCondition without having selector names -> should allow simple conditions only!
            Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).whereAll(Sql2.Condition.isNull("title")).build(),
            is("SELECT * FROM [mgnl:component] WHERE [title] IS NULL")
        );
    }

    @Test
    public void selectComponentsWithPropertyValue() {
        assertThat(
            Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).whereAll(Sql2.Condition.String.property("title").equalsAny().values("Test")).build(),
            is("SELECT * FROM [mgnl:component] WHERE [title] = 'Test'")
        );
    }

    @Test
    public void selectComponentsWithPropertyContains() {
        assertThat(
            Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).whereAll(Sql2.Condition.String.property("title").likeAny().values("Test")).build(),
            is("SELECT * FROM [mgnl:component] WHERE [title] LIKE '%Test%'")
        );
    }

    @Test
    public void selectComponentsWithPropertyStartsWith() {
        assertThat(
            Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).whereAll(Sql2.Condition.String.property("title").startsWithAny().values("Test", "Title")).build(),
            is("SELECT * FROM [mgnl:component] WHERE ([title] LIKE 'Test%' OR [title] LIKE 'Title%')")
        );
    }

    @Test
    public void selectComponentsPublishedBeforeDate() {
        Calendar cal = getCalendar(2020, Calendar.JANUARY, 1, 0, 0, 0);
        assertThat(
            Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).whereAll(Sql2.Condition.lastActivated().lowerThan().value(cal)).build(),
            is("SELECT * FROM [mgnl:component] WHERE [mgnl:lastActivated] < cast('2020-01-01T00:00:00.000+01:00' as date)")
        );
    }

    @Test
    public void selectComponentsCreatedAfterDate() {
        Calendar cal = getCalendar(2020, Calendar.JANUARY, 1, 0, 0, 0);
        assertThat(
            Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).whereAll(Sql2.Condition.createdAfter(cal)).build(),
            is("SELECT * FROM [mgnl:component] WHERE [mgnl:created] > cast('2020-01-01T00:00:00.000+01:00' as date)")
        );
    }

    @Test
    public void selectComponentsCreatedAfterBindValue() {
        assertThat(
            Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).whereAll(Sql2.Condition.created().greaterOrEqualThan().bindVariable("date")).build(),
            is("SELECT * FROM [mgnl:component] WHERE [mgnl:created] >= $date")
        );
    }

    @Test
    public void selectComponentsWithinDateRange() {
        Calendar from = getCalendar(2020, Calendar.JANUARY, 1, 0, 0, 0);
        Calendar to = getCalendar(2020, Calendar.MARCH, 1, 0, 0, 0);
        assertThat(
            Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).whereAll(
                Sql2.Condition.Date.property("date").greaterOrEqualThan().value(from),
                Sql2.Condition.Date.property("date").lowerThan().value(to)
            ).build(),
            is("SELECT * FROM [mgnl:component] WHERE ([date] >= cast('2020-01-01T00:00:00.000+01:00' as date) AND [date] < cast('2020-03-01T00:00:00.000+01:00' as date))")
        );
    }

    @Test
    public void selectComponentsBelowPath() {
        assertThat(
            Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).whereAll(Sql2.Condition.Path.isDescendant("/root/path")).build(),
            is("SELECT * FROM [mgnl:component] WHERE isdescendantnode('/root/path')")
        );
    }

    @Test
    public void selectNotChildComponents() {
        assertThat(
            Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).whereAll(Sql2.Condition.Path.is().not().child("/root/path")).build(),
            is("SELECT * FROM [mgnl:component] WHERE not(ischildnode('/root/path'))")
        );
    }

    @Test
    public void selectNodeByPath() {
        assertThat(
            Sql2.Statement.selectAll().whereAll(Sql2.Condition.Path.is().same("/root/path")).build(),
            is("SELECT * FROM [nt:base] WHERE issamenode('/root/path')")
        );
    }

    @Test
    public void selectByComplexQuery() {
        assertThat(Sql2.Statement.selectAll().whereAny(
            Sql2.Condition.and().matches(
                Sql2.Condition.Path.isChild("migros/de"),
                Sql2.Condition.template("myModule:my/component-1", "myModule:my/component-2")
            ),
            Sql2.Condition.and().not().matches(
                Sql2.Condition.Path.isChild("migros/de"),
                Sql2.Condition.template("myModule:my/other/component")
            )
        ).build(),
            is("SELECT * FROM [nt:base] WHERE ((ischildnode('/migros/de') AND ([mgnl:template] = 'myModule:my/component-1' OR [mgnl:template] = 'myModule:my/component-2')) OR not(ischildnode('/migros/de') AND [mgnl:template] = 'myModule:my/other/component'))")
        );
    }

    @Test
    public void selectOrderedByScore() {
        assertThat(Sql2.Statement.selectAll().whereAll(
            Sql2.Condition.String.property("title").lowerCase().likeAny().values("test", "toast")
        ).orderByScore().descending().build(),
            is("SELECT * FROM [nt:base] WHERE (lower([title]) LIKE '%test%' OR lower([title]) LIKE '%toast%') ORDER BY [jcr:score] DESC")
        );
    }

    @Test
    public void selectUuidsFromPagesOrdered() {
        assertThat(Sql2.Statement.selectAttributes("title", "date", "jcr:uuid").from(NodeTypes.Page.NAME).whereAll(
            Sql2.Condition.String.property("title").startsWithAny().values("Test", "Toast")
        ).orderBy("date").ascending().build(),
            is("SELECT [title],[date],[jcr:uuid] FROM [mgnl:page] WHERE ([title] LIKE 'Test%' OR [title] LIKE 'Toast%') ORDER BY [date] ASC")
        );
    }

    // Joins only with row queries, because: javax.jcr.RepositoryException: This query result contains more than one selector
    @Test
    public void selectChildrenOfParentWithTemplate() {
        assertThat(Sql2.Statement.selectAll().from(NodeTypes.Component.NAME).selectAs("teaser")
            .innerJoin(NodeTypes.Page.NAME).joinAs("page").on(
                Sql2.JoinOn.selectedDescendantOfJoined()
            )
            .whereAll(
                Sql2.Condition.template("m5-tk-core-components:components/content/standardImageTextTeaser"),
                Sql2.Condition.template("m5-relaunch-my-migros:pages/myCumulusPage").forJoin()
            ).build(),
            is("SELECT teaser.*,page.* FROM [mgnl:component] AS teaser INNER JOIN [mgnl:page] AS page ON isdescendantnode(page,teaser) WHERE (teaser.[mgnl:template] = 'm5-tk-core-components:components/content/standardImageTextTeaser' AND page.[mgnl:template] = 'm5-relaunch-my-migros:pages/myCumulusPage')")
        );
    }

    private Calendar getCalendar(int year, int month, int date, int hour, int minute, int second) {
        Calendar result = Calendar.getInstance();
        result.set(year, month, date, hour, minute, second);
        result.set(Calendar.MILLISECOND, 0);
        return result;
    }

}
