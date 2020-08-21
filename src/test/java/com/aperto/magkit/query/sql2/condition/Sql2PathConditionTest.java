package com.aperto.magkit.query.sql2.condition;

import com.aperto.magkit.mockito.ContextMockUtils;
import com.aperto.magkit.mockito.jcr.NodeMockUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test Sql2PathCondition.
 *
 * @author wolf.bubenik@aperto.com
 * @since 01.04.2020
 */
public class Sql2PathConditionTest {

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @After
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
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

        Node node = NodeMockUtils.mockNode("test/path");
        assertThat(Sql2PathCondition.is().descendant(node).asString(), is("isdescendantnode('/test/path')"));
    }

    @Test
    public void same() throws RepositoryException {
        assertThat(Sql2PathCondition.is().same("").asString(), is(""));
        assertThat(Sql2PathCondition.is().same("  ").asString(), is(""));
        assertThat(Sql2PathCondition.is().same("/test ").asString(), is("issamenode('/test')"));
        assertThat(Sql2PathCondition.is().same("test/path").asString(), is("issamenode('/test/path')"));
        assertThat(Sql2PathCondition.is().same(" test/path ").asString(), is("issamenode('/test/path')"));

        Node node = NodeMockUtils.mockNode("test/path");
        assertThat(Sql2PathCondition.is().same(node).asString(), is("issamenode('/test/path')"));
    }

    @Test
    public void child() throws RepositoryException {
        assertThat(Sql2PathCondition.is().child("").asString(), is(""));
        assertThat(Sql2PathCondition.is().child("  ").asString(), is(""));
        assertThat(Sql2PathCondition.is().child("/test ").asString(), is("ischildnode('/test')"));
        assertThat(Sql2PathCondition.is().child("test/path").asString(), is("ischildnode('/test/path')"));
        assertThat(Sql2PathCondition.is().child(" test/path ").asString(), is("ischildnode('/test/path')"));

        Node node = NodeMockUtils.mockNode("test/path");
        assertThat(Sql2PathCondition.is().child(node).asString(), is("ischildnode('/test/path')"));
    }
}