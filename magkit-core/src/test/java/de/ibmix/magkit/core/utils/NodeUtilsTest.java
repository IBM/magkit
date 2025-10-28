package de.ibmix.magkit.core.utils;

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

import de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils;
import de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation;
import de.ibmix.magkit.test.jcr.NodeStubbingOperation;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.UUID;

import static de.ibmix.magkit.core.utils.NodeUtils.getAncestorOrSelfWithTemplate;
import static de.ibmix.magkit.core.utils.NodeUtils.getAncestorWithPrimaryType;
import static de.ibmix.magkit.core.utils.NodeUtils.getPathForIdentifier;
import static de.ibmix.magkit.core.utils.NodeUtils.hasSubComponents;
import static de.ibmix.magkit.core.utils.NodeUtils.isNodeType;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockAreaNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockComponentNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockMgnlNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.cms.node.PageNodeStubbingOperation.stubTemplate;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubType;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for node utils.
 *
 * @author frank.sommer
 * @since 26.05.14
 */
public class NodeUtilsTest {

    private static final String NODE_TYPE = "test:type";

    @Test
    public void hasSubComponentsTest() throws RepositoryException {
        assertFalse(hasSubComponents(null, null));

        Node page = mockPageNode("page");
        assertFalse(hasSubComponents(page, null));
        assertFalse(hasSubComponents(page, "area"));

        mockAreaNode("page/area");
        assertFalse(hasSubComponents(page, "area"));

        mockAreaNode("page/area/subArea");
        assertFalse(hasSubComponents(page, "area"));

        mockComponentNode("page/area/subArea/component");
        assertFalse(hasSubComponents(page, "area"));

        mockComponentNode("page/area/component");
        assertTrue(hasSubComponents(page, "area"));
    }

    @Test
    public void testPathForIdentifier() throws RepositoryException {
        mockPageNode("one", stubIdentifier("1"));
        assertNull(getPathForIdentifier(null, null));
        assertNull(getPathForIdentifier("", "1"));
        assertNull(getPathForIdentifier("any", ""));
        assertNull(getPathForIdentifier("any", "x"));
        assertNull(getPathForIdentifier(WEBSITE, "x"));
        assertEquals("/one", getPathForIdentifier(WEBSITE, "1"));
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
        assertNull(getAncestorOrSelfWithTemplate(null, null));
        mockNode("/root", stubTemplate("test:template"));
        Node result = mockNode("/root/result", stubTemplate("test:template"));
        Node child = mockNode("/root/result/section/test/child");
        assertNull(getAncestorOrSelfWithTemplate(child, null));
        assertNull(getAncestorOrSelfWithTemplate(child, "test:other"));
        assertEquals(result, getAncestorOrSelfWithTemplate(child, "test:template"));
        assertEquals(result, getAncestorOrSelfWithTemplate(result, "test:template"));
    }

    @Test
    public void ancestorWithType() throws RepositoryException {
        assertNull(getAncestorWithPrimaryType(null, null));
        Node folder = mockMgnlNode(RepositoryConstants.WEBSITE, "/folder", NodeTypes.Folder.NAME);
        assertNull(getAncestorWithPrimaryType(folder, null));

        Node page = mockPageNode("/folder/page");
        Node subpage = mockPageNode("/folder/page/subpage");
        Node area = mockAreaNode("/folder/page/subpage/area");
        Node component = mockComponentNode("/folder/page/subpage/area/component");
        assertEquals(area, getAncestorWithPrimaryType(component, NodeTypes.Area.NAME));
        assertEquals(subpage, getAncestorWithPrimaryType(component, NodeTypes.Page.NAME));
        assertEquals(folder, getAncestorWithPrimaryType(component, NodeTypes.Folder.NAME));

        assertEquals(page, getAncestorWithPrimaryType(subpage, NodeTypes.Page.NAME));
    }

    @Test
    public void getChildPages() throws RepositoryException {
        assertEquals(0, NodeUtils.getChildPages(null).size());

        Node root = mockPageNode("root");
        assertEquals(0, NodeUtils.getChildPages(root).size());

        mockPageNode("root/page1");
        assertEquals(1, NodeUtils.getChildPages(root).size());

        mockPageNode("root/page2");
        mockPageNode("root/page2/subpage");
        mockAreaNode("root/area");
        assertEquals(2, NodeUtils.getChildPages(root).size());
    }

    @Test
    public void getTemplateType() throws RepositoryException {
        assertNull(NodeUtils.getTemplateType(null));

        Node node = mockNode("test");
        assertNull(NodeUtils.getTemplateType(node));

        stubTemplate("test:template").of(node);
        assertNull(NodeUtils.getTemplateType(node));

        stubTemplate("test:templateWithType", TemplateDefinitionStubbingOperation.stubType("success")).of(node);
        assertEquals("success", NodeUtils.getTemplateType(node));
    }

    @Test
    public void getDepthTest() throws RepositoryException {
        assertEquals(-1, NodeUtils.getDepth(null));

        Node node = mockNode("root");
        assertEquals(1, NodeUtils.getDepth(node));

        node = mockNode("root/home");
        assertEquals(2, NodeUtils.getDepth(node));

        node = mockNode("root/home/page");
        assertEquals(3, NodeUtils.getDepth(node));

        Mockito.doThrow(RepositoryException.class).when(node).getDepth();
        assertEquals(-1, NodeUtils.getDepth(node));
    }

    @Test
    public void getNodeByReference() throws RepositoryException {
        assertNull(NodeUtils.getNodeByReference(null, null));
        assertNull(NodeUtils.getNodeByReference(" ", " "));
        assertNull(NodeUtils.getNodeByReference("test", ""));

        Node node = mockPageNode("root/test/page", stubIdentifier(UUID.randomUUID().toString()));
        assertNull(NodeUtils.getNodeByReference("website", "just something"));
        assertEquals(node, NodeUtils.getNodeByReference("website", "/root/test/page"));
        assertEquals(node, NodeUtils.getNodeByReference("website", node.getIdentifier()));
    }

    @Test
    public void getIdentifier() throws RepositoryException {
        assertNull(NodeUtils.getIdentifier(null));
        final Node node = mockNode(stubIdentifier("1234"));
        assertEquals("1234", NodeUtils.getIdentifier(node));
    }

    @Test
    public void getPath() throws RepositoryException {
        assertNull(NodeUtils.getPath(null));
        final Node node = mockNode("node");
        assertEquals("/node", NodeUtils.getPath(node));
    }

    @Test
    public void getName() throws RepositoryException {
        assertNull(NodeUtils.getName(null));
        final Node node = mockNode("node");
        assertEquals("node", NodeUtils.getName(node));
    }

    @Test
    public void getNodeByIdentifier() throws RepositoryException {
        Node expected = MagnoliaNodeMockUtils.mockPageNode("test", stubIdentifier("123"));
        assertNull(NodeUtils.getNodeByIdentifier(null));
        assertNull(NodeUtils.getNodeByIdentifier(EMPTY));
        assertNull(NodeUtils.getNodeByIdentifier("456"));
        assertEquals(expected, NodeUtils.getNodeByIdentifier("123"));
    }

    @Test
    public void getChildAssetNodes() throws RepositoryException {
        final Node test = mockNode("node");
        assertEquals(0, NodeUtils.getChildAssetNodes(null).size());
        assertEquals(0, NodeUtils.getChildAssetNodes(test).size());

        MagnoliaNodeMockUtils.mockComponentNode("node/component");
        assertEquals(0, NodeUtils.getChildAssetNodes(test).size());

        final Node asset = mockNode("node/asset1", NodeStubbingOperation.stubType("mgnl:asset"));
        assertEquals(1, NodeUtils.getChildAssetNodes(test).size());
        assertEquals(asset, NodeUtils.getChildAssetNodes(test).get(0));

        mockNode("node/asset2", NodeStubbingOperation.stubType("mgnl:asset"));
        assertEquals(2, NodeUtils.getChildAssetNodes(test).size());
    }

    @Test
    public void getNodes() throws RepositoryException {
        assertNull(NodeUtils.getNodes(null));
        Node node = mockNode("test");
        assertFalse(NodeUtils.getNodes(node).hasNext());
        Mockito.verify(node).getNodes();
    }

    @Test
    public void getNodesWithNamePattern() throws RepositoryException {
        assertNull(NodeUtils.getNodes(null, "pattern"));
        Node node = mockNode();
        assertFalse(NodeUtils.getNodes(node, "pattern").hasNext());
        Mockito.verify(node).getNodes("pattern");
    }

    @Test
    public void getNodesWithNameGlobs() throws RepositoryException {
        String[] globs = new String[]{"pattern1", "pattern2"};
        assertNull(NodeUtils.getNodes(null, globs));
        Node node = mockNode();
        assertFalse(NodeUtils.getNodes(node, globs).hasNext());
        Mockito.verify(node).getNodes(globs);
    }

    @AfterEach
    public void tearDown() throws Exception {
        cleanContext();
    }
}
