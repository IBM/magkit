package de.ibmix.magkit.setup.nodebuilder.task;

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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.module.InstallContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.ibmix.magkit.test.cms.module.InstallContextStubbingOperation.stubCurrentModuleDefinition;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubName;
import static de.ibmix.magkit.test.cms.module.ModuleMockUtils.mockInstallContext;
import static de.ibmix.magkit.test.cms.module.ModuleMockUtils.mockModuleDefinition;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link TaskLogErrorHandler} verifying delegation of reported messages to {@link InstallContext#warn(String)}.
 * Ensures constructor stores the context reference and that null values are passed through without interception.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-27
 */
public class TaskLogErrorHandlerTest {

    private InstallContext _installContext;
    private TaskLogErrorHandler _handler;

    @BeforeEach
    public void setUp() {
        ContextMockUtils.cleanContext();
        _installContext = mockInstallContext(stubCurrentModuleDefinition(mockModuleDefinition(stubName("test-module"))));
        _handler = new TaskLogErrorHandler(_installContext);
    }

    /**
     * Delegates non-null message to InstallContext.warn.
     */
    @Test
    public void reportDelegatesWarn() {
        String message = "some warning";
        _handler.report(message);
        verify(_installContext).warn(message);
    }

    /**
     * Delegates null message unchanged to InstallContext.warn (no NullPointerException expected).
     */
    @Test
    public void reportDelegatesNullWarn() {
        _handler.report(null);
        verify(_installContext).warn(null);
    }

    /**
     * Delegates empty message to InstallContext.warn.
     */
    @Test
    public void reportDelegatesEmptyWarn() {
        String empty = "";
        _handler.report(empty);
        verify(_installContext).warn(empty);
    }

    /**
     * Throws NullPointerException when constructed with null InstallContext and report is invoked.
     */
    @Test
    public void reportThrowsOnNullContext() {
        TaskLogErrorHandler nullHandler = new TaskLogErrorHandler(null);
        assertThrows(NullPointerException.class, () -> nullHandler.report("x"));
    }
}
