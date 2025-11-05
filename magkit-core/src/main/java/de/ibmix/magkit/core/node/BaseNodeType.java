package de.ibmix.magkit.core.node;

/*-
 * #%L
 * magkit-core
 * %%
 * Copyright (C) 2023 - 2024 IBM iX
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

import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;

/**
 * Minimal dummy {@link NodeType} implementation used by {@link NullableDelegateNodeWrapper} when no real
 * underlying node instance exists. Its main purpose is to supply non-null, predictable defaults so wrapper
 * instances can safely answer JCR API calls without throwing {@link NullPointerException}.
 * <ul>
 *   <li>All capability checks (canSetProperty, canAddChildNode, etc.) return false.</li>
 *   <li>Definition arrays are empty, not null.</li>
 *   <li>Queryability and structural flags return conservative defaults (false).</li>
 * </ul>
 * Usage preconditions: Construct with a non-empty name. Suitable only for transient/in-memory wrapper scenarios
 * where full node type metadata is unnecessary.
 * Thread-safety: Immutable after construction (name field not reassigned). Safe for concurrent reads.
 * Null and error handling: Methods never throw repository-related exceptions; always return neutral defaults (false,
 * empty arrays, null for primary item name and subtype iterators).
 * Side effects: None – purely value object used for answering wrapper metadata queries.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-03-13
 */
public class BaseNodeType implements NodeType {

    private String _name;

    /**
     * Create a dummy node type with the provided name.
     *
     * @param name technical name returned by {@link #getName()}
     */
    public BaseNodeType(String name) {
        _name = name;
    }

    /** @return empty array (no supertypes) */
    @Override
    public NodeType[] getSupertypes() {
        return new NodeType[0];
    }

    /** @return empty array (no declared supertypes) */
    @Override
    public NodeType[] getDeclaredSupertypes() {
        return new NodeType[0];
    }

    /** @return null (no subtype iteration supported) */
    @Override
    public NodeTypeIterator getSubtypes() {
        return null;
    }

    /** @return null (no declared subtype iteration supported) */
    @Override
    public NodeTypeIterator getDeclaredSubtypes() {
        return null;
    }

    /** Always returns false. */
    @Override
    public boolean isNodeType(String nodeTypeName) {
        return false;
    }

    /** @return empty array */
    @Override
    public PropertyDefinition[] getPropertyDefinitions() {
        return new PropertyDefinition[0];
    }

    /** @return empty array */
    @Override
    public NodeDefinition[] getChildNodeDefinitions() {
        return new NodeDefinition[0];
    }

    /** Always returns false. */
    @Override
    public boolean canSetProperty(String propertyName, Value value) {
        return false;
    }

    /** Always returns false. */
    @Override
    public boolean canSetProperty(String propertyName, Value[] values) {
        return false;
    }

    /** Always returns false. */
    @Override
    public boolean canAddChildNode(String childNodeName) {
        return false;
    }

    /** Always returns false. */
    @Override
    public boolean canAddChildNode(String childNodeName, String nodeTypeName) {
        return false;
    }

    /** Always returns false. */
    @Override
    public boolean canRemoveItem(String itemName) {
        return false;
    }

    /** Always returns false. */
    @Override
    public boolean canRemoveNode(String nodeName) {
        return false;
    }

    /** Always returns false. */
    @Override
    public boolean canRemoveProperty(String propertyName) {
        return false;
    }

    /** @return configured name */
    @Override
    public String getName() {
        return _name;
    }

    /** @return empty array */
    @Override
    public String[] getDeclaredSupertypeNames() {
        return new String[0];
    }

    /** @return false */
    @Override
    public boolean isAbstract() {
        return false;
    }

    /** @return false */
    @Override
    public boolean isMixin() {
        return false;
    }

    /** @return false */
    @Override
    public boolean hasOrderableChildNodes() {
        return false;
    }

    /** @return false */
    @Override
    public boolean isQueryable() {
        return false;
    }

    /** @return null (no primary item) */
    @Override
    public String getPrimaryItemName() {
        return null;
    }

    /** @return empty array */
    @Override
    public PropertyDefinition[] getDeclaredPropertyDefinitions() {
        return new PropertyDefinition[0];
    }

    /** @return empty array */
    @Override
    public NodeDefinition[] getDeclaredChildNodeDefinitions() {
        return new NodeDefinition[0];
    }
}
