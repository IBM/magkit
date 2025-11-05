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

import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Sql2.Condition methods.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-02-01
 */
public class Sql2ConditionTest {

    @Test
    public void stringIdentifier() {
        assertEquals("", Sql2.Condition.String.identifierEquals(null).asString());
        assertEquals("", Sql2.Condition.String.identifierEquals().asString());
        assertEquals("[jcr:uuid] = '123'", Sql2.Condition.String.identifierEquals("123").asString());
        assertEquals("([jcr:uuid] = 'test' OR [jcr:uuid] = 'test-again')", Sql2.Condition.String.identifierEquals("test", "test-again").asString());
    }

    @Test
    public void stringTemplate() {
        assertEquals("", Sql2.Condition.String.templateEquals(null).asString());
        assertEquals("", Sql2.Condition.String.templateEquals().asString());
        assertEquals("[mgnl:template] = 'tpl'", Sql2.Condition.String.templateEquals("tpl").asString());
        assertEquals("([mgnl:template] = 'test' OR [mgnl:template] = 'other')", Sql2.Condition.String.templateEquals("test", "other").asString());
    }

    @Test
    public void dateCreatedBefore() {
        assertEquals("", Sql2.Condition.Date.createdBefore(null).asString());
        assertTrue(Sql2.Condition.Date.createdBefore(getDateZero()).asString().startsWith("[mgnl:created] < cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.createdBefore(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateCreatedAfter() {
        assertEquals("", Sql2.Condition.Date.createdAfter(null).asString());
        assertTrue(Sql2.Condition.Date.createdAfter(getDateZero()).asString().startsWith("[mgnl:created] > cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.createdAfter(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateLastActivatedBefore() {
        assertEquals("", Sql2.Condition.Date.lastActivatedBefore(null).asString());
        assertTrue(Sql2.Condition.Date.lastActivatedBefore(getDateZero()).asString().startsWith("[mgnl:lastActivated] < cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.lastActivatedBefore(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateLastActivatedAfter() {
        assertEquals("", Sql2.Condition.Date.lastActivatedAfter(null).asString());
        assertTrue(Sql2.Condition.Date.lastActivatedAfter(getDateZero()).asString().startsWith("[mgnl:lastActivated] > cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.lastActivatedAfter(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateLastModifiedBefore() {
        assertEquals("", Sql2.Condition.Date.lastModifiedBefore(null).asString());
        assertTrue(Sql2.Condition.Date.lastModifiedBefore(getDateZero()).asString().startsWith("[mgnl:lastModified] < cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.lastModifiedBefore(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateLastModifiedAfter() {
        assertEquals("", Sql2.Condition.Date.lastModifiedAfter(null).asString());
        assertTrue(Sql2.Condition.Date.lastModifiedAfter(getDateZero()).asString().startsWith("[mgnl:lastModified] > cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.lastModifiedAfter(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateDeletedBefore() {
        assertEquals("", Sql2.Condition.Date.deletedBefore(null).asString());
        assertTrue(Sql2.Condition.Date.deletedBefore(getDateZero()).asString().startsWith("[mgnl:deleted] < cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.deletedBefore(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateDeletedAfter() {
        assertEquals("", Sql2.Condition.Date.deletedAfter(null).asString());
        assertTrue(Sql2.Condition.Date.deletedAfter(getDateZero()).asString().startsWith("[mgnl:deleted] > cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.deletedAfter(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void longPropertyLowerThan() {
        assertEquals("[test] < 0", Sql2.Condition.Long.propertyLowerThan("test", 0L).asString());
    }

    @Test
    public void longPropertyEqualAny() {
        assertEquals("[test] = 0", Sql2.Condition.Long.propertyEqualsAny("test", 0L).asString());
        assertEquals("([test] = 0 OR [test] = 1)", Sql2.Condition.Long.propertyEqualsAny("test", 0L, 1L).asString());
    }

    @Test
    public void longPropertyGraterOrEqualsThan() {
        assertEquals("[test] >= 0", Sql2.Condition.Long.propertyGraterOrEqualThan("test", 0L).asString());
    }

    @Test
    public void longPropertyGraterThan() {
        assertEquals("[test] > 0", Sql2.Condition.Long.propertyGraterThan("test", 0L).asString());
    }

    @Test
    public void longPropertyBetween() {
        assertEquals("([test] >= 0 AND [test] < 10)", Sql2.Condition.Long.propertyBetween("test", 0L, 10L).asString());
    }

    @Test
    public void doublePropertyLowerThan() {
        assertEquals("[test] < 0.0", Sql2.Condition.Double.propertyLowerThan("test", 0D).asString());
    }

    @Test
    public void doublePropertyEqualAny() {
        assertEquals("[test] = 0.0", Sql2.Condition.Double.propertyEqualsAny("test", 0D).asString());
        assertEquals("([test] = 0.0 OR [test] = 1.0)", Sql2.Condition.Double.propertyEqualsAny("test", 0D, 1D).asString());
    }

    @Test
    public void doublePropertyGraterOrEqualsThan() {
        assertEquals("[test] >= 0.0", Sql2.Condition.Double.propertyGraterOrEqualThan("test", 0D).asString());
    }

    @Test
    public void doublePropertyGraterThan() {
        assertEquals("[test] > 0.0", Sql2.Condition.Double.propertyGraterThan("test", 0D).asString());
    }

    @Test
    public void doublePropertyBetween() {
        assertEquals("([test] >= 0.0 AND [test] < 10.0)", Sql2.Condition.Double.propertyBetween("test", 0D, 10D).asString());
    }

    @Test
    public void pathIsChild() throws RepositoryException {
        assertEquals("", Sql2.Condition.Path.isChild((String) null).asString());
        assertEquals("", Sql2.Condition.Path.isChild("").asString());
        assertEquals("ischildnode('/some/path')", Sql2.Condition.Path.isChild("/some/path").asString());

        Node node = null;
        assertEquals("", Sql2.Condition.Path.isChild(node).asString());
        node = mockNode("test/path");
        assertEquals("ischildnode('/test/path')", Sql2.Condition.Path.isChild(node).asString());
    }

    @Test
    public void pathIsDescendant() throws RepositoryException {
        assertEquals("", Sql2.Condition.Path.isDescendant((String) null).asString());
        assertEquals("", Sql2.Condition.Path.isDescendant("").asString());
        assertEquals("isdescendantnode('/some/path')", Sql2.Condition.Path.isDescendant("/some/path").asString());

        Node node = null;
        assertEquals("", Sql2.Condition.Path.isDescendant(node).asString());
        node = mockNode("test/path");
        assertEquals("isdescendantnode('/test/path')", Sql2.Condition.Path.isDescendant(node).asString());
    }

    @Test
    public void fullTextContainsAll() {
        assertEquals("", Sql2.Condition.FullText.containsAll().asString("from", "join"));
        assertEquals("", Sql2.Condition.FullText.containsAll((String[]) null).asString("from", "join"));
        assertEquals("contains(from.*, 'test')", Sql2.Condition.FullText.containsAll("test").asString("from", "join"));
        assertEquals("contains(from.*, 'test other')", Sql2.Condition.FullText.containsAll("test", "other", "", "  ").asString("from", "join"));
    }

    @Test
    public void fullTextContainsAny() {
        assertEquals("", Sql2.Condition.FullText.containsAny().asString("from", "join"));
        assertEquals("", Sql2.Condition.FullText.containsAny((String[]) null).asString("from", "join"));
        assertEquals("contains(from.*, 'test')", Sql2.Condition.FullText.containsAny("test").asString("from", "join"));
        assertEquals("contains(from.*, 'test OR other')", Sql2.Condition.FullText.containsAny("test", "other", "", "  ").asString("from", "join"));
    }

    @Test
    public void fullTextPropertyContainsAll() {
        assertEquals("", Sql2.Condition.FullText.propertyContainsAll("test").asString("from", "join"));
        assertEquals("", Sql2.Condition.FullText.propertyContainsAll("test", (String[]) null).asString("from", "join"));
        assertEquals("contains(from.test, 'value')", Sql2.Condition.FullText.propertyContainsAll("test", "value").asString("from", "join"));
        assertEquals("contains(from.test, 'value other')", Sql2.Condition.FullText.propertyContainsAll("test", "value", "other", "", "  ").asString("from", "join"));
    }

    @Test
    public void fullTextPropertyContainsAny() {
        assertEquals("", Sql2.Condition.FullText.propertyContainsAny("test").asString("from", "join"));
        assertEquals("", Sql2.Condition.FullText.propertyContainsAny("test", (String[]) null).asString("from", "join"));
        assertEquals("contains(from.test, 'value')", Sql2.Condition.FullText.propertyContainsAny("test", "value").asString("from", "join"));
        assertEquals("contains(from.test, 'value OR other')", Sql2.Condition.FullText.propertyContainsAny("test", "value", "other", "", "  ").asString("from", "join"));
    }

    private Calendar getDateZero() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        return cal;
    }
}
