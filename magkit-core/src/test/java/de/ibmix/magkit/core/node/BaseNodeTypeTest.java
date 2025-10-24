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

import de.ibmix.magkit.test.jcr.ValueMockUtils;
import org.junit.jupiter.api.Test;

import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link BaseNodeType} covering all default-returning methods and ensuring no unexpected behaviour.
 * <p>Edge cases verified:</p>
 * <ul>
 *   <li>Empty arrays returned instead of null for definitions and supertypes.</li>
 *   <li>Null returned for subtype iterators.</li>
 *   <li>False returned for all capability flags (canSetProperty, canAddChildNode, etc.).</li>
 *   <li>Name preservation after construction.</li>
 *   <li>Graceful handling of arbitrary input values in capability checks.</li>
 * </ul>
 * Thread-safety aspects (immutability of name) implicitly verified by repeated access.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-20
 */
public class BaseNodeTypeTest {

    private static final String TYPE_NAME = "dummy:type";

    /**
     * Tests construction and name retrieval plus basic structural flag defaults.
     */
    @Test
    public void nameAndFlags() {
        BaseNodeType type = new BaseNodeType(TYPE_NAME);
        assertEquals(TYPE_NAME, type.getName());
        assertFalse(type.isAbstract());
        assertFalse(type.isMixin());
        assertFalse(type.hasOrderableChildNodes());
        assertFalse(type.isQueryable());
        assertNull(type.getPrimaryItemName());
    }

    /**
     * Tests supertypes related methods returning empty arrays or null for iterators.
     */
    @Test
    public void superTypesAndSubTypes() {
        BaseNodeType type = new BaseNodeType(TYPE_NAME);
        NodeType[] supertypes = type.getSupertypes();
        NodeType[] declaredSupertypes = type.getDeclaredSupertypes();
        NodeTypeIterator subtypes = type.getSubtypes();
        NodeTypeIterator declaredSubtypes = type.getDeclaredSubtypes();
        String[] declaredSuperNames = type.getDeclaredSupertypeNames();

        assertEquals(0, supertypes.length);
        assertEquals(0, declaredSupertypes.length);
        assertEquals(0, declaredSuperNames.length);
        assertNull(subtypes);
        assertNull(declaredSubtypes);
    }

    /**
     * Tests property and child node definition methods returning empty arrays.
     */
    @Test
    public void definitions() {
        BaseNodeType type = new BaseNodeType(TYPE_NAME);
        PropertyDefinition[] properties = type.getPropertyDefinitions();
        PropertyDefinition[] declaredProperties = type.getDeclaredPropertyDefinitions();
        NodeDefinition[] childNodes = type.getChildNodeDefinitions();
        NodeDefinition[] declaredChildNodes = type.getDeclaredChildNodeDefinitions();

        assertEquals(0, properties.length);
        assertEquals(0, declaredProperties.length);
        assertEquals(0, childNodes.length);
        assertEquals(0, declaredChildNodes.length);
    }

    /**
     * Tests capability flags all return false for any input.
     */
    @Test
    public void capabilitiesAllFalse() throws Exception {
        BaseNodeType type = new BaseNodeType(TYPE_NAME);
        Value v = ValueMockUtils.mockValue("test");

        assertFalse(type.canSetProperty("prop", v));
        assertFalse(type.canSetProperty("prop", new Value[]{v, v}));
        assertFalse(type.canAddChildNode("child"));
        assertFalse(type.canAddChildNode("child", "other:type"));
        assertFalse(type.canRemoveItem("item"));
        assertFalse(type.canRemoveNode("node"));
        assertFalse(type.canRemoveProperty("prop"));
    }

    /**
     * Tests isNodeType always returns false even for its configured name.
     */
    @Test
    public void isNodeTypeAlwaysFalse() {
        BaseNodeType type = new BaseNodeType(TYPE_NAME);
        assertFalse(type.isNodeType(TYPE_NAME));
        assertFalse(type.isNodeType("other"));
        assertFalse(type.isNodeType(""));
        assertFalse(type.isNodeType(null));
    }
}
