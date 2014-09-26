package com.aperto.magkit.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static com.aperto.magkit.mockito.WebContextStubbingOperation.stubJcrSession;
import static com.aperto.magkit.mockito.jcr.QueryManagerStubbingOperation.stubbQuery;
import static com.aperto.magkit.mockito.jcr.QueryMockUtils.mockQueryManager;
import static com.aperto.magkit.mockito.jcr.QueryStubbingOperation.stubbResult;
import static com.aperto.magkit.utils.NodeQueryUtil.getComponentsWithTemplate;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static javax.jcr.query.Query.XPATH;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author philipp.guettler
 * @since 27.09.13
 */
public class NodeQueryUtilTest {
    private static final String TEST_TPL_NAME = "test-case:pages/templateName";
    private static final String TEST_NODE_PATH = "/portal/news/node";
    private static final String TEST_TYPE = "test:type";
    private static final String TEST_CUSTOM_XPATH_EXPRESSION = "other constraints";
    private QueryManager _queryManager;

    @Test
    public void testReplaceStringBuilderWithXpathBuilder() throws Exception {
        final StringBuilder statement = new StringBuilder();
        statement.append("/jcr:root");
        statement.append(TEST_NODE_PATH);
        statement.append("//element(*," + TEST_TYPE + ")");
        statement.append("[@mgnl:template='").append(TEST_TPL_NAME).append("']");
        statement.append(TEST_CUSTOM_XPATH_EXPRESSION);

        final ConstraintBuilder builder = new ConstraintBuilder().addTplNameConstraint(TEST_TPL_NAME);
        assertEquals(statement.toString(), new XpathBuilder().path(TEST_NODE_PATH).type(TEST_TYPE).property(builder).append(TEST_CUSTOM_XPATH_EXPRESSION).build());
    }

    @Test
    public void testReplaceStringBuilderWithXpathBuilderWithoutPath() throws Exception {
        final StringBuilder statement = new StringBuilder();
        statement.append("/jcr:root");
        statement.append("//element(*," + TEST_TYPE + ")");
        statement.append("[@mgnl:template='").append(TEST_TPL_NAME).append("']");
        statement.append(TEST_CUSTOM_XPATH_EXPRESSION);

        final ConstraintBuilder builder = new ConstraintBuilder().addTplNameConstraint(TEST_TPL_NAME);
        assertEquals(statement.toString(), new XpathBuilder().type(TEST_TYPE).property(builder).append(TEST_CUSTOM_XPATH_EXPRESSION).build());
    }

    @Test
    public void testReplaceStringBuilderWithXpathBuilderWithoutCustomExpression() throws Exception {
        final StringBuilder statement = new StringBuilder();
        statement.append("/jcr:root");
        statement.append(TEST_NODE_PATH);
        statement.append("//element(*," + TEST_TYPE + ")");
        statement.append("[@mgnl:template='").append(TEST_TPL_NAME).append("']");

        final ConstraintBuilder builder = new ConstraintBuilder().addTplNameConstraint(TEST_TPL_NAME);
        assertEquals(statement.toString(), new XpathBuilder().path(TEST_NODE_PATH).type(TEST_TYPE).property(builder).append("").build());
    }

    @Test
    public void testComponentQueryWithoutRoot() throws RepositoryException {
        getComponentsWithTemplate(TEST_TPL_NAME, (Node) null, null);
        verify(_queryManager).createQuery("/jcr:root//element(*,mgnl:component)[@mgnl:template='test-case:pages/templateName'] order by @jcr:primaryType", XPATH);

    }

    @Test
    public void testComponentQueryWithoutConstraint() throws RepositoryException {
        Node node = mock(Node.class);
        when(node.getPath()).thenReturn("/searchRoot");
        getComponentsWithTemplate(TEST_TPL_NAME, node, null);
        verify(_queryManager).createQuery("/jcr:root/searchRoot//element(*,mgnl:component)[@mgnl:template='test-case:pages/templateName'] order by @jcr:primaryType", XPATH);
    }

    @Test
    public void testComponentQueryWithConstraint() throws RepositoryException {
        Node node = mock(Node.class);
        when(node.getPath()).thenReturn("/searchRoot");
        getComponentsWithTemplate(TEST_TPL_NAME, node, "jcr:contains(*,'123-456')");
        verify(_queryManager).createQuery("/jcr:root/searchRoot//element(*,mgnl:component)[@mgnl:template='test-case:pages/templateName' and jcr:contains(*,'123-456')] order by @jcr:primaryType", XPATH);
    }

    @Before
    public void setUp() throws RepositoryException {
        // setup mock context
        mockWebContext(stubJcrSession(WEBSITE));

        // setup mock QueryManager
        _queryManager = mockQueryManager(WEBSITE,
                stubbQuery(Query.XPATH, anyString(), stubbResult())
        );
    }

    @After
    public void cleanup() {
        cleanContext();
    }
}
