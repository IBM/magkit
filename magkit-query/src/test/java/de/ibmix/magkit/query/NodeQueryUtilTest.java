package de.ibmix.magkit.query;

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

import de.ibmix.magkit.query.xpath.ConstraintBuilder;
import de.ibmix.magkit.query.xpath.XpathBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.ibmix.magkit.query.NodeQueryUtil.findDescendantComponent;
import static de.ibmix.magkit.query.NodeQueryUtil.findDescendantComponents;
import static de.ibmix.magkit.query.NodeQueryUtil.getComponentsWithTemplate;
import static de.ibmix.magkit.query.NodeQueryUtil.getPagesWithTemplate;
import static de.ibmix.magkit.query.NodeQueryUtil.createSqlQuery;
import static de.ibmix.magkit.query.NodeQueryUtil.createXPathQuery;
import static de.ibmix.magkit.query.NodeQueryUtil.createQuery;
import static de.ibmix.magkit.query.NodeQueryUtil.executeSqlQuery;
import static de.ibmix.magkit.query.NodeQueryUtil.executeQuery;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQuery;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQueryResult;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.query.QueryMockUtils.mockQueryManager;
import static de.ibmix.magkit.test.jcr.query.QueryStubbingOperation.stubResult;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static javax.jcr.query.Query.JCR_SQL2;
import static javax.jcr.query.Query.XPATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link NodeQueryUtil} covering query building, execution and binding behaviors.
 *
 * @author philipp.guettler
 * @author wolf.bubenik@ibmix.de
 * @since 27.09.13
 */
public class NodeQueryUtilTest {
    private static final String TEST_TPL_NAME = "test-case:pages/templateName";
    private static final String TEST_NODE_PATH = "/portal/news/node";
    private static final String TEST_TYPE = "test:type";
    private static final String TEST_CUSTOM_XPATH_EXPRESSION = "other constraints";
    private QueryManager _queryManager;

    @Test
    public void testReplaceStringBuilderWithXpathBuilder() {
        final ConstraintBuilder builder = new ConstraintBuilder().addTplNameConstraint(TEST_TPL_NAME);
        assertEquals("/jcr:root" + TEST_NODE_PATH + "//element(*," + TEST_TYPE + ")" + "[@mgnl:template='" + TEST_TPL_NAME + "']" + TEST_CUSTOM_XPATH_EXPRESSION, new XpathBuilder().path(TEST_NODE_PATH).type(TEST_TYPE).property(builder).append(TEST_CUSTOM_XPATH_EXPRESSION).build());
    }

    @Test
    public void testReplaceStringBuilderWithXpathBuilderWithoutPath() {
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

    @Test
    public void findComponentsWithoutSearchRoots() throws Exception {
        findDescendantComponents(TEST_TPL_NAME);
        verify(_queryManager).createQuery("select * from [mgnl:component] where [mgnl:template] = 'test-case:pages/templateName'", JCR_SQL2);
    }

    @Test
    public void getComponentsWithTemplateStringRootAndConstraint() throws Exception {
        getComponentsWithTemplate(TEST_TPL_NAME, "/stringRoot", "@prop='value'");
        verify(_queryManager).createQuery("/jcr:root/stringRoot//element(*,mgnl:component)[@mgnl:template='test-case:pages/templateName' and @prop='value'] order by @jcr:primaryType", XPATH);
    }

    @Test
    public void getPagesWithTemplateOnlyTemplate() throws Exception {
        getPagesWithTemplate(TEST_TPL_NAME, (Node) null);
        verify(_queryManager).createQuery("/jcr:root//element(*,mgnl:page)[MetaData/@mgnl:template='test-case:pages/templateName']", XPATH);
    }

    @Test
    public void getPagesWithTemplateWithRoot() throws Exception {
        Node root = mockNode("/rootPath");
        getPagesWithTemplate(TEST_TPL_NAME, root);
        verify(_queryManager).createQuery("/jcr:root/rootPath//element(*,mgnl:page)[MetaData/@mgnl:template='test-case:pages/templateName']", XPATH);
    }

    @Test
    public void getPagesWithTemplateWithRootAndCondition() throws Exception {
        Node root = mockNode("/rootPath");
        getPagesWithTemplate(TEST_TPL_NAME, root, "[jcr:contains(.,'abc')]");
        verify(_queryManager).createQuery("/jcr:root/rootPath//element(*,mgnl:page)[MetaData/@mgnl:template='test-case:pages/templateName'][jcr:contains(.,'abc')]", XPATH);
    }

    @Test
    public void findDescendantComponentReturnsFirstResult() throws Exception {
        Node result1 = mockNode(WEBSITE, "/page/area/component1");
        Node result2 = mockNode(WEBSITE, "/page/area/component2");
        String statement = "select * from [mgnl:component] where [mgnl:template] = '" + TEST_TPL_NAME + "' and (ISDESCENDANTNODE('/root'))";
        mockQueryResult(WEBSITE, JCR_SQL2, statement, result1, result2);
        Node found = findDescendantComponent(TEST_TPL_NAME, "/root");
        assertEquals(result1, found);
    }

    @Test
    public void executeSqlQueryWrapper() throws Exception {
        Node n1 = mockNode(WEBSITE, "/node1");
        String statement = "select * from [nt:base]";
        mockQueryResult(WEBSITE, JCR_SQL2, statement, n1);
        List<Node> result = executeSqlQuery("select * from [nt:base]");
        assertEquals(1, result.size());
        assertEquals(n1, result.get(0));
    }

    @Test
    public void createQueryWithBindValuesSql2() throws Exception {
        Value v1 = mock(Value.class);
        Value v2 = mock(Value.class);
        Map<String, Value> bindValues = new HashMap<>();
        bindValues.put("var1", v1);
        bindValues.put("var2", v2);
        Query sqlQueryMock = mockQuery(WEBSITE, JCR_SQL2, "select * from [nt:base] where prop=$var1 and other=$var2");
        createQuery("select * from [nt:base] where prop=$var1 and other=$var2", JCR_SQL2, bindValues, WEBSITE);
        verify(sqlQueryMock, times(1)).bindValue("var1", v1);
        verify(sqlQueryMock, times(1)).bindValue("var2", v2);
    }

    @Test
    public void createQueryWithBindValuesXPathDoesNotBind() throws Exception {
        Value v1 = mock(Value.class);
        Map<String, Value> bindValues = new HashMap<>();
        bindValues.put("var1", v1);
        Query xpathQueryMock = mockQuery(WEBSITE, XPATH, "/jcr:root//element(*,mgnl:component)");
        createQuery("/jcr:root//element(*,mgnl:component)", XPATH, bindValues, WEBSITE);
        verify(xpathQueryMock, never()).bindValue(anyString(), any());
    }

    @Test
    public void createSqlAndXPathConvenience() throws Exception {
        mockQuery(WEBSITE, XPATH, "/jcr:root//element(*,mgnl:component)");
        mockQuery(WEBSITE, JCR_SQL2, "select * from [nt:base]");
        assertNotNull(createSqlQuery("select * from [nt:base]", null));
        assertNotNull(createXPathQuery("/jcr:root//element(*,mgnl:component)"));
    }

    @Test
    public void executeQueryMultiSelectorRows() throws Exception {
        Node page1 = mockNode(WEBSITE, "/page1");
        Node page2 = mockNode(WEBSITE, "/page2");
        Node subPage1 = mockNode(WEBSITE, "/page1/page");
        Node subPage2 = mockNode(WEBSITE, "/page2/page");
        Query query = mockQuery(WEBSITE, JCR_SQL2, "select * from [nt:base]", stubResult(page1, page2));

        List<Node> resultNodes = executeQuery(query, "page");
        assertEquals(2, resultNodes.size());
        assertEquals(subPage1, resultNodes.get(0));
        assertEquals(subPage2, resultNodes.get(1));
    }

    @Test
    public void executeQueryMultiSelectorRowsRepositoryException() throws Exception {
        Query query = mock(Query.class);
        when(query.execute()).thenThrow(new RepositoryException("error"));
        List<Node> resultNodes = executeQuery(query, "page");
        assertEquals(0, resultNodes.size());
    }

    @Test
    public void executeQueryNullQueryReturnsEmpty() {
        List<Node> resultNodes = executeQuery((Query) null, "page");
        assertTrue(resultNodes.isEmpty());
    }

    @BeforeEach
    public void setUp() throws RepositoryException {
        // setup mock context
        mockWebContext(stubJcrSession(WEBSITE));

        // setup mock QueryManager
        _queryManager = mockQueryManager(WEBSITE);
    }

    @AfterEach
    public void cleanup() {
        cleanContext();
    }
}
