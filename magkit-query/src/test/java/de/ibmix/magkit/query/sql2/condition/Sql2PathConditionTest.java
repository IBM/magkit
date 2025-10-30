package de.ibmix.magkit.query.sql2.condition;

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Sql2PathCondition.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-01
 */
public class Sql2PathConditionTest {

    @AfterEach
    public void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void isTest() {
        assertEquals("", Sql2PathCondition.is().asString());
    }

    @Test
    public void not() {
        assertEquals("", Sql2PathCondition.is().not().asString());
        assertEquals("", Sql2PathCondition.is().not().descendant("").asString());
        assertEquals("", Sql2PathCondition.is().not().descendant("  ").asString());
        assertEquals("not(isdescendantnode('/test/path'))", Sql2PathCondition.is().not().descendant("test/path").asString());
    }

    @Test
    public void descendant() throws RepositoryException {
        assertEquals("", Sql2PathCondition.is().descendant("").asString());
        assertEquals("", Sql2PathCondition.is().descendant("  ").asString());
        assertEquals("isdescendantnode('/test')", Sql2PathCondition.is().descendant("/test ").asString());
        assertEquals("isdescendantnode('/test/path')", Sql2PathCondition.is().descendant("test/path").asString());
        assertEquals("isdescendantnode('/test/path')", Sql2PathCondition.is().descendant(" test/path ").asString());

        Node node = mockNode("test/path");
        assertEquals("isdescendantnode('/test/path')", Sql2PathCondition.is().descendant(node).asString());
    }

    @Test
    public void same() throws RepositoryException {
        assertEquals("", Sql2PathCondition.is().same("").asString());
        assertEquals("", Sql2PathCondition.is().same("  ").asString());
        assertEquals("issamenode('/test')", Sql2PathCondition.is().same("/test ").asString());
        assertEquals("issamenode('/test/path')", Sql2PathCondition.is().same("test/path").asString());
        assertEquals("issamenode('/test/path')", Sql2PathCondition.is().same(" test/path ").asString());

        Node node = mockNode("test/path");
        assertEquals("issamenode('/test/path')", Sql2PathCondition.is().same(node).asString());
    }

    @Test
    public void child() throws RepositoryException {
        assertEquals("", Sql2PathCondition.is().child("").asString());
        assertEquals("", Sql2PathCondition.is().child("  ").asString());
        assertEquals("ischildnode('/test')", Sql2PathCondition.is().child("/test ").asString());
        assertEquals("ischildnode('/test/path')", Sql2PathCondition.is().child("test/path").asString());
        assertEquals("ischildnode('/test/path')", Sql2PathCondition.is().child(" test/path ").asString());

        Node node = mockNode("test/path");
        assertEquals("ischildnode('/test/path')", Sql2PathCondition.is().child(node).asString());
    }

    @Test
    public void selectorsFromAndJoin() {
        String fromSelector = "fromSel";
        String joinSelector = "joinSel";
        assertEquals("ischildnode(fromSel, '/a/b')", Sql2PathCondition.is().child("a/b").asString(fromSelector, joinSelector));
        assertEquals("ischildnode(joinSel, '/a/b')", Sql2PathCondition.is().child("a/b").forJoin().asString(fromSelector, joinSelector));
    }

    @Test
    public void negatedWithJoinSelector() {
        String fromSelector = "fromSel";
        String joinSelector = "joinSel";
        assertEquals("not(issamenode(joinSel, '/a'))", Sql2PathCondition.is().not().same("a").forJoin().asString(fromSelector, joinSelector));
    }

    @Test
    public void isNotEmptyStates() {
        Sql2PathCondition empty = Sql2PathCondition.is();
        assertFalse(empty.isNotEmpty());
        assertFalse(Sql2PathCondition.is().not().isNotEmpty());
        assertTrue(Sql2PathCondition.is().child("a/b").isNotEmpty());
    }
}
