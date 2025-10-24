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

import de.ibmix.magkit.test.jcr.NodeStubbingOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import java.util.List;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockMgnlNode;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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

    @Test
    public void isValidValueTest() throws RepositoryException {
        Node node = mockMgnlNode("NodeName", "media", "mgnl:podcast", NodeStubbingOperation.stubIdentifier("NodeName"));
        when(node.getMixinNodeTypes()).thenReturn(new NodeType[0]);
        assertTrue(_nodeTypeValidator.isValidValue(node));
    }

    @Test
    public void isNotValidValueTest() throws RepositoryException {
        Node node = mockMgnlNode("NodeName", "media", "mgnl:video", NodeStubbingOperation.stubIdentifier("NodeName"));
        when(node.getMixinNodeTypes()).thenReturn(new NodeType[0]);
        assertFalse(_nodeTypeValidator.isValidValue(node));
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }
}
