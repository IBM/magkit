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

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.version.Version;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Read-only wrapper around a real JCR {@link Node}. All mutating operations throw
 * {@link UnsupportedOperationException}. This is useful for exposing defensive node views to callers that must not
 * accidentally change repository state (e.g. templating / rendering layer).
 * <ul>
 *   <li>Rejects all write operations (setProperty, addNode, mixins, versioning, restore, save, remove).</li>
 *   <li>Delegates read operations to the wrapped node unchanged.</li>
 *   <li>Simple construction via public constructor taking a {@link Node}.</li>
 * </ul>
 * Usage example:
 * <pre>{@code
 * Node original = ...;
 * Node immutable = new ImmutableNodeWrapper(original);
 * Property p = immutable.getProperty("title"); // works
 * immutable.setProperty("title", "New") // throws UnsupportedOperationException
 * }</pre>
 * Null and error handling: Constructor requires a non-null wrapped node (validated by super-class). Repository
 * exceptions from delegated read operations propagate unchanged. Side effects: none, no writes. Thread-safety:
 * same as wrapped node (not enforced); wrapper itself is stateless and safe for concurrent reads.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-17
 */
public class ImmutableNodeWrapper extends NullableDelegateNodeWrapper {

    private static final String UNSUPPORTED_METHOD_MESSAGE = "Changing of states are not supported by this implementation.";

    /**
     * Create an immutable wrapper for a real JCR node.
     *
     * @param wrapped underlying node (must not be null)
     */
    public ImmutableNodeWrapper(final Node wrapped) {
        super(wrapped);
    }

    /**
     * Unsupported: adding child nodes is forbidden.
     *
     * @param relPath relative path of child to create
     * @return never returns (always throws)
     * @throws RepositoryException declared by interface
     */
    @Override
    public Node addNode(String relPath) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: adding child nodes with primary type is forbidden.
     *
     * @param relPath relative path
     * @param primaryNodeTypeName primary type name
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Node addNode(String relPath, String primaryNodeTypeName) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: reordering children is forbidden.
     *
     * @param srcChildRelPath source child relative path
     * @param destChildRelPath destination child relative path
     * @throws RepositoryException declared by interface
     */
    @Override
    public void orderBefore(String srcChildRelPath, String destChildRelPath) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting property.
     *
     * @param name property name
     * @param value property value
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, Value value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting property.
     *
     * @param name property name
     * @param value property value
     * @param type explicit property type
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, Value value, int type) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting multi valued property.
     *
     * @param name property name
     * @param values property values
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, Value[] values) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting multi valued property.
     *
     * @param name property name
     * @param values property values
     * @param type explicit property type
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, Value[] values, int type) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting multi valued String property.
     *
     * @param name property name
     * @param values string values
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, String[] values) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting multi valued String property with type.
     *
     * @param name property name
     * @param values string values
     * @param type explicit type
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, String[] values, int type) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting String property.
     *
     * @param name property name
     * @param value string value
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, String value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting String property with explicit type.
     *
     * @param name property name
     * @param value string value
     * @param type explicit type
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, String value, int type) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting binary via stream.
     *
     * @param name property name
     * @param value input stream
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, InputStream value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting binary value.
     *
     * @param name property name
     * @param value binary value
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, Binary value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting boolean property.
     *
     * @param name property name
     * @param value boolean value
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, boolean value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting double property.
     *
     * @param name property name
     * @param value double value
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, double value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting decimal property.
     *
     * @param name property name
     * @param value decimal value
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, BigDecimal value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting long property.
     *
     * @param name property name
     * @param value long value
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, long value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting calendar property.
     *
     * @param name property name
     * @param value calendar value
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, Calendar value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting reference property.
     *
     * @param name property name
     * @param value referenced node
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Property setProperty(String name, Node value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: setting primary type.
     *
     * @param nodeTypeName primary node type name
     * @throws RepositoryException declared by interface
     */
    @Override
    public void setPrimaryType(String nodeTypeName) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: adding mixin.
     *
     * @param mixinName mixin name
     * @throws RepositoryException declared by interface
     */
    @Override
    public void addMixin(String mixinName) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: removing mixin.
     *
     * @param mixinName mixin name
     * @throws RepositoryException declared by interface
     */
    @Override
    public void removeMixin(String mixinName) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: version checkin.
     *
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public Version checkin() throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: version checkout.
     *
     * @throws RepositoryException declared by interface
     */
    @Override
    public void checkout() throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: merge completion.
     *
     * @param version version
     * @throws RepositoryException declared by interface
     */
    @Override
    public void doneMerge(Version version) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: merge cancel.
     *
     * @param version version
     * @throws RepositoryException declared by interface
     */
    @Override
    public void cancelMerge(Version version) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: workspace update.
     *
     * @param srcWorkspace source workspace name
     * @throws RepositoryException declared by interface
     */
    @Override
    public void update(String srcWorkspace) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: node merge.
     *
     * @param srcWorkspace source workspace
     * @param bestEffort flag
     * @return never returns
     * @throws RepositoryException declared by interface
     */
    @Override
    public NodeIterator merge(String srcWorkspace, boolean bestEffort) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: restore by version name.
     *
     * @param versionName version name
     * @param removeExisting flag remove existing
     * @throws RepositoryException declared by interface
     */
    @Override
    public void restore(String versionName, boolean removeExisting) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: restore version.
     *
     * @param version version instance
     * @param removeExisting flag
     * @throws RepositoryException declared by interface
     */
    @Override
    public void restore(Version version, boolean removeExisting) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: restore version relative path.
     *
     * @param version version instance
     * @param relPath relative path
     * @param removeExisting flag
     * @throws RepositoryException declared by interface
     */
    @Override
    public void restore(Version version, String relPath, boolean removeExisting) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: restore by label.
     *
     * @param versionLabel label
     * @param removeExisting flag
     * @throws RepositoryException declared by interface
     */
    @Override
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: save node.
     *
     * @throws RepositoryException declared by interface
     */
    @Override
    public void save() throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: refresh node state.
     *
     * @param keepChanges keep changes flag
     * @throws RepositoryException declared by interface
     */
    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    /**
     * Unsupported: remove node.
     *
     * @throws RepositoryException declared by interface
     */
    @Override
    public void remove() throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }
}
