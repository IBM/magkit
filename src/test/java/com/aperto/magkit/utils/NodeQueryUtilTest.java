package com.aperto.magkit.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryManager;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static com.aperto.magkit.mockito.WebContextStubbingOperation.stubJcrSession;
import static com.aperto.magkit.mockito.jcr.QueryMockUtils.mockQueryManager;
import static com.aperto.magkit.utils.NodeQueryUtil.findDescendantComponents;
import static com.aperto.magkit.utils.NodeQueryUtil.getComponentsWithTemplate;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static javax.jcr.query.Query.JCR_SQL2;
import static javax.jcr.query.Query.XPATH;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        final ConstraintBuilder builder = new ConstraintBuilder().addTplNameConstraint(TEST_TPL_NAME);
        assertEquals("/jcr:root" + TEST_NODE_PATH + "//element(*," + TEST_TYPE + ")" + "[@mgnl:template='" + TEST_TPL_NAME + "']" + TEST_CUSTOM_XPATH_EXPRESSION, new XpathBuilder().path(TEST_NODE_PATH).type(TEST_TYPE).property(builder).append(TEST_CUSTOM_XPATH_EXPRESSION).build());
    }

    @Test
    public void testReplaceStringBuilderWithXpathBuilderWithoutPath() throws Exception {
        final ConstraintBuilder builder = new ConstraintBuilder().addTplNameConstraint(TEST_TPL_NAME);
        assertEquals("/jcr:root" + "//element(*," + TEST_TYPE + ")" + "[@mgnl:template='" + TEST_TPL_NAME + "']" + TEST_CUSTOM_XPATH_EXPRESSION, new XpathBuilder().type(TEST_TYPE).property(builder).append(TEST_CUSTOM_XPATH_EXPRESSION).build());
    }

    @Test
    public void testReplaceStringBuilderWithXpathBuilderWithoutCustomExpression() throws Exception {
        final ConstraintBuilder builder = new ConstraintBuilder().addTplNameConstraint(TEST_TPL_NAME);
        assertEquals("/jcr:root" + TEST_NODE_PATH + "//element(*," + TEST_TYPE + ")" + "[@mgnl:template='" + TEST_TPL_NAME + "']", new XpathBuilder().path(TEST_NODE_PATH).type(TEST_TYPE).property(builder).append("").build());
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

    @Test
    public void findComponentsWithSingleSearchRoot() throws Exception {
        findDescendantComponents(TEST_TPL_NAME, TEST_NODE_PATH + "/area1");
        verify(_queryManager).createQuery("select * from [mgnl:component] where [mgnl:template] = 'test-case:pages/templateName' and (ISDESCENDANTNODE('/portal/news/node/area1'))", JCR_SQL2);
    }

    @Test
    public void findComponentsWithMultipleSearchRoots() throws Exception {
        findDescendantComponents(TEST_TPL_NAME, TEST_NODE_PATH + "/area1", TEST_NODE_PATH + "/area2");
        verify(_queryManager).createQuery("select * from [mgnl:component] where [mgnl:template] = 'test-case:pages/templateName' and (ISDESCENDANTNODE('/portal/news/node/area1') or ISDESCENDANTNODE('/portal/news/node/area2'))", JCR_SQL2);
    }

    @Before
    public void setUp() throws RepositoryException {
        // setup mock context
        mockWebContext(stubJcrSession(WEBSITE));

        // setup mock QueryManager
        _queryManager = mockQueryManager(WEBSITE);
    }

    @After
    public void cleanup() {
        cleanContext();
    }
}
