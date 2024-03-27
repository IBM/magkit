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
 * A dummy NodeType to be used for null-save NodeWrappers.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-03-13
 */
public class BaseNodeType implements NodeType {

    private String _name;

    public BaseNodeType(String name) {
        _name = name;
    }

    @Override
    public NodeType[] getSupertypes() {
        return new NodeType[0];
    }

    @Override
    public NodeType[] getDeclaredSupertypes() {
        return new NodeType[0];
    }

    @Override
    public NodeTypeIterator getSubtypes() {
        return null;
    }

    @Override
    public NodeTypeIterator getDeclaredSubtypes() {
        return null;
    }

    @Override
    public boolean isNodeType(String nodeTypeName) {
        return false;
    }

    @Override
    public PropertyDefinition[] getPropertyDefinitions() {
        return new PropertyDefinition[0];
    }

    @Override
    public NodeDefinition[] getChildNodeDefinitions() {
        return new NodeDefinition[0];
    }

    @Override
    public boolean canSetProperty(String propertyName, Value value) {
        return false;
    }

    @Override
    public boolean canSetProperty(String propertyName, Value[] values) {
        return false;
    }

    @Override
    public boolean canAddChildNode(String childNodeName) {
        return false;
    }

    @Override
    public boolean canAddChildNode(String childNodeName, String nodeTypeName) {
        return false;
    }

    @Override
    public boolean canRemoveItem(String itemName) {
        return false;
    }

    @Override
    public boolean canRemoveNode(String nodeName) {
        return false;
    }

    @Override
    public boolean canRemoveProperty(String propertyName) {
        return false;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String[] getDeclaredSupertypeNames() {
        return new String[0];
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isMixin() {
        return false;
    }

    @Override
    public boolean hasOrderableChildNodes() {
        return false;
    }

    @Override
    public boolean isQueryable() {
        return false;
    }

    @Override
    public String getPrimaryItemName() {
        return null;
    }

    @Override
    public PropertyDefinition[] getDeclaredPropertyDefinitions() {
        return new PropertyDefinition[0];
    }

    @Override
    public NodeDefinition[] getDeclaredChildNodeDefinitions() {
        return new NodeDefinition[0];
    }
}
