package de.ibmix.magkit.ui.dialogs.validators;

/*-
 * #%L
 * IBM iX Magnolia Kit UI
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import java.util.List;
import java.util.Collections;

import com.vaadin.data.ValidationResult;
import com.vaadin.data.ValueContext;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockMgnlNode;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

/**
 * Unit tests for {@link NodeTypeValidator}.
 * @author ngoc.tran
 * @since 2024-03-22
 */
public class NodeTypeValidatorTest {
    private NodeTypeValidator _nodeTypeValidator;

    @BeforeEach
    public void setUp() {
        NodeTypeValidatorDefinition definition = new NodeTypeValidatorDefinition();
        definition.setErrorMessage("Is not valid!");
        definition.setAcceptedNodeTypes(List.of("mgnl:podcast"));
        _nodeTypeValidator = new NodeTypeValidator(definition);
    }

    /**
     * Valid when primary type matches single accepted type.
     */
    @Test
    public void isValidValueTest() throws RepositoryException {
        Node node = mockMgnlNode("NodeName", "media", "mgnl:podcast");
        when(node.getMixinNodeTypes()).thenReturn(new NodeType[0]);
        assertTrue(_nodeTypeValidator.isValidValue(node));
    }

    /**
     * Invalid when primary type does not match accepted list.
     */
    @Test
    public void isNotValidValueTest() throws RepositoryException {
        Node node = mockMgnlNode("NodeName", "media", "mgnl:video");
        when(node.getMixinNodeTypes()).thenReturn(new NodeType[0]);
        assertFalse(_nodeTypeValidator.isValidValue(node));
    }

    /**
     * Valid when node is null (validator treats absence as valid).
     */
    @Test
    public void isValidWhenNodeNull() {
        assertTrue(_nodeTypeValidator.isValidValue(null));
    }

    /**
     * Valid when accepted node type list is empty.
     */
    @Test
    public void isValidWhenAcceptedListEmpty() throws RepositoryException {
        NodeTypeValidatorDefinition definition = new NodeTypeValidatorDefinition();
        definition.setErrorMessage("Is not valid!");
        definition.setAcceptedNodeTypes(Collections.emptyList());
        NodeTypeValidator emptyListValidator = new NodeTypeValidator(definition);
        Node node = mockMgnlNode("NodeName", "media", "mgnl:video");
        assertTrue(emptyListValidator.isValidValue(node));
    }

    /**
     * Valid when accepted list is null (treated as empty by CollectionUtils.isEmpty).
     */
    @Test
    public void isValidWhenAcceptedListNull() throws RepositoryException {
        NodeTypeValidatorDefinition definition = new NodeTypeValidatorDefinition();
        definition.setErrorMessage("Is not valid!");
        NodeTypeValidator nullListValidator = new NodeTypeValidator(definition);
        Node node = mockMgnlNode("NodeName", "media", "mgnl:any");
        assertTrue(nullListValidator.isValidValue(node));
    }

    /**
     * Valid when second accepted type matches after first throws RepositoryException.
     */
    @Test
    public void isValidWhenFirstTypeThrowsSecondMatches() throws RepositoryException {
        Node node = mockMgnlNode("NodeName", "media", "mgnl:podcast");
        NodeTypeValidatorDefinition definition = new NodeTypeValidatorDefinition();
        definition.setErrorMessage("Is not valid!");
        definition.setAcceptedNodeTypes(List.of("throwType", "mgnl:podcast"));
        NodeTypeValidator multiValidator = new NodeTypeValidator(definition);
        doThrow(new RepositoryException("failure")).when(node).isNodeType("throwType");
        assertTrue(multiValidator.isValidValue(node));
    }

    /**
     * Valid apply result when node matches accepted type.
     */
    @Test
    public void applyValidNode() throws RepositoryException {
        Node node = mockMgnlNode("NodeName", "media", "mgnl:podcast");
        ValidationResult result = _nodeTypeValidator.apply(node, new ValueContext());
        assertFalse(result.isError());
    }

    /**
     * Error apply result when node does not match accepted type.
     */
    @Test
    public void applyInvalidNode() throws RepositoryException {
        Node node = mockMgnlNode("NodeName", "media", "mgnl:video");
        ValidationResult result = _nodeTypeValidator.apply(node, new ValueContext());
        assertTrue(result.isError());
        assertEquals("Is not valid!", result.getErrorMessage());
    }

    /**
     * Valid apply result when value is null.
     */
    @Test
    public void applyNullNode() {
        ValidationResult result = _nodeTypeValidator.apply(null, new ValueContext());
        assertFalse(result.isError());
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }
}
