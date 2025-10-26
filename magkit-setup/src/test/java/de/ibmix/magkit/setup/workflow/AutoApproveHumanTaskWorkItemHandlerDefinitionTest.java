package de.ibmix.magkit.setup.workflow;

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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link AutoApproveHumanTaskWorkItemHandlerDefinition} ensuring the constructor binds the implementation class
 * to {@link AutoApproveHumanTaskWorkItemHandler}. No side effects are expected and the instance should be immutable
 * after construction.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-26
 */
public class AutoApproveHumanTaskWorkItemHandlerDefinitionTest {

    /**
     * Verifies that the constructor sets the implementation class correctly.
     */
    @Test
    public void implementationClassIsSetToAutoApproveHandler() {
        AutoApproveHumanTaskWorkItemHandlerDefinition definition = new AutoApproveHumanTaskWorkItemHandlerDefinition();
        Class<?> implClass = definition.getImplementationClass();
        assertNotNull(implClass);
        assertEquals(AutoApproveHumanTaskWorkItemHandler.class, implClass);
    }
}

