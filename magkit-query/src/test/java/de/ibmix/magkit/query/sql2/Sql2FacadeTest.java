package de.ibmix.magkit.query.sql2;

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

import de.ibmix.magkit.query.sql2.condition.Sql2JoinConstraint;
import de.ibmix.magkit.query.sql2.statement.Sql2SelectorNames;
import info.magnolia.jcr.util.NodeTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQueryResult;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static javax.jcr.query.Query.JCR_SQL2;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Sql2 facade covering high level query shortcuts and convenience condition builders.
 * Verifies statement fragments and execution behavior for representative scenarios.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-29
 */
public class Sql2FacadeTest {

    @BeforeEach
    public void setUp() throws Exception {
        mockWebContext(stubJcrSession(WEBSITE));
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }

    /**
     * nodesByIdentifiers returns empty list when ids array is null or empty.
     */
    @Test
    public void nodesByIdentifiersEmpty() {
        List<Node> resultNull = Sql2.Query.nodesByIdentifiers(WEBSITE, (String[]) null);
        List<Node> resultEmpty = Sql2.Query.nodesByIdentifiers(WEBSITE, new String[0]);
        assertTrue(resultNull.isEmpty());
        assertTrue(resultEmpty.isEmpty());
    }

    /**
     * nodesByIdentifiers executes a query matching all given identifiers.
     */
    @Test
    public void nodesByIdentifiers() throws Exception {
        Node n1 = mockNode(WEBSITE, "/root/a", null);
        Node n2 = mockNode(WEBSITE, "/root/b", null);
        String expected = "SELECT * FROM [nt:base] WHERE ([jcr:uuid] = 'id-1' OR [jcr:uuid] = 'id-2')";
        mockQueryResult(WEBSITE, JCR_SQL2, expected, n1, n2);
        List<Node> result = Sql2.Query.nodesByIdentifiers(WEBSITE, "id-1", "id-2");
        assertEquals(2, result.size());
        assertEquals(n1, result.get(0));
        assertEquals(n2, result.get(1));
    }

    /**
     * nodesByTemplates returns empty list for empty template array.
     */
    @Test
    public void nodesByTemplatesEmpty() {
        List<Node> result = Sql2.Query.nodesByTemplates("/root");
        assertTrue(result.isEmpty());
    }

    /**
     * nodesByTemplates executes combined descendant and template constraints.
     */
    @Test
    public void nodesByTemplates() throws Exception {
        Node n1 = mockNode(WEBSITE, "/root/x");
        String expected = "SELECT * FROM [nt:base] WHERE (isdescendantnode('/root') AND ([mgnl:template] = 'tpl:one' OR [mgnl:template] = 'tpl:two'))";
        mockQueryResult(WEBSITE, JCR_SQL2, expected, n1);
        List<Node> result = Sql2.Query.nodesByTemplates("/root", "tpl:one", "tpl:two");
        assertEquals(1, result.size());
        assertEquals(n1, result.get(0));
    }

    /**
     * nodesFrom shortcut without conditions builds a simple select for given node type.
     */
    @Test
    public void nodesFromWithoutConditions() throws Exception {
        Node n1 = mockNode(WEBSITE, "/root/page");
        String expected = "SELECT * FROM [mgnl:page]";
        mockQueryResult(WEBSITE, JCR_SQL2, expected, n1);
        List<Node> result = Sql2.Query.nodesFrom(WEBSITE, NodeTypes.Page.NAME);
        assertEquals(1, result.size());
        assertEquals(n1, result.get(0));
    }

    /**
     * nodesFrom with condition applies whereAll group.
     */
    @Test
    public void nodesFromWithCondition() throws Exception {
        Node n1 = mockNode(WEBSITE, "/root/component");
        String expected = "SELECT * FROM [mgnl:component] WHERE [title] = 'Test'";
        Sql2JoinConstraint constraint = Sql2.Condition.String.property("title").equalsAny().values("Test");
        mockQueryResult(WEBSITE, JCR_SQL2, expected, n1);
        List<Node> result = Sql2.Query.nodesFrom(WEBSITE, NodeTypes.Component.NAME, constraint);
        assertEquals(1, result.size());
        assertEquals(n1, result.get(0));
    }

    /**
     * nameEquals convenience condition with multiple names.
     */
    @Test
    public void nameEqualsMultiple() {
        String statement = Sql2.Statement.select().whereAll(Sql2.Condition.nameEquals("home", "about")).build();
        assertEquals("SELECT * FROM [nt:base] WHERE (name() = 'home' OR name() = 'about')", statement);
    }

    /**
     * Date created before and after convenience methods.
     */
    @Test
    public void dateCreatedBeforeAfter() {
        Calendar cal = calendar(2020, Calendar.JANUARY, 1, 0, 0, 0);
        String before = Sql2.Statement.select().whereAll(Sql2.Condition.Date.createdBefore(cal)).build();
        String after = Sql2.Statement.select().whereAll(Sql2.Condition.Date.createdAfter(cal)).build();
        assertTrue(before.contains("[mgnl:created] < cast('2020-01-01T00:00:00"));
        assertTrue(after.contains("[mgnl:created] > cast('2020-01-01T00:00:00"));
    }

    /**
     * Date lastActivated convenience methods.
     */
    @Test
    public void dateLastActivatedBeforeAfter() {
        Calendar cal = calendar(2021, Calendar.JUNE, 15, 12, 30, 0);
        String before = Sql2.Statement.select().whereAll(Sql2.Condition.Date.lastActivatedBefore(cal)).build();
        String after = Sql2.Statement.select().whereAll(Sql2.Condition.Date.lastActivatedAfter(cal)).build();
        assertTrue(before.contains("[mgnl:lastActivated] < cast('2021-06-15T12:30:00"));
        assertTrue(after.contains("[mgnl:lastActivated] > cast('2021-06-15T12:30:00"));
    }

    /**
     * Date lastModified convenience methods.
     */
    @Test
    public void dateLastModifiedBeforeAfter() {
        Calendar cal = calendar(2022, Calendar.DECEMBER, 31, 23, 59, 59);
        String before = Sql2.Statement.select().whereAll(Sql2.Condition.Date.lastModifiedBefore(cal)).build();
        String after = Sql2.Statement.select().whereAll(Sql2.Condition.Date.lastModifiedAfter(cal)).build();
        assertTrue(before.contains("[mgnl:lastModified] < cast('2022-12-31T23:59:59"));
        assertTrue(after.contains("[mgnl:lastModified] > cast('2022-12-31T23:59:59"));
    }

    /**
     * Date deleted convenience methods.
     */
    @Test
    public void dateDeletedBeforeAfter() {
        Calendar cal = calendar(2023, Calendar.APRIL, 10, 10, 0, 0);
        String before = Sql2.Statement.select().whereAll(Sql2.Condition.Date.deletedBefore(cal)).build();
        String after = Sql2.Statement.select().whereAll(Sql2.Condition.Date.deletedAfter(cal)).build();
        assertTrue(before.contains("[mgnl:deleted] < cast('2023-04-10T10:00:00"));
        assertTrue(after.contains("[mgnl:deleted] > cast('2023-04-10T10:00:00"));
    }

    /**
     * Double propertyBetween convenience method.
     */
    @Test
    public void doublePropertyBetween() {
        String statement = Sql2.Statement.select().whereAll(Sql2.Condition.Double.propertyBetween("rating", 1.0D, 2.0D)).build();
        assertEquals("SELECT * FROM [nt:base] WHERE ([rating] >= 1.0 AND [rating] < 2.0)", statement);
    }

    /**
     * Long propertyBetween convenience method.
     */
    @Test
    public void longPropertyBetween() {
        String statement = Sql2.Statement.select().whereAll(Sql2.Condition.Long.propertyBetween("count", 10L, 20L)).build();
        assertEquals("SELECT * FROM [nt:base] WHERE ([count] >= 10 AND [count] < 20)", statement);
    }

    /**
     * Full text containsAll / containsAny convenience methods.
     */
    @Test
    public void fullTextContainsAllAny() {
        String all = Sql2.Statement.select().as("s").whereAll(Sql2.Condition.FullText.containsAll("alpha", "beta")).build();
        String any = Sql2.Statement.select().as("s").whereAll(Sql2.Condition.FullText.containsAny("alpha", "beta")).build();
        assertEquals("SELECT * FROM [nt:base] AS s WHERE contains(s.*, 'alpha beta')", all);
        assertEquals("SELECT * FROM [nt:base] AS s WHERE contains(s.*, 'alpha OR beta')", any);
    }

    /**
     * Full text propertyContainsAll / propertyContainsAny convenience methods.
     */
    @Test
    public void fullTextPropertyContainsAllAny() {
        String all = Sql2.Statement.select().as("s").whereAll(Sql2.Condition.FullText.propertyContainsAll("title", "hello", "world")).build();
        String any = Sql2.Statement.select().as("s").whereAll(Sql2.Condition.FullText.propertyContainsAny("title", "hello", "world")).build();
        assertEquals("SELECT * FROM [nt:base] AS s WHERE contains(s.title, 'hello world')", all);
        assertEquals("SELECT * FROM [nt:base] AS s WHERE contains(s.title, 'hello OR world')", any);
    }

    /**
     * Full text condition without selector name should throw IllegalStateException.
     */
    @Test
    public void fullTextMissingSelector() {
        assertThrows(IllegalStateException.class, () -> Sql2.Statement.select().whereAll(Sql2.Condition.FullText.containsAny("x")).build());
    }

    /**
     * JoinOn child relation produces expected ON clause.
     */
    @Test
    public void joinOnChildRelation() {
        String statement = Sql2.Statement.select("title").from(NodeTypes.Component.NAME).as("c")
            .innerJoin(NodeTypes.Page.NAME).joinAs("p").on(Sql2.JoinOn.joinedChildOfSelected()).build();
        assertEquals("SELECT c.[title],p.* FROM [mgnl:component] AS c INNER JOIN [mgnl:page] AS p ON ischildnode(p,c)", statement);
    }

    /**
     * JoinOn descendant and equals relations.
     */
    @Test
    public void joinOnDescendantAndEqualsRelations() {
        String descendant = Sql2.Statement.select().from(NodeTypes.Component.NAME).as("c")
            .leftOuterJoin(NodeTypes.Page.NAME).joinAs("p").on(Sql2.JoinOn.joinedDescendantOfSelected()).build();
        String equals = Sql2.Statement.select().from(NodeTypes.Component.NAME).as("c")
            .rightOuterJoin(NodeTypes.Page.NAME).joinAs("p").on(Sql2.JoinOn.joinedEqualsSelected()).build();
        assertEquals("SELECT c.*,p.* FROM [mgnl:component] AS c LEFT OUTER JOIN [mgnl:page] AS p ON isdescendantnode(p,c)", descendant);
        assertEquals("SELECT c.*,p.* FROM [mgnl:component] AS c RIGHT OUTER JOIN [mgnl:page] AS p ON issamenode(c,p)", equals);
    }

    /**
     * Path child and descendant conditions accepting Node parameter.
     */
    @Test
    public void pathChildAndDescendantWithNode() throws Exception {
        Node parent = mockNode(WEBSITE, "/root/parent");
        Node ancestor = mockNode(WEBSITE, "/root/ancestor");
        String statement = Sql2.Statement.select().whereAll(
            Sql2.Condition.Path.isChild(parent),
            Sql2.Condition.Path.isDescendant(ancestor)
        ).build();
        assertEquals("SELECT * FROM [nt:base] WHERE (ischildnode('/root/parent') AND isdescendantnode('/root/ancestor'))", statement);
    }

    /**
     * String templateEquals and identifierEquals convenience conditions.
     */
    @Test
    public void stringTemplateAndIdentifierEquals() {
        String templateStatement = Sql2.Statement.select().whereAll(Sql2.Condition.String.templateEquals("module:tpl")).build();
        String identifierStatement = Sql2.Statement.select().whereAll(Sql2.Condition.String.identifierEquals("uuid-123")).build();
        assertEquals("SELECT * FROM [nt:base] WHERE [mgnl:template] = 'module:tpl'", templateStatement);
        assertEquals("SELECT * FROM [nt:base] WHERE [jcr:uuid] = 'uuid-123'", identifierStatement);
    }

    @Test
    public void statementSelectContentNodes() {
        String statement = Sql2.Statement.selectContentNodes().build();
        assertEquals("SELECT * FROM [mgnl:contentNode]", statement);

        statement = Sql2.Statement.selectContentNodes("some", "other").build();
        assertEquals("SELECT [some],[other] FROM [mgnl:contentNode]", statement);
    }

    @Test
    public void statementSelectContents() {
        String statement = Sql2.Statement.selectContents().build();
        assertEquals("SELECT * FROM [mgnl:content]", statement);

        statement = Sql2.Statement.selectContents("some", "other").build();
        assertEquals("SELECT [some],[other] FROM [mgnl:content]", statement);
    }

    @Test
    public void statementSelectPages() {
        String statement = Sql2.Statement.selectPages().build();
        assertEquals("SELECT * FROM [mgnl:page]", statement);

        statement = Sql2.Statement.selectPages("some", "other").build();
        assertEquals("SELECT [some],[other] FROM [mgnl:page]", statement);
    }

    @Test
    public void statementSelectAreas() {
        String statement = Sql2.Statement.selectAreas().build();
        assertEquals("SELECT * FROM [mgnl:area]", statement);

        statement = Sql2.Statement.selectAreas("some", "other").build();
        assertEquals("SELECT [some],[other] FROM [mgnl:area]", statement);
    }

    @Test
    public void conditionOr() {
        String condition = Sql2.Condition.or().asString();
        assertEquals("", condition);

        condition = Sql2.Condition.or().matches(Sql2.Condition.String.propertyEquals("test", "success"), Sql2.Condition.String.propertyEquals("other", "something", "else")).asString();
        assertEquals("([test] = 'success' OR ([other] = 'something' OR [other] = 'else'))", condition);
    }

    @Test
    public void joinOnSelectedChiledOfJoined() {
        StringBuilder statement = new StringBuilder();
        Sql2.JoinOn.selectedChildOfJoined().appendTo(statement, new Sql2SelectorNames() {
            @Override
            public String getFromSelectorName() {
                return "from";
            }

            @Override
            public String getJoinSelectorName() {
                return "join";
            }
        });
        assertEquals("ischildnode(from,join)", statement.toString());
    }

    private Calendar calendar(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
}
