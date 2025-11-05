package de.ibmix.magkit.setup.nodebuilder;

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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.jcr.nodebuilder.ErrorHandler;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.voting.voters.URIPatternVoter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import de.ibmix.magkit.test.jcr.NodeMockUtils;

import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubType;
import static de.ibmix.magkit.test.jcr.SessionMockUtils.mockSession;
import static de.ibmix.magkit.test.jcr.SessionStubbingOperation.stubValueFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link NodeOperationFactory} covering path creation branches, existing path usage, type mismatch, property mutation, removals, ordering and voter creation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-26
 */
public class NodeOperationFactoryTest {

    private static final ErrorHandler EH = mock(ErrorHandler.class);

    @BeforeEach
    public void setUp() {
        ContextMockUtils.cleanContext();
        reset(EH);
    }

    /**
     * Creates missing path segments with explicit type and verifies terminal existence.
     */
    @Test
    public void createMissingPathWithType() throws RepositoryException {
        Node root = NodeMockUtils.mockNode("setup", "/root", stubType(NodeTypes.Content.NAME));
        NodeOperationFactory.addOrGetNode("a/b/c", NodeTypes.ContentNode.NAME).exec(root, EH);
        assertTrue(root.hasNode("a/b/c"));
    }

    /**
     * Creates missing path segments using default content type when type is null.
     */
    @Test
    public void createMissingPathDefaultContentType() throws RepositoryException {
        Node root = NodeMockUtils.mockNode("setup", "/root", stubType(NodeTypes.Content.NAME));
        NodeOperationFactory.addOrGetNode("/d/e/f/", null).exec(root, EH);
        assertTrue(root.hasNode("d/e/f"));
    }

    /**
     * Returns existing node without recreation when type matches.
     */
    @Test
    public void existingPathReturnsNodeWhenTypeMatches() throws RepositoryException {
        Node root = NodeMockUtils.mockNode("setup", "/root", stubType(NodeTypes.Content.NAME));
        Node existing = NodeMockUtils.mockNode("setup", "/root/x", stubType(NodeTypes.ContentNode.NAME));
        NodeOperationFactory.addOrGetNode("x", NodeTypes.ContentNode.NAME).exec(root, EH);
        assertEquals(existing.getPath(), root.getNode("x").getPath());
    }

    /**
     * Throws RepositoryException on type mismatch of existing terminal node.
     */
    @Test
    public void existingPathThrowsOnTypeMismatch() throws RepositoryException {
        Node root = NodeMockUtils.mockNode("setup", "/root", stubType(NodeTypes.Content.NAME));
        NodeMockUtils.mockNode("setup", "/root/mismatch", stubType(NodeTypes.Content.NAME));
        NodeOperationFactory.addOrGetNode("mismatch", NodeTypes.ContentNode.NAME).exec(root, EH);
        verify(EH).handle(any(RepositoryException.class), eq(root));
    }

    /**
     * Overwrites property value ignoring previous value, verifying setProperty invocation twice.
     */
    @Test
    public void addOrSetPropertyOverwritesValue() throws RepositoryException {
        mockSession("setup", stubValueFactory());
        Node node = NodeMockUtils.mockNode("setup", "/root/node", stubProperty("flag", "old"));
        NodeOperationFactory.addOrSetProperty("flag", "old").exec(node, EH);
        NodeOperationFactory.addOrSetProperty("flag", true).exec(node, EH);
        verify(node, times(2)).setProperty(eq("flag"), any(Value.class));
    }

    /**
     * Removes existing property via session.removeItem.
     */
    @Test
    public void removeIfExistsRemovesProperty() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("setup", "/root/node", stubProperty("prop", "value"));
        NodeOperationFactory.removeIfExists("prop").exec(node, EH);
        verify(node.getSession()).removeItem("/root/node/prop");
    }

    /**
     * Removes existing child node via session.removeItem.
     */
    @Test
    public void removeIfExistsRemovesChild() throws RepositoryException {
        Node parent = NodeMockUtils.mockNode("setup", "/root/parent", stubNode("child"));
        NodeOperationFactory.removeIfExists("child").exec(parent, EH);
        verify(parent.getSession()).removeItem("/root/parent/child");
    }

    /**
     * Skips removal when property and node are absent.
     */
    @Test
    public void removeIfExistsSkipsAbsent() throws RepositoryException {
        Node parent = NodeMockUtils.mockNode("setup", "/root/skip", stubType(NodeTypes.Content.NAME));
        NodeOperationFactory.removeIfExists("absent").exec(parent, EH);
        verify(parent.getSession(), never()).removeItem(anyString());
    }

    /**
     * Removes all direct child nodes, verifying session.removeItem for each.
     */
    @Test
    public void removeAllChildsRemovesChildren() throws RepositoryException {
        Node parent = NodeMockUtils.mockNode("setup", "/root/parentAll", stubNode("a"), stubNode("b"));
        NodeOperationFactory.removeAllChilds().exec(parent, EH);
        verify(parent.getSession()).removeItem("/root/parentAll/a");
        verify(parent.getSession()).removeItem("/root/parentAll/b");
    }

    /**
     * Delegates ordering before to parent orderBefore.
     */
    @Test
    public void orderBeforeDelegates() throws RepositoryException {
        Node parent = NodeMockUtils.mockNode("setup", "/root/order");
        Node second = NodeMockUtils.mockNode("setup", "/root/order/second");
        NodeOperationFactory.orderBefore("third", "second").exec(second, EH);
        verify(parent).orderBefore("third", "second");
    }

    /**
     * Executes orderAfter without exception (cannot easily verify internal NodeUtil static behaviour).
     */
    @Test
    public void orderAfterExecutes() throws RepositoryException {
        Node parent = NodeMockUtils.mockNode("setup", "/root/orderAfter", stubNode("a"), stubNode("other"));
        NodeOperationFactory.orderAfter("a", "other").exec(parent, EH);
        verify(parent).orderBefore("a", null);
    }

    /**
     * Creates URI pattern voter node and sets pattern and class properties (verifies setProperty calls).
     */
    @Test
    public void addUriPatternVoterCreatesNode() throws RepositoryException {
        mockSession("setup", stubValueFactory());
        Node parent = NodeMockUtils.mockNode("setup", "/root/voters");
        NodeOperationFactory.addUriPatternVoter("voterUri", "/path/.*").exec(parent, EH);
        Node voter = parent.getNode("voterUri");
        assertNotNull(voter);
        assertEquals(URIPatternVoter.class.getName(), voter.getProperty("class").getString());
        assertEquals("/path/.*", voter.getProperty("pattern").getString());
    }

    /**
     * Creates generic pattern voter node and sets class and pattern properties.
     */
    @Test
    public void addPatternVoterCreatesNode() throws RepositoryException {
        mockSession("setup", stubValueFactory());
        Node parent = NodeMockUtils.mockNode("setup", "/root/votersGeneric");
        NodeOperationFactory.addPatternVoter("customVoter", "com.example.CustomVoter", "^/custom/.*").exec(parent, EH);
        Node voter = parent.getNode("customVoter");
        assertNotNull(voter);
        assertEquals("com.example.CustomVoter", voter.getProperty("class").getString());
        assertEquals("^/custom/.*", voter.getProperty("pattern").getString());
    }
}
