package de.ibmix.magkit.core.node;

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

import info.magnolia.jcr.wrapper.DelegateNodeWrapper;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.PropertyIteratorAdapter;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.ActivityViolationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Abstract base for Node wrappers that MAY wrap a real JCR {@link Node} but can also operate without one (nullable).
 * It centralises defensive null checking before delegating to the wrapped node and supplies safe defaults when
 * no underlying node exists. This enables creation of transient in-memory node graphs for view/model logic without
 * persisting or requiring repository state.
 * <ul>
 *   <li>Graceful degradation: All read operations return neutral defaults (null, empty iterator, empty strings, 0 index)</li>
 *   <li>Write operations simply no-op when no wrapped node exists (unless overridden by subclasses)</li>
 *   <li>Hierarchy metadata (name, type) can be provided synthetically via constructor</li>
 *   <li>Extensible: concrete subclasses add overlay/fallback/immutability behaviour</li>
 * </ul>
 * Usage example:
 * <pre>{@code
 * NullableDelegateNodeWrapper synthetic = new NullableDelegateNodeWrapper("virtual", "mgnl:content") {
 *     // implement additional behaviour here
 * };
 * String name = synthetic.getName(); // "virtual"
 * NodeType type = synthetic.getPrimaryNodeType(); // BaseNodeType("mgnl:content")
 * }</pre>
 * Null & error handling: If a wrapped node exists all repository exceptions propagate unchanged. Without a wrapped
 * node, methods return neutral defaults and do not throw (unless contract requires otherwise, e.g. validation inside
 * constructors). Thread-safety: Not thread-safe; instances should be confined to request scope. Side effects: None –
 * calls never modify repository state when underlying node is absent.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-03-15
 */
public abstract class NullableDelegateNodeWrapper extends DelegateNodeWrapper {

    private String _name;
    private NodeType _primaryNodeType;

    /**
     * Construct a purely synthetic wrapper without a backing node. Name and primary node type are mandatory so that
     * hierarchy and type related JCR API calls can respond safely.
     *
     * @param name synthetic node name (must not be empty)
     * @param primaryNodeType technical name of the primary node type (must not be empty)
     */
    protected NullableDelegateNodeWrapper(String name, String primaryNodeType) {
        super();
        notEmpty(name);
        notEmpty(primaryNodeType);
        _name = name;
        _primaryNodeType = new BaseNodeType(primaryNodeType);
    }

    /**
     * Construct wrapper for a real JCR node. All operations will delegate to this node.
     *
     * @param node real node to wrap (must not be null)
     */
    protected NullableDelegateNodeWrapper(Node node) {
        super(node);
        notNull(node);
    }
    
    /**
     * Indicates whether a real wrapped node exists. Useful for subclasses deciding delegation or fallback logic.
     *
     * @return true if a non-null wrapped node is present
     */
    public boolean hasWrappedNode() {
        return getWrappedNode() != null;
    }


    @Override
    public String toString() {
        return hasWrappedNode() ? getWrappedNode().toString() : "";
    }

    /////////////
    //
    //  Delegating method stubs
    //
    /////////////

    @Override
    public void addMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().addMixin(mixinName);
        }
    }

    @Override
    public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().addNode(relPath) : null;
    }

    @Override
    public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().addNode(relPath, primaryNodeTypeName) : null;
    }

    @Override
    public boolean canAddMixin(String mixinName) throws NoSuchNodeTypeException, RepositoryException {
        return hasWrappedNode() && getWrappedNode().canAddMixin(mixinName);
    }

    @Override
    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().cancelMerge(version);
        }
    }

    @Override
    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().checkin() : null;
    }

    @Override
    public void checkout() throws UnsupportedRepositoryOperationException, LockException, ActivityViolationException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().checkout();
        }
    }

    @Override
    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().doneMerge(version);
        }
    }

    @Override
    public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().followLifecycleTransition(transition);
        }   
    }

    @Override
    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getAllowedLifecycleTransistions() : null;
    }

    @Override
    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getBaseVersion() : null;
    }

    @Override
    public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getCorrespondingNodePath(workspaceName) : null;
    }

    @Override
    public NodeDefinition getDefinition() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getDefinition() : null;
    }

    @Override
    public String getIdentifier() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getIdentifier() : EMPTY;
    }

    @Override
    public int getIndex() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getIndex() : 0;
    }

    @Override
    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getLock() : null;
    }

    @Override
    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getMixinNodeTypes() : null;
    }

    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getNode(relPath) : null;
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getNodes() : new NodeIteratorAdapter(Collections.emptyList());
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getNodes(namePattern) : new NodeIteratorAdapter(Collections.emptyList());
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getNodes(nameGlobs) : new NodeIteratorAdapter(Collections.emptyList());
    }

    @Override
    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getPrimaryItem() : null;
    }

    @Override
    public NodeType getPrimaryNodeType() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getPrimaryNodeType() : _primaryNodeType;
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getProperties() : new PropertyIteratorAdapter(Collections.emptyList());
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getProperties(namePattern) : new PropertyIteratorAdapter(Collections.emptyList());
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getProperties(nameGlobs) : new PropertyIteratorAdapter(Collections.emptyList());
    }

    @Override
    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getProperty(relPath) : null;
    }

    @Override
    public PropertyIterator getReferences() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getReferences() : new PropertyIteratorAdapter(Collections.emptyList());
    }

    @Override
    public PropertyIterator getReferences(String name) throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getReferences(name) : new PropertyIteratorAdapter(Collections.emptyList());
    }

    @Override
    public NodeIterator getSharedSet() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getSharedSet() : new NodeIteratorAdapter(Collections.emptyList());
    }

    @Override
    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getUUID() : EMPTY;
    }

    @Override
    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getVersionHistory() : null;
    }

    @Override
    public PropertyIterator getWeakReferences() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getWeakReferences() : new PropertyIteratorAdapter(Collections.emptyList());
    }

    @Override
    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getWeakReferences(name) : new PropertyIteratorAdapter(Collections.emptyList());
    }

    @Override
    public boolean hasNode(String relPath) throws RepositoryException {
        return hasWrappedNode() && getWrappedNode().hasNode(relPath);
    }

    @Override
    public boolean hasNodes() throws RepositoryException {
        return hasWrappedNode() && getWrappedNode().hasNodes();
    }

    @Override
    public boolean hasProperties() throws RepositoryException {
        return hasWrappedNode() && getWrappedNode().hasProperties();
    }

    @Override
    public boolean hasProperty(String relPath) throws RepositoryException {
        return hasWrappedNode() && getWrappedNode().hasProperty(relPath);
    }

    @Override
    public boolean holdsLock() throws RepositoryException {
        return hasWrappedNode() && getWrappedNode().holdsLock();
    }

    @Override
    public boolean isCheckedOut() throws RepositoryException {
        return hasWrappedNode() && getWrappedNode().isCheckedOut();
    }

    @Override
    public boolean isLocked() throws RepositoryException {
        return hasWrappedNode() && getWrappedNode().isLocked();
    }

    @Override
    public boolean isNodeType(String nodeTypeName) throws RepositoryException {
        return hasWrappedNode() && getWrappedNode().isNodeType(nodeTypeName);
    }

    @Override
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().lock(isDeep, isSessionScoped) : null;
    }

    @Override
    public NodeIterator merge(String srcWorkspace, boolean bestEffort) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().merge(srcWorkspace, bestEffort) : new NodeIteratorAdapter(Collections.emptyList());
    }

    @Override
    public void orderBefore(String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().orderBefore(srcChildRelPath, destChildRelPath);
        }
    }

    @Override
    public void removeMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().removeMixin(mixinName);
        }
    }

    @Override
    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().removeShare();
        }
    }

    @Override
    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().removeSharedSet();
        }
    }

    @Override
    public void restore(String versionName, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().restore(versionName, removeExisting);
        }
    }

    @Override
    public void restore(Version version, boolean removeExisting) throws VersionException, ItemExistsException, InvalidItemStateException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().restore(version, removeExisting);
        }
    }

    @Override
    public void restore(Version version, String relPath, boolean removeExisting) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().restore(version, relPath, removeExisting);
        }
    }

    @Override
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().restoreByLabel(versionLabel, removeExisting);
        }
    }

    @Override
    public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().setPrimaryType(nodeTypeName);
        }
    }

    @Override
    public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, value) : null;
    }

    @Override
    public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, values) : null;
    }

    @Override
    public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, values) : null;
    }

    @Override
    public Property setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, value) : null;
    }

    @Override
    public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, value) : null;
    }

    @Override
    public Property setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, value) : null;
    }

    @Override
    public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, value) : null;
    }

    @Override
    public Property setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, value) : null;
    }

    @Override
    public Property setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, value) : null;
    }

    @Override
    public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, value) : null;
    }

    @Override
    public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, value) : null;
    }

    @Override
    public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, value) : null;
    }

    @Override
    public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, value, type) : null;
    }

    @Override
    public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, values, type) : null;
    }

    @Override
    public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, values, type) : null;
    }

    @Override
    public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().setProperty(name, value, type) : null;
    }

    @Override
    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().unlock();
        }
    }

    @Override
    public void update(String srcWorkspace) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().update(srcWorkspace);
        }
    }

    @Override
    public void accept(ItemVisitor visitor) throws RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().accept(visitor);
        }
    }

    @Override
    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getAncestor(depth) : null;
    }

    @Override
    public int getDepth() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getDepth() : 0;
    }

    @Override
    public String getName() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getName() : _name;
    }

    @Override
    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getParent() : null;
    }

    @Override
    public String getPath() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getPath() : '/' + _name;
    }

    @Override
    public Session getSession() throws RepositoryException {
        return hasWrappedNode() ? getWrappedNode().getSession() : null;
    }

    @Override
    public boolean isModified() {
        return hasWrappedNode() && getWrappedNode().isModified();
    }

    @Override
    public boolean isNew() {
        return hasWrappedNode() && getWrappedNode().isNew();
    }

    @Override
    public boolean isNode() {
        return hasWrappedNode() && getWrappedNode().isNode();
    }

    @Override
    public boolean isSame(Item otherItem) throws RepositoryException {
        boolean result = false;
        if (this == otherItem) {
            result = true;
        } else if (otherItem instanceof Node) {
            result = hasWrappedNode()
                && getIdentifier().equals(((Node) otherItem).getIdentifier())
                && getSession().getWorkspace().getName().equals(otherItem.getSession().getWorkspace().getName());
        }
        return result;
    }

    @Override
    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().refresh(keepChanges);
        }
    }

    @Override
    public void remove() throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().remove();
        }
    }

    @Override
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        if (hasWrappedNode()) {
            getWrappedNode().save();
        }
    }
}
