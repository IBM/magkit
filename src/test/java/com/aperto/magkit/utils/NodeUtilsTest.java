package com.aperto.magkit.utils;

import info.magnolia.test.mock.jcr.MockSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static com.aperto.magkit.mockito.WebContextStubbingOperation.stubJcrSession;
import static com.aperto.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubType;
import static com.aperto.magkit.utils.NodeUtils.*;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static info.magnolia.test.mock.jcr.SessionTestUtil.createSession;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;

/**
 * Tests for node utils.
 *
 * @author frank.sommer
 * @since 26.05.14
 */
public class NodeUtilsTest {

    public static final String NODE_TYPE = "test:type";

    @Test
    public void testSenselessComponentChecks() throws Exception {
        assertThat(hasSubComponents(getNodeByIdentifier("0"), null), is(false));
        assertThat(hasSubComponents(getNodeByIdentifier("0"), null), is(false));
        assertThat(hasSubComponents(getNodeByIdentifier("0"), ""), is(false));
        assertThat(hasSubComponents(getNodeByIdentifier("0"), " "), is(false));
        assertThat(hasSubComponents(getNodeByIdentifier("0"), "area"), is(false));
    }

    @Test
    public void testComponentCheckWithEmptyArea() throws Exception {
        assertThat(hasSubComponents(getNodeByIdentifier("1"), "area"), is(false));
    }

    @Test
    public void testComponentCheckWithAnySubNode() throws Exception {
        assertThat(hasSubComponents(getNodeByIdentifier("2"), "area"), is(false));
    }

    @Test
    public void testComponentCheckWithOneSubComponent() throws Exception {
        assertThat(hasSubComponents(getNodeByIdentifier("3"), "area"), is(true));
    }

    @Test
    public void testPathForIdentifier() {
        assertThat(getPathForIdentifier(null, null), nullValue());
        assertThat(getPathForIdentifier("", "1"), nullValue());
        assertThat(getPathForIdentifier("any", ""), nullValue());
        assertThat(getPathForIdentifier("any", "x"), nullValue());
        assertThat(getPathForIdentifier(WEBSITE, "x"), nullValue());
        assertThat(getPathForIdentifier(WEBSITE, "1"), equalTo("/one"));
    }

    @Test
    public void testIsNodeType() throws RepositoryException {
        Node testNode = mockNode(stubType(NODE_TYPE));
        assertTrue(isNodeType(testNode, NODE_TYPE));
        assertFalse(isNodeType(testNode, "otherType"));
        assertFalse(isNodeType(testNode, null));
        assertFalse(isNodeType(null, NODE_TYPE));
        assertFalse(isNodeType(null, null));
    }

    @Before
    public void setUp() throws Exception {
        MockSession session = createSession(WEBSITE, getClass().getResourceAsStream("componentCheck.properties"));
        mockWebContext(stubJcrSession(WEBSITE, session));
    }

    @After
    public void tearDown() throws Exception {
        cleanContext();
    }
}
