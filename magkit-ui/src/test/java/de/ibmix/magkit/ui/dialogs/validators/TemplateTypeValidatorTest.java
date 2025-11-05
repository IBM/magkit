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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.List;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.PageNodeStubbingOperation.stubTemplate;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubType;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TemplateTypeValidator} covering null handling, wildcard acceptance and matching logic.
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-31
 */
public class TemplateTypeValidatorTest {

    @AfterEach
    public void tearDown() {
        cleanContext();
    }

    @Test
    public void isNotValidValueNullNode() {
        TemplateTypeValidatorDefinition definition = new TemplateTypeValidatorDefinition();
        definition.setErrorMessage("Is not valid!");
        TemplateTypeValidator validator = new TemplateTypeValidator(definition);
        assertFalse(validator.isValidValue(null));
    }

    @Test
    public void isValidWildcardAcceptedValuesWithoutTemplateType() throws RepositoryException {
        TemplateTypeValidatorDefinition definition = new TemplateTypeValidatorDefinition();
        definition.setErrorMessage("Is not valid!");
        TemplateTypeValidator validator = new TemplateTypeValidator(definition);
        Node node = mockNode("node");
        assertTrue(validator.isValidValue(node));
    }

    @Test
    public void isValidWildcardAcceptedValuesWithTemplateType() throws RepositoryException {
        TemplateTypeValidatorDefinition definition = new TemplateTypeValidatorDefinition();
        definition.setErrorMessage("Is not valid!");
        TemplateTypeValidator validator = new TemplateTypeValidator(definition);
        Node node = mockNode("nodeWildcard", stubTemplate("test:template", stubType("anyType")));
        assertTrue(validator.isValidValue(node));
    }

    @Test
    public void isNotValidValueWithoutTemplateType() throws RepositoryException {
        TemplateTypeValidatorDefinition definition = new TemplateTypeValidatorDefinition();
        definition.setErrorMessage("Is not valid!");
        definition.setAcceptedValues(List.of("allowedType"));
        TemplateTypeValidator validator = new TemplateTypeValidator(definition);
        Node node = mockNode("node");
        assertFalse(validator.isValidValue(node));
    }

    @Test
    public void isNotValidValueWrongTemplateType() throws RepositoryException {
        TemplateTypeValidatorDefinition definition = new TemplateTypeValidatorDefinition();
        definition.setErrorMessage("Is not valid!");
        definition.setAcceptedValues(List.of("allowedType"));
        TemplateTypeValidator validator = new TemplateTypeValidator(definition);
        Node node = mockNode("node", stubTemplate("test:template", stubType("otherType")));
        assertFalse(validator.isValidValue(node));
    }

    @Test
    public void isValidValueAcceptedTemplateType() throws RepositoryException {
        TemplateTypeValidatorDefinition definition = new TemplateTypeValidatorDefinition();
        definition.setErrorMessage("Is not valid!");
        definition.setAcceptedValues(List.of("allowedType"));
        TemplateTypeValidator validator = new TemplateTypeValidator(definition);
        Node node = mockNode("node", stubTemplate("test:template", stubType("allowedType")));
        assertTrue(validator.isValidValue(node));
    }

    @Test
    public void applyValidationResults() throws RepositoryException {
        TemplateTypeValidatorDefinition definition = new TemplateTypeValidatorDefinition();
        definition.setErrorMessage("Is not valid!");
        definition.setAcceptedValues(List.of("allowedType"));
        TemplateTypeValidator validator = new TemplateTypeValidator(definition);
        ValidationResult nullResult = validator.apply(null, new ValueContext());
        assertTrue(nullResult.isError());
        Node node = mockNode("node", stubTemplate("test:template", stubType("otherType")));
        ValidationResult wrongTypeResult = validator.apply(node, new ValueContext());
        assertTrue(wrongTypeResult.isError());
        stubTemplate("test:template2", stubType("allowedType")).of(node);
        ValidationResult okResult = validator.apply(node, new ValueContext());
        assertFalse(okResult.isError());
    }
}
