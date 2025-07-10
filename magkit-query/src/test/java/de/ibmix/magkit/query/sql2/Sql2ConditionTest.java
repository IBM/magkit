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

import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

/**
 * Test Sql2.Condition methods.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-02-01
 */
public class Sql2ConditionTest {

    @Test
    public void stringIdentifier() {
        assertThat(Sql2.Condition.String.identifierEquals((String) null).asString(), is(""));
        assertThat(Sql2.Condition.String.identifierEquals().asString(), is(""));
        assertThat(Sql2.Condition.String.identifierEquals("123").asString(), is("[jcr:uuid] = '123'"));
        assertThat(Sql2.Condition.String.identifierEquals("test", "test-again").asString(), is("([jcr:uuid] = 'test' OR [jcr:uuid] = 'test-again')"));
    }

    @Test
    public void stringTemplate() {
        assertThat(Sql2.Condition.String.templateEquals((String) null).asString(), is(""));
        assertThat(Sql2.Condition.String.templateEquals().asString(), is(""));
        assertThat(Sql2.Condition.String.templateEquals("tpl").asString(), is("[mgnl:template] = 'tpl'"));
        assertThat(Sql2.Condition.String.templateEquals("test", "other").asString(), is("([mgnl:template] = 'test' OR [mgnl:template] = 'other')"));
    }

    @Test
    public void dateCreatedBefore() {
        assertThat(Sql2.Condition.Date.createdBefore(null).asString(), is(""));
        assertTrue(Sql2.Condition.Date.createdBefore(getDateZero()).asString().startsWith("[mgnl:created] < cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.createdBefore(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateCreatedAfter() {
        assertThat(Sql2.Condition.Date.createdAfter(null).asString(), is(""));
        assertTrue(Sql2.Condition.Date.createdAfter(getDateZero()).asString().startsWith("[mgnl:created] > cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.createdAfter(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateLastActivatedBefore() {
        assertThat(Sql2.Condition.Date.lastActivatedBefore(null).asString(), is(""));
        assertTrue(Sql2.Condition.Date.lastActivatedBefore(getDateZero()).asString().startsWith("[mgnl:lastActivated] < cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.lastActivatedBefore(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateLastActivatedAfter() {
        assertThat(Sql2.Condition.Date.lastActivatedAfter(null).asString(), is(""));
        assertTrue(Sql2.Condition.Date.lastActivatedAfter(getDateZero()).asString().startsWith("[mgnl:lastActivated] > cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.lastActivatedAfter(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateLastModifiedBefore() {
        assertThat(Sql2.Condition.Date.lastModifiedBefore(null).asString(), is(""));
        assertTrue(Sql2.Condition.Date.lastModifiedBefore(getDateZero()).asString().startsWith("[mgnl:lastModified] < cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.lastModifiedBefore(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateLastModifiedAfter() {
        assertThat(Sql2.Condition.Date.lastModifiedAfter(null).asString(), is(""));
        assertTrue(Sql2.Condition.Date.lastModifiedAfter(getDateZero()).asString().startsWith("[mgnl:lastModified] > cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.lastModifiedAfter(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateDeletedBefore() {
        assertThat(Sql2.Condition.Date.deletedBefore(null).asString(), is(""));
        assertTrue(Sql2.Condition.Date.deletedBefore(getDateZero()).asString().startsWith("[mgnl:deleted] < cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.deletedBefore(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void dateDeletedAfter() {
        assertThat(Sql2.Condition.Date.deletedAfter(null).asString(), is(""));
        assertTrue(Sql2.Condition.Date.deletedAfter(getDateZero()).asString().startsWith("[mgnl:deleted] > cast('1970-01-01"));
        assertTrue(Sql2.Condition.Date.deletedAfter(getDateZero()).asString().endsWith("' as date)"));
    }

    @Test
    public void longPropertyLowerThan() {
        assertThat(Sql2.Condition.Long.propertyLowerThan("test", 0L).asString(), is("[test] < 0"));
    }

    @Test
    public void longPropertyEqualAny() {
        assertThat(Sql2.Condition.Long.propertyEqualsAny("test", 0L).asString(), is("[test] = 0"));
        assertThat(Sql2.Condition.Long.propertyEqualsAny("test", 0L, 1L).asString(), is("([test] = 0 OR [test] = 1)"));
    }

    @Test
    public void longPropertyGraterOrEqualsThan() {
        assertThat(Sql2.Condition.Long.propertyGraterOrEqualThan("test", 0L).asString(), is("[test] >= 0"));
    }

    @Test
    public void longPropertyGraterThan() {
        assertThat(Sql2.Condition.Long.propertyGraterThan("test", 0L).asString(), is("[test] > 0"));
    }

    @Test
    public void longPropertyBetween() {
        assertThat(Sql2.Condition.Long.propertyBetween("test", 0L, 10L).asString(), is("([test] >= 0 AND [test] < 10)"));
    }

    @Test
    public void doublePropertyLowerThan() {
        assertThat(Sql2.Condition.Double.propertyLowerThan("test", 0D).asString(), is("[test] < 0.0"));
    }

    @Test
    public void doublePropertyEqualAny() {
        assertThat(Sql2.Condition.Double.propertyEqualsAny("test", 0D).asString(), is("[test] = 0.0"));
        assertThat(Sql2.Condition.Double.propertyEqualsAny("test", 0D, 1D).asString(), is("([test] = 0.0 OR [test] = 1.0)"));
    }

    @Test
    public void doublePropertyGraterOrEqualsThan() {
        assertThat(Sql2.Condition.Double.propertyGraterOrEqualThan("test", 0D).asString(), is("[test] >= 0.0"));
    }

    @Test
    public void doublePropertyGraterThan() {
        assertThat(Sql2.Condition.Double.propertyGraterThan("test", 0D).asString(), is("[test] > 0.0"));
    }

    @Test
    public void doublePropertyBetween() {
        assertThat(Sql2.Condition.Double.propertyBetween("test", 0D, 10D).asString(), is("([test] >= 0.0 AND [test] < 10.0)"));
    }

    @Test
    public void pathIsChild() throws RepositoryException {
        assertThat(Sql2.Condition.Path.isChild((String) null).asString(), is(""));
        assertThat(Sql2.Condition.Path.isChild("").asString(), is(""));
        assertThat(Sql2.Condition.Path.isChild("/some/path").asString(), is("ischildnode('/some/path')"));

        Node node = null;
        assertThat(Sql2.Condition.Path.isChild(node).asString(), is(""));
        node = mockNode("test/path");
        assertThat(Sql2.Condition.Path.isChild(node).asString(), is("ischildnode('/test/path')"));
    }

    @Test
    public void pathIsDescendant() throws RepositoryException {
        assertThat(Sql2.Condition.Path.isDescendant((String) null).asString(), is(""));
        assertThat(Sql2.Condition.Path.isDescendant("").asString(), is(""));
        assertThat(Sql2.Condition.Path.isDescendant("/some/path").asString(), is("isdescendantnode('/some/path')"));

        Node node = null;
        assertThat(Sql2.Condition.Path.isDescendant(node).asString(), is(""));
        node = mockNode("test/path");
        assertThat(Sql2.Condition.Path.isDescendant(node).asString(), is("isdescendantnode('/test/path')"));
    }

    @Test
    public void fullTextContainsAll() {
        assertThat(Sql2.Condition.FullText.containsAll().asString("from", "join"), is(""));
        assertThat(Sql2.Condition.FullText.containsAll((String[]) null).asString("from", "join"), is(""));
        assertThat(Sql2.Condition.FullText.containsAll("test").asString("from", "join"), is("contains(from.*, 'test')"));
        assertThat(Sql2.Condition.FullText.containsAll("test", "other", "", "  ").asString("from", "join"), is("contains(from.*, 'test other')"));
    }

    @Test
    public void fullTextContainsAny() {
        assertThat(Sql2.Condition.FullText.containsAny().asString("from", "join"), is(""));
        assertThat(Sql2.Condition.FullText.containsAny((String[]) null).asString("from", "join"), is(""));
        assertThat(Sql2.Condition.FullText.containsAny("test").asString("from", "join"), is("contains(from.*, 'test')"));
        assertThat(Sql2.Condition.FullText.containsAny("test", "other", "", "  ").asString("from", "join"), is("contains(from.*, 'test OR other')"));
    }

    @Test
    public void fullTextPropertyContainsAll() {
        assertThat(Sql2.Condition.FullText.propertyContainsAll("test").asString("from", "join"), is(""));
        assertThat(Sql2.Condition.FullText.propertyContainsAll("test", (String[]) null).asString("from", "join"), is(""));
        assertThat(Sql2.Condition.FullText.propertyContainsAll("test", "value").asString("from", "join"), is("contains(from.test, 'value')"));
        assertThat(Sql2.Condition.FullText.propertyContainsAll("test", "value", "other", "", "  ").asString("from", "join"), is("contains(from.test, 'value other')"));
    }

    @Test
    public void fullTextPropertyContainsAny() {
        assertThat(Sql2.Condition.FullText.propertyContainsAny("test").asString("from", "join"), is(""));
        assertThat(Sql2.Condition.FullText.propertyContainsAny("test", (String[]) null).asString("from", "join"), is(""));
        assertThat(Sql2.Condition.FullText.propertyContainsAny("test", "value").asString("from", "join"), is("contains(from.test, 'value')"));
        assertThat(Sql2.Condition.FullText.propertyContainsAny("test", "value", "other", "", "  ").asString("from", "join"), is("contains(from.test, 'value OR other')"));
    }

    private Calendar getDateZero() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        return cal;
    }
}
