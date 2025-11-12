package de.ibmix.magkit.setup.delta;

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
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.jcr.util.NodeNameHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StandardTasks} covering task creation branches and version classifier update logic conditions.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-28
 */
public class StandardTasksTest {

    private NodeNameHelper _nodeNameHelper;

    @BeforeEach
    public void setUp() {
        ContextMockUtils.cleanContext();
        _nodeNameHelper = mockComponentInstance(NodeNameHelper.class);
        when(_nodeNameHelper.getValidatedName(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * Creates simple workflow task sequence and verifies non-null composite task instance.
     */
    @Test
    public void setSimpleWorkflowCreatesCompositeTask() {
        Task task = StandardTasks.setSimpleWorkflow();
        assertNotNull(task);
    }

    /**
     * Creates monitoring bypass task (URI starts with slash) and verifies non-null task instance for path branch.
     */
    @Test
    public void addBypassForMonitoringCreatesUriBypassTask() {
        Task task = StandardTasks.addBypassForMonitoring();
        assertNotNull(task);
    }

    /**
     * Creates bypass for URI pattern starting with slash and verifies non-null task instance and validated node name.
     */
    @ParameterizedTest
    @CsvSource({
        "/assets, /server/filters, assets",
        "/monitoring/, /server/filters, monitoring",
        "css, /server/filters, css"
    })
    public void addFilteringBypassIfMissing(String uriPattern, String filterPath, String expectedValidatedName) {
        Task task = StandardTasks.addFilteringBypassIfMissing(uriPattern, filterPath);
        assertNotNull(task);
        verify(_nodeNameHelper).getValidatedName(expectedValidatedName);
    }

    /**
     * Creates security callback adjustment task and verifies non-null instance.
     */
    @Test
    public void setSecurityCallbackCreatesTask() {
        Task task = StandardTasks.setSecurityCallback();
        assertNotNull(task);
    }

    /**
     * Returns true when moving from classifier version to released version.
     */
    @Test
    public void hasModuleNewRevisionReturnsTrueOnReleasedVersion() {
        Version fromVersion = versionMock("SNAPSHOT");
        Version toVersion = versionMock(null);
        when(toVersion.isEquivalent(fromVersion)).thenReturn(true);
        assertTrue(StandardTasks.hasModuleNewRevision(fromVersion, toVersion));
    }

    /**
     * Returns true when classifier changes to a different classifier.
     */
    @Test
    public void hasModuleNewRevisionReturnsTrueOnClassifierChange() {
        Version fromVersion = versionMock("BETA");
        Version toVersion = versionMock("RC1");
        when(toVersion.isEquivalent(fromVersion)).thenReturn(true);
        assertTrue(StandardTasks.hasModuleNewRevision(fromVersion, toVersion));
    }

    /**
     * Returns false when versions equivalent and both have no classifier.
     */
    @ParameterizedTest
    @CsvSource({
        ", , true",
        ", ALPHA, true",
        "SNAPSHOT, , false",
        "SNAPSHOT, SNAPSHOT, true"
    })
    public void hasModuleNewRevisionReturnsFalseOnSameRelease(String fromClassifier, String toClassifier, boolean isEquivalent) {
        Version fromVersion = versionMock(fromClassifier);
        Version toVersion = versionMock(toClassifier);
        when(toVersion.isEquivalent(fromVersion)).thenReturn(isEquivalent);
        assertFalse(StandardTasks.hasModuleNewRevision(fromVersion, toVersion));
    }

    private Version versionMock(String classifier) {
        Version version = mock(Version.class);
        when(version.getClassifier()).thenReturn(classifier);
        when(version.isEquivalent(any())).thenReturn(false);
        return version;
    }
}
