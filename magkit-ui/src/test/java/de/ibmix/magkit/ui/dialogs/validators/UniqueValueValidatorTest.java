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

import com.vaadin.data.ValidationResult;
import com.vaadin.data.ValueContext;
import info.magnolia.jcr.util.NodeTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.Optional;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQuery;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.test.jcr.query.QueryStubbingOperation.stubResult;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UniqueValueValidator} covering configuration and result evaluation branches.
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-31
 */
public class UniqueValueValidatorTest {

    @BeforeEach
    public void setUp() {
        cleanContext();
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }

    @Test
    public void missingConfiguration() {
        UniqueValueValidatorDefinition definition = new UniqueValueValidatorDefinition();
        definition.setErrorMessage("error message");
        info.magnolia.ui.ValueContext<Item> itemContext = mock(info.magnolia.ui.ValueContext.class);
        when(itemContext.getSingle()).thenReturn(Optional.empty());
        UniqueValueValidator validator = new UniqueValueValidator(definition, itemContext);
        ValidationResult result = validator.apply("Any", new ValueContext());
        assertTrue(result.isError());
    }

    @Test
    public void uniqueValueNoResult() throws RepositoryException {
        String workspace = "test";
        String nodeType = NodeTypes.Content.NAME;
        String propertyName = "title";
        String value = "Some Title";
        UniqueValueValidatorDefinition definition = getValidatorDefinition(workspace, nodeType, propertyName);
        Node currentNode = mockNode(workspace, "/current", stubIdentifier("id-1"));
        info.magnolia.ui.ValueContext<Item> itemContext = mock(info.magnolia.ui.ValueContext.class);
        when(itemContext.getSingle()).thenReturn(Optional.of(currentNode));
        String expectedQuery = "SELECT * FROM [" + nodeType + "] WHERE [" + propertyName + "] = '" + value + "' ORDER BY [mgnl:lastModified] DESC";
        mockQuery(workspace, Query.JCR_SQL2, expectedQuery, stubResult());
        UniqueValueValidator validator = new UniqueValueValidator(definition, itemContext);
        ValidationResult result = validator.apply(value, new ValueContext());
        assertFalse(result.isError());
    }

    @Test
    public void uniqueValueSingleResultSelf() throws RepositoryException {
        String workspace = "test";
        String nodeType = NodeTypes.Content.NAME;
        String propertyName = "title";
        String value = "Some Title";
        UniqueValueValidatorDefinition definition = getValidatorDefinition(workspace, nodeType, propertyName);
        Node currentNode = mockNode(workspace, "/current", stubIdentifier("id-1"));
        info.magnolia.ui.ValueContext<Item> itemContext = mock(info.magnolia.ui.ValueContext.class);
        when(itemContext.getSingle()).thenReturn(Optional.of(currentNode));
        Node foundNode = mockNode(workspace, "/found", stubIdentifier("id-1"));
        String expectedQuery = "SELECT * FROM [" + nodeType + "] WHERE [" + propertyName + "] = '" + value + "' ORDER BY [mgnl:lastModified] DESC";
        mockQuery(workspace, Query.JCR_SQL2, expectedQuery, stubResult(foundNode));
        UniqueValueValidator validator = new UniqueValueValidator(definition, itemContext);
        ValidationResult result = validator.apply(value, new ValueContext());
        assertFalse(result.isError());
    }


    @Test
    public void duplicateDifferentNode() throws RepositoryException {
        String workspace = "test";
        String nodeType = NodeTypes.Content.NAME;
        String propertyName = "title";
        String value = "Some Title";
        UniqueValueValidatorDefinition definition = getValidatorDefinition(workspace, nodeType, propertyName);
        Node currentNode = mockNode(workspace, "/current", stubIdentifier("id-1"));
        info.magnolia.ui.ValueContext<Item> itemContext = mock(info.magnolia.ui.ValueContext.class);
        when(itemContext.getSingle()).thenReturn(Optional.of(currentNode));
        Node foundNode = mockNode(workspace, "/found", stubIdentifier("id-2"));
        String expectedQuery = "SELECT * FROM [" + nodeType + "] WHERE [" + propertyName + "] = '" + value + "' ORDER BY [mgnl:lastModified] DESC";
        mockQuery(workspace, Query.JCR_SQL2, expectedQuery, stubResult(foundNode));
        UniqueValueValidator validator = new UniqueValueValidator(definition, itemContext);
        ValidationResult result = validator.apply(value, new ValueContext());
        assertTrue(result.isError());
    }

    @Test
    public void duplicateMultipleNodes() throws RepositoryException {
        String workspace = "test";
        String nodeType = NodeTypes.Content.NAME;
        String propertyName = "title";
        String value = "Some Title";
        UniqueValueValidatorDefinition definition = getValidatorDefinition(workspace, nodeType, propertyName);
        Node currentNode = mockNode(workspace, "/current", stubIdentifier("id-1"));
        info.magnolia.ui.ValueContext<Item> itemContext = mock(info.magnolia.ui.ValueContext.class);
        when(itemContext.getSingle()).thenReturn(Optional.of(currentNode));
        Node foundNode1 = mockNode(workspace, "/found1", stubIdentifier("id-2"));
        Node foundNode2 = mockNode(workspace, "/found2", stubIdentifier("id-3"));
        String expectedQuery = "SELECT * FROM [" + nodeType + "] WHERE [" + propertyName + "] = '" + value + "' ORDER BY [mgnl:lastModified] DESC";
        mockQuery(workspace, Query.JCR_SQL2, expectedQuery, stubResult(foundNode1, foundNode2));
        UniqueValueValidator validator = new UniqueValueValidator(definition, itemContext);
        ValidationResult result = validator.apply(value, new ValueContext());
        assertTrue(result.isError());
    }

    @Test
    public void noCurrentNodeNoFoundNodes() throws RepositoryException {
        String workspace = "test";
        String nodeType = NodeTypes.Content.NAME;
        String propertyName = "title";
        String value = "Some Title";
        UniqueValueValidatorDefinition definition = getValidatorDefinition(workspace, nodeType, propertyName);
        info.magnolia.ui.ValueContext<Item> itemContext = mock(info.magnolia.ui.ValueContext.class);
        when(itemContext.getSingle()).thenReturn(Optional.empty());
        String expectedQuery = "SELECT * FROM [" + nodeType + "] WHERE [" + propertyName + "] = '" + value + "' ORDER BY [mgnl:lastModified] DESC";
        mockQuery(workspace, Query.JCR_SQL2, expectedQuery, stubResult());
        UniqueValueValidator validator = new UniqueValueValidator(definition, itemContext);
        ValidationResult result = validator.apply(value, new ValueContext());
        assertFalse(result.isError());
    }

    @Test
    public void noCurrentNodeSingleFoundNode() throws RepositoryException {
        String workspace = "test";
        String nodeType = NodeTypes.Content.NAME;
        String propertyName = "title";
        String value = "Some Title";
        UniqueValueValidatorDefinition definition = getValidatorDefinition(workspace, nodeType, propertyName);
        info.magnolia.ui.ValueContext<Item> itemContext = mock(info.magnolia.ui.ValueContext.class);
        when(itemContext.getSingle()).thenReturn(Optional.empty());
        Node foundNode = mockNode(workspace, "/found", stubIdentifier("id-2"));
        String expectedQuery = "SELECT * FROM [" + nodeType + "] WHERE [" + propertyName + "] = '" + value + "' ORDER BY [mgnl:lastModified] DESC";
        mockQuery(workspace, Query.JCR_SQL2, expectedQuery, stubResult(foundNode));
        UniqueValueValidator validator = new UniqueValueValidator(definition, itemContext);
        ValidationResult result = validator.apply(value, new ValueContext());
        assertTrue(result.isError());
    }

    private static UniqueValueValidatorDefinition getValidatorDefinition(String workspace, String nodeType, String propertyName) {
        UniqueValueValidatorDefinition definition = new UniqueValueValidatorDefinition();
        definition.setErrorMessage("error message");
        definition.setWorkspace(workspace);
        definition.setNodeType(nodeType);
        definition.setPropertyName(propertyName);
        return definition;
    }
}
