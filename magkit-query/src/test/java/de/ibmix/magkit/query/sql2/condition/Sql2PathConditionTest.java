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

import org.junit.After;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Tests for Sql2PathCondition.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-01
 */
public class Sql2PathConditionTest {

    @After
    public void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void isTest() {
        assertThat(Sql2PathCondition.is().asString(), is(""));
    }

    @Test
    public void not() {
        assertThat(Sql2PathCondition.is().not().asString(), is(""));
        assertThat(Sql2PathCondition.is().not().descendant("").asString(), is(""));
        assertThat(Sql2PathCondition.is().not().descendant("  ").asString(), is(""));
        assertThat(Sql2PathCondition.is().not().descendant("test/path").asString(), is("not(isdescendantnode('/test/path'))"));
    }

    @Test
    public void descendant() throws RepositoryException {
        assertThat(Sql2PathCondition.is().descendant("").asString(), is(""));
        assertThat(Sql2PathCondition.is().descendant("  ").asString(), is(""));
        assertThat(Sql2PathCondition.is().descendant("/test ").asString(), is("isdescendantnode('/test')"));
        assertThat(Sql2PathCondition.is().descendant("test/path").asString(), is("isdescendantnode('/test/path')"));
        assertThat(Sql2PathCondition.is().descendant(" test/path ").asString(), is("isdescendantnode('/test/path')"));

        Node node = mockNode("test/path");
        assertThat(Sql2PathCondition.is().descendant(node).asString(), is("isdescendantnode('/test/path')"));
    }

    @Test
    public void same() throws RepositoryException {
        assertThat(Sql2PathCondition.is().same("").asString(), is(""));
        assertThat(Sql2PathCondition.is().same("  ").asString(), is(""));
        assertThat(Sql2PathCondition.is().same("/test ").asString(), is("issamenode('/test')"));
        assertThat(Sql2PathCondition.is().same("test/path").asString(), is("issamenode('/test/path')"));
        assertThat(Sql2PathCondition.is().same(" test/path ").asString(), is("issamenode('/test/path')"));

        Node node = mockNode("test/path");
        assertThat(Sql2PathCondition.is().same(node).asString(), is("issamenode('/test/path')"));
    }

    @Test
    public void child() throws RepositoryException {
        assertThat(Sql2PathCondition.is().child("").asString(), is(""));
        assertThat(Sql2PathCondition.is().child("  ").asString(), is(""));
        assertThat(Sql2PathCondition.is().child("/test ").asString(), is("ischildnode('/test')"));
        assertThat(Sql2PathCondition.is().child("test/path").asString(), is("ischildnode('/test/path')"));
        assertThat(Sql2PathCondition.is().child(" test/path ").asString(), is("ischildnode('/test/path')"));

        Node node = mockNode("test/path");
        assertThat(Sql2PathCondition.is().child(node).asString(), is("ischildnode('/test/path')"));
    }
}
