package de.ibmix.magkit.core.node;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2025 IBM iX
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

import de.ibmix.magkit.test.jcr.SessionMockUtils;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.NodeIterator;
import javax.jcr.Value;
import javax.jcr.version.Version;

import java.util.ArrayList;
import java.util.List;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link NullableDelegateNodeWrapper} covering synthetic construction, validation behaviour,
 * safe default responses when no wrapped node exists and basic delegation when a node is present.
 *
 * Edge cases covered:
 * <ul>
 *   <li>Constructor validation for null/empty name and primary type.</li>
 *   <li>Null wrapped node constructor rejection.</li>
 *   <li>All read methods return neutral defaults in synthetic mode (null, empty, zero, empty string).</li>
 *   <li>No-op behaviour for write/mutation methods in synthetic mode.</li>
 *   <li>Delegation path for name, path, property, hasProperty, iterators.</li>
 *   <li>isSame logic for identical underlying node and self-reference.</li>
 * </ul>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-20
 */
public class NullableDelegateNodeWrapperTest {

    @After
    public void tearDown() {
        SessionMockUtils.cleanSession();
    }

    @Test(expected = NullPointerException.class)
    public void syntheticConstructorNullNameThrows() {
        new TestNullableDelegateNodeWrapper(null, "mgnl:content");
    }

    @Test(expected = IllegalArgumentException.class)
    public void syntheticConstructorEmptyNameThrows() {
        new TestNullableDelegateNodeWrapper("", "mgnl:content");
    }

    @Test(expected = NullPointerException.class)
    public void syntheticConstructorNullPrimaryTypeThrows() {
        new TestNullableDelegateNodeWrapper("virtual", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void syntheticConstructorEmptyPrimaryTypeThrows() {
        new TestNullableDelegateNodeWrapper("virtual", "");
    }

    @Test(expected = NullPointerException.class)
    public void wrappingConstructorNullNodeThrows() {
        new TestNullableDelegateNodeWrapper((Node) null);
    }

    @Test
    public void syntheticDefaults() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertFalse(synthetic.hasWrappedNode());
        assertThat(synthetic.getName(), is("virtual"));
        assertThat(synthetic.getPath(), is("/virtual"));
        assertThat(synthetic.getPrimaryNodeType().getName(), is("mgnl:content"));
        assertThat(synthetic.getIdentifier(), is(""));
        assertThat(synthetic.getUUID(), is(""));
        assertThat(synthetic.getIndex(), is(0));
        assertThat(synthetic.getDepth(), is(0));
        assertFalse(synthetic.isNode());
        assertFalse(synthetic.isModified());
        assertFalse(synthetic.isNew());
        assertFalse(synthetic.hasNodes());
        assertFalse(synthetic.hasProperties());
        assertFalse(synthetic.hasProperty("any"));
        assertThat(synthetic.getNode("child"), nullValue());
        assertThat(synthetic.getProperty("prop"), nullValue());
        NodeIterator nodes = synthetic.getNodes();
        assertFalse(nodes.hasNext());
        PropertyIterator props = synthetic.getProperties();
        assertFalse(props.hasNext());
        assertThat(synthetic.toString(), is(""));
    }

    @Test
    public void syntheticWriteOperationsNoOp() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        synthetic.addMixin("mix:versionable");
        synthetic.setProperty("p", "v");
        synthetic.lock(false, false);
        synthetic.unlock();
        synthetic.save();
        synthetic.remove();
        synthetic.refresh(false);
        assertThat(synthetic.getProperty("p"), nullValue());
        assertThat(synthetic.lock(false, false), nullValue());
    }

    @Test
    public void syntheticAdditionalWriteNoOps() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        // Version instances mocked to satisfy signatures
        Version v = mock(Version.class);
        synthetic.doneMerge(v);
        synthetic.cancelMerge(v);
        synthetic.restore("1.0", true);
        synthetic.restore(v, true);
        synthetic.restore(v, "rel", true);
        synthetic.restoreByLabel("lbl", true);
        synthetic.followLifecycleTransition("activate");
        synthetic.setPrimaryType("mgnl:page");
        synthetic.removeMixin("mix:versionable");
        synthetic.removeShare();
        synthetic.removeSharedSet();
        // Assert more default getters still neutral
        assertThat(synthetic.getAllowedLifecycleTransistions(), nullValue());
        assertThat(synthetic.getDefinition(), nullValue());
    }

    @Test
    public void delegationReadsUnderlyingNode() throws Exception {
        Node base = mockNode("base", stubProperty("title", "underlying"));
        TestNullableDelegateNodeWrapper wrapper = new TestNullableDelegateNodeWrapper(base);
        assertTrue(wrapper.hasWrappedNode());
        assertThat(wrapper.getName(), is(base.getName()));
        assertThat(wrapper.getPath(), is(base.getPath()));
        Property p = wrapper.getProperty("title");
        assertThat(p.getString(), is("underlying"));
        assertTrue(wrapper.getProperties("title*").hasNext());
    }

    @Test
    public void isSameUnderlyingAndSyntheticCases() throws Exception {
        Node n1 = mockNode("root/a", stubProperty("x", "1"));
        TestNullableDelegateNodeWrapper w1 = new TestNullableDelegateNodeWrapper(n1);
        assertTrue(w1.isSame(w1.getWrappedNode()));
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertFalse(synthetic.isSame(n1));
    }

    @Test
    public void isSameSelfReference() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virt", "mgnl:content");
        assertTrue(synthetic.isSame(synthetic));
    }

    @Test
    public void emptyIteratorsProduceNoElements() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        NodeIterator nodeIt = synthetic.getNodes(new String[]{"*"});
        List<String> names = new ArrayList<>();
        while (nodeIt.hasNext()) {
            names.add(nodeIt.nextNode().getName());
        }
        assertTrue(names.isEmpty());
        PropertyIterator pit = synthetic.getProperties(new String[]{"*"});
        assertFalse(pit.hasNext());
    }

    @Test
    public void getCorrespondingNodePathSyntheticReturnsNull() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertThat(synthetic.getCorrespondingNodePath("otherWs"), nullValue());
    }

    @Test
    public void lockAndVersioningReturnsNullSynthetic() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertThat(synthetic.getBaseVersion(), nullValue());
        assertThat(synthetic.getVersionHistory(), nullValue());
        assertThat(synthetic.getLock(), nullValue());
    }

    @Test
    public void getPrimaryItemNullWhenSynthetic() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertThat(synthetic.getPrimaryItem(), nullValue());
    }

    @Test
    public void addNodeReturnsNullSynthetic() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertThat(synthetic.addNode("child"), nullValue());
        assertThat(synthetic.addNode("child", "nt:unstructured"), nullValue());
    }

    @Test
    public void setPropertyVariousOverloadsReturnNullSynthetic() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertThat(synthetic.setProperty("p", "v"), nullValue());
        assertThat(synthetic.setProperty("p", new String[]{"a", "b"}), nullValue());
        assertThat(synthetic.setProperty("p", new String[]{"a", "b"}, 1), nullValue());
        assertThat(synthetic.setProperty("p", (Value) null), nullValue());
        assertThat(synthetic.setProperty("p", (Value[]) null), nullValue());
    }

    @Test
    public void syntheticSetPropertyRemainingOverloadsReturnNull() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertThat(synthetic.setProperty("v1", (Value) null), nullValue());
        assertThat(synthetic.setProperty("v2", (Value[]) null), nullValue());
        assertThat(synthetic.setProperty("v3", (Value) null, 1), nullValue());
        assertThat(synthetic.setProperty("v4", (Value[]) null, 1), nullValue());
        assertThat(synthetic.setProperty("v5", "str", 1), nullValue());
        assertThat(synthetic.setProperty("v6", new java.io.ByteArrayInputStream(new byte[0])), nullValue());
        javax.jcr.Binary bin = mock(javax.jcr.Binary.class);
        assertThat(synthetic.setProperty("v7", bin), nullValue());
        assertThat(synthetic.setProperty("v8", true), nullValue());
        assertThat(synthetic.setProperty("v9", 1.23d), nullValue());
        assertThat(synthetic.setProperty("v10", java.math.BigDecimal.ONE), nullValue());
        assertThat(synthetic.setProperty("v11", 123L), nullValue());
        assertThat(synthetic.setProperty("v12", java.util.Calendar.getInstance()), nullValue());
        Node node = mockNode("otherNode");
        assertThat(synthetic.setProperty("v13", node), nullValue());
    }

    @Test
    public void delegatedLifecycleDefinitionAndPrimaryType() throws Exception {
        Node base = mockNode("base", stubProperty("x", "y"));
        TestNullableDelegateNodeWrapper wrapper = new TestNullableDelegateNodeWrapper(base);
        // Branch execution; values may be null depending on test node implementation
        wrapper.getAllowedLifecycleTransistions();
        verify(base).getAllowedLifecycleTransistions();
        wrapper.getDefinition();
        verify(base).getDefinition();
        assertThat(wrapper.getPrimaryNodeType().getName(), is(base.getPrimaryNodeType().getName()));
    }

    @Test
    public void isSameNonNodeItemReturnsFalse() throws Exception {
        Node n1 = mockNode("root/a", stubProperty("x", "1"));
        TestNullableDelegateNodeWrapper w1 = new TestNullableDelegateNodeWrapper(n1);
        Property prop = n1.getProperty("x");
        assertFalse(w1.isSame(prop));
    }

    @Test
    public void delegatedToStringUsesUnderlying() throws Exception {
        Node base = mockNode("base", stubProperty("x", "y"));
        TestNullableDelegateNodeWrapper wrapper = new TestNullableDelegateNodeWrapper(base);
        assertThat(wrapper.toString(), is(base.toString()));
    }

    @Test
    public void syntheticGetMixinNodeTypesReturnsNull() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertThat(synthetic.getMixinNodeTypes(), nullValue());
    }

    @Test
    public void syntheticPatternAndGlobNodeIteratorsEmpty() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertFalse(synthetic.getNodes("any*").hasNext());
        assertFalse(synthetic.getNodes(new String[]{"any*"}).hasNext());
    }

    @Test
    public void syntheticPatternPropertiesIteratorEmpty() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertFalse(synthetic.getProperties("prop*").hasNext());
    }

    @Test
    public void syntheticReferencesAndSharedSetEmpty() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertFalse(synthetic.getReferences().hasNext());
        assertFalse(synthetic.getReferences("any").hasNext());
        assertFalse(synthetic.getWeakReferences().hasNext());
        assertFalse(synthetic.getWeakReferences("any").hasNext());
        assertFalse(synthetic.getSharedSet().hasNext());
    }

    @Test
    public void syntheticMergeReturnsEmptyIterator() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertFalse(synthetic.merge("ws", true).hasNext());
    }

    @Test
    public void syntheticIsNodeTypeAlwaysFalse() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertFalse(synthetic.isNodeType("mgnl:content"));
        assertFalse(synthetic.isNodeType("other:type"));
    }

    @Test
    public void syntheticAcceptDoesNotInvokeVisitor() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        ItemVisitor visitor = mock(ItemVisitor.class);
        synthetic.accept(visitor);
        verify(visitor, never()).visit(any(Node.class));
        verify(visitor, never()).visit(any(Property.class));
    }

    @Test
    public void syntheticGetAncestorReturnsNull() throws Exception {
        TestNullableDelegateNodeWrapper synthetic = new TestNullableDelegateNodeWrapper("virtual", "mgnl:content");
        assertThat(synthetic.getAncestor(0), nullValue());
    }

    @Test
    public void delegatedIdentifier() throws Exception {
        Node base = mockNode("base", stubProperty("x", "y"));
        String expectedId = base.getIdentifier();
        TestNullableDelegateNodeWrapper wrapper = new TestNullableDelegateNodeWrapper(base);
        assertThat(wrapper.getIdentifier(), is(expectedId));
    }

    /**
     * Exercises a broad set of delegated operations to cover hasWrappedNode true branches. Any repository exceptions are ignored;
     * the intent is branch execution rather than repository state change.
     */
    @Test
    public void delegatedOperationsExecuteOnNodeTestFlags() throws Exception {
        Node base = mockNode("base", stubProperty("title", "underlying"));
        TestNullableDelegateNodeWrapper wrapper = new TestNullableDelegateNodeWrapper(base);
        assertTrue(wrapper.hasWrappedNode());

        // Boolean/flag methods (may all be false but branch to underlying is executed)
        wrapper.hasNode("unknown");
        verify(base).hasNode("unknown");
        wrapper.hasNodes();
        verify(base).hasNodes();
        wrapper.hasProperties();
        verify(base).hasProperties();
        wrapper.hasProperty("title");
        verify(base).hasProperty("title");
        wrapper.canAddMixin("mix:versionable");
        verify(base).canAddMixin("mix:versionable");
        wrapper.holdsLock();
        verify(base).holdsLock();
        wrapper.isCheckedOut();
        verify(base).isCheckedOut();
        wrapper.isLocked();
        verify(base).isLocked();
        wrapper.isNodeType("mgnl:content");
        verify(base).isNodeType("mgnl:content");

        // Refresh/save/remove (no-op expected)
        wrapper.refresh(false);
        verify(base).refresh(false);
        wrapper.save();
        verify(base).save();
        wrapper.remove();
        verify(base).remove();

        // Ancestor traversal
        wrapper.getAncestor(0);
        verify(base).getAncestor(0);

        // Accept delegation (visitor may or may not be invoked depending on mock implementation; ensure no exception)
        ItemVisitor visitor = mock(ItemVisitor.class);
        wrapper.accept(visitor);
        verify(base).accept(visitor);
    }

    @Test
    public void delegatedOperationsExecuteOnNodeTestMetadata() throws Exception {
        Node base = mockNode("base", stubProperty("title", "underlying"));
        TestNullableDelegateNodeWrapper wrapper = new TestNullableDelegateNodeWrapper(base);
        assertTrue(wrapper.hasWrappedNode());
        // Read metadata delegation
        wrapper.getIndex();
        verify(base).getIndex();
        wrapper.getDepth();
        verify(base).getDepth();
        wrapper.getUUID();
        verify(base).getUUID();
        wrapper.getSession();
        verify(base, times(6)).getSession();
        wrapper.getParent();
        verify(base, times(16)).getParent();
    }

    @Test
    public void delegatedOperationsExecuteOnNodeTestStructuralOperations() throws Exception {
        Node base = mockNode("base", stubProperty("title", "underlying"));
        TestNullableDelegateNodeWrapper wrapper = new TestNullableDelegateNodeWrapper(base);
        assertTrue(wrapper.hasWrappedNode());
        // Structural operations (expected no-op or underlying logic)
        wrapper.orderBefore("a", "b");
        verify(base).orderBefore("a", "b");
        wrapper.lock(false, false);
        verify(base).lock(false, false);
        wrapper.unlock();
        verify(base).unlock();
        wrapper.update("ws");
        verify(base).update("ws");
        wrapper.checkout();
        verify(base).checkout();
        wrapper.checkin();
        verify(base).checkin();
    }

    @Test
    public void delegatedOperationsExecuteOnNodeTestVersioningOperations() throws Exception {
        Node base = mockNode("base", stubProperty("title", "underlying"));
        TestNullableDelegateNodeWrapper wrapper = new TestNullableDelegateNodeWrapper(base);
        assertTrue(wrapper.hasWrappedNode());
        // Version/merge related
        wrapper.cancelMerge(mock(Version.class));
        verify(base).cancelMerge(Mockito.<Version>any());
        wrapper.doneMerge(mock(Version.class));
        verify(base).doneMerge(Mockito.<Version>any());
        wrapper.merge("ws", true);
        verify(base).merge("ws", true);
        wrapper.restore("v1", true);
        verify(base).restore("v1", true);
        Version v = mock(Version.class);
        wrapper.restore(v, true);
        verify(base).restore(v, true);
        wrapper.restore(v, "rel", true);
        verify(base).restore(v, "rel", true);
        wrapper.restoreByLabel("lbl", true);
        verify(base).restoreByLabel("lbl", true);
    }

    @Test
    public void delegatedOperationsExecuteOnNodeTestLifecycleOperations() throws Exception {
        Node base = mockNode("base", stubProperty("title", "underlying"));
        TestNullableDelegateNodeWrapper wrapper = new TestNullableDelegateNodeWrapper(base);
        assertTrue(wrapper.hasWrappedNode());
        // Lifecycle/mixin
        wrapper.followLifecycleTransition("activate");
        verify(base).followLifecycleTransition("activate");
        wrapper.addMixin("mix:versionable");
        verify(base).addMixin("mix:versionable");
        wrapper.removeMixin("mix:versionable");
        verify(base).removeMixin("mix:versionable");
        wrapper.setPrimaryType("mgnl:page");
        verify(base).setPrimaryType("mgnl:page");
        wrapper.removeShare();
        verify(base).removeShare();
        wrapper.removeSharedSet();
        verify(base).removeSharedSet();
    }

    @Test
    public void delegatedOperationsExecuteOnNodeTestPropertyOverloads() throws Exception {
        Node base = mockNode("base", stubProperty("title", "underlying"));
        TestNullableDelegateNodeWrapper wrapper = new TestNullableDelegateNodeWrapper(base);
        assertTrue(wrapper.hasWrappedNode());
        // Property set overloads
        wrapper.setProperty("p1", "stringValue");
        verify(base).setProperty("p1", "stringValue");
        String[] values = new String[]{"a", "b"};
        wrapper.setProperty("p2", values);
        verify(base).setProperty("p2", values);
        wrapper.setProperty("p3", values, 1);
        verify(base).setProperty("p3", values, 1);
        wrapper.setProperty("p4", (Value) null);
        verify(base).setProperty("p4", (Value) null);
        wrapper.setProperty("p5", (Value[]) null);
        verify(base).setProperty("p5", (Value[]) null);
    }

    /**
     * Negative isSame comparison with a different underlying node ensures mismatch branch.
     */
    @Test
    public void isSameDifferentNodeFalse() throws Exception {
        Node n1 = mockNode("root/a", stubProperty("x", "1"));
        Node n2 = mockNode("root/b", stubProperty("y", "2"));
        TestNullableDelegateNodeWrapper w1 = new TestNullableDelegateNodeWrapper(n1);
        assertFalse(w1.isSame(n2));
    }

    /**
     * Minimal concrete subclass to allow instantiation of abstract base for tests.
     */
    private static class TestNullableDelegateNodeWrapper extends NullableDelegateNodeWrapper {
        protected TestNullableDelegateNodeWrapper(String name, String primaryType) {
            super(name, primaryType);
        }
        protected TestNullableDelegateNodeWrapper(Node node) {
            super(node);
        }
    }
}
