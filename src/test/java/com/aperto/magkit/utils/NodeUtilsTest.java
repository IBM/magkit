package com.aperto.magkit.utils;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockAreaNode;
import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockComponentNode;
import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockMgnlNode;
import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.MagnoliaNodeStubbingOperation.stubTemplate;
import static com.aperto.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubIdentifier;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubType;
import static com.aperto.magkit.utils.NodeUtils.getAncestorWithPrimaryType;
import static com.aperto.magkit.utils.NodeUtils.getAncestorOrSelfWithTemplate;
import static com.aperto.magkit.utils.NodeUtils.getPathForIdentifier;
import static com.aperto.magkit.utils.NodeUtils.hasSubComponents;
import static com.aperto.magkit.utils.NodeUtils.isNodeType;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation;
import com.google.protobuf.Empty;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;

/**
 * Tests for node utils.
 *
 * @author frank.sommer
 * @since 26.05.14
 */
public class NodeUtilsTest {

    public static final String NODE_TYPE = "test:type";

    @Test
    public void hasSubComponentsTest() throws RepositoryException {
        assertThat(hasSubComponents(null, null), is(false));

        Node page = mockPageNode("page");
        assertThat(hasSubComponents(page, null), is(false));
        assertThat(hasSubComponents(page, "area"), is(false));

        mockAreaNode("page/area");
        assertThat(hasSubComponents(page, "area"), is(false));

        mockAreaNode("page/area/subArea");
        assertThat(hasSubComponents(page, "area"), is(false));

        mockComponentNode("page/area/subArea/component");
        assertThat(hasSubComponents(page, "area"), is(false));

        mockComponentNode("page/area/component");
        assertThat(hasSubComponents(page, "area"), is(true));
    }

    @Test
    public void testPathForIdentifier() throws RepositoryException {
        mockPageNode("one", stubIdentifier("1"));
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
        assertFalse(isNodeType(testNode, EMPTY));
        assertFalse(isNodeType(testNode, " \t  "));
        assertFalse(isNodeType(null, NODE_TYPE));
        assertFalse(isNodeType(null, null));
    }

    @Test
    public void ancestorWithTemplate() throws RepositoryException {
        assertThat(getAncestorOrSelfWithTemplate(null, null), nullValue());
        mockNode("/root", stubTemplate("test:template"));
        Node result = mockNode("/root/result", stubTemplate("test:template"));
        Node child = mockNode("/root/result/section/test/child");
        assertThat(getAncestorOrSelfWithTemplate(child, null), nullValue());
        assertThat(getAncestorOrSelfWithTemplate(child, "test:other"), nullValue());
        assertThat(getAncestorOrSelfWithTemplate(child, "test:template"), is(result));
        assertThat(getAncestorOrSelfWithTemplate(result, "test:template"), is(result));
    }

    @Test
    public void ancestorWithType() throws RepositoryException {
        assertThat(getAncestorWithPrimaryType(null, null), nullValue());
        Node folder = mockMgnlNode("/folder", RepositoryConstants.WEBSITE, NodeTypes.Folder.NAME);
        assertThat(getAncestorWithPrimaryType(folder, null), nullValue());

        Node page = mockPageNode("/folder/page");
        Node subpage = mockPageNode("/folder/page/subpage");
        Node area = mockAreaNode("/folder/page/subpage/area");
        Node component = mockComponentNode("/folder/page/subpage/area/component");
        assertThat(getAncestorWithPrimaryType(component, NodeTypes.Area.NAME), is(area));
        assertThat(getAncestorWithPrimaryType(component, NodeTypes.Page.NAME), is(subpage));
        assertThat(getAncestorWithPrimaryType(component, NodeTypes.Folder.NAME), is(folder));

        assertThat(getAncestorWithPrimaryType(subpage, NodeTypes.Page.NAME), is(page));
    }

    @Test
    public void getChildPages() throws RepositoryException {
        assertThat(NodeUtils.getChildPages(null).size(), is(0));

        Node root = mockPageNode("root");
        assertThat(NodeUtils.getChildPages(root).size(), is(0));

        mockPageNode("root/page1");
        assertThat(NodeUtils.getChildPages(root).size(), is(1));

        mockPageNode("root/page2");
        mockPageNode("root/page2/subpage");
        mockAreaNode("root/area");
        assertThat(NodeUtils.getChildPages(root).size(), is(2));
    }

    @Test
    public void getTemplateType() throws RepositoryException {
        assertThat(NodeUtils.getTemplateType(null), nullValue());

        Node node = mockNode("test");
        assertThat(NodeUtils.getTemplateType(node), nullValue());

        stubTemplate("test:template").of(node);
        assertThat(NodeUtils.getTemplateType(node), nullValue());

        stubTemplate("test:templateWithType", TemplateDefinitionStubbingOperation.stubType("success")).of(node);
        assertThat(NodeUtils.getTemplateType(node), is("success"));
    }

    @Test
    public void getDepthTest() throws RepositoryException {
        assertThat(NodeUtils.getDepth(null), is(-1));

        Node node = mockNode("root");
        assertThat(NodeUtils.getDepth(node), is(1));

        node = mockNode("root/home");
        assertThat(NodeUtils.getDepth(node), is(2));

        node = mockNode("root/home/page");
        assertThat(NodeUtils.getDepth(node), is(3));

        Mockito.doThrow(RepositoryException.class).when(node).getDepth();
        assertThat(NodeUtils.getDepth(node), is(-1));
    }

    @Test
    public void getNodeByReference() throws RepositoryException {
        assertThat(NodeUtils.getNodeByReference(null, null), nullValue());
        assertThat(NodeUtils.getNodeByReference(" ", " "), nullValue());
        assertThat(NodeUtils.getNodeByReference("test", ""), nullValue());

        Node node = mockPageNode("root/test/page", stubIdentifier(UUID.randomUUID().toString()));
        assertThat(NodeUtils.getNodeByReference("website", "just something"), nullValue());
        assertThat(NodeUtils.getNodeByReference("website", "/root/test/page"), is(node));
        assertThat(NodeUtils.getNodeByReference("website", node.getIdentifier()), is(node));
    }

    @Before
    public void setUp() throws Exception {
        cleanContext();
    }

    @After
    public void tearDown() throws Exception {
        cleanContext();
    }
}
