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

import info.magnolia.module.workflow.jbpm.humantask.HumanTask;
import info.magnolia.module.workflow.jbpm.humantask.parameter.HumanTaskParameterResolver;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.task.TasksManager;
import info.magnolia.task.definition.registry.TaskDefinitionRegistry;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AutoApproveHumanTaskWorkItemHandler} covering happy path and error handling.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-26
 */
public class AutoApproveHumanTaskWorkItemHandlerTest {

    @Test
    public void executeWorkItemAutoApprovesTask() {
        TaskDefinitionRegistry taskDefinitionRegistry = mock(TaskDefinitionRegistry.class);
        ComponentProvider componentProvider = mock(ComponentProvider.class);
        TasksManager tasksManager = mock(TasksManager.class);
        KieSession kieSession = mock(KieSession.class);
        WorkItem workItem = mock(WorkItem.class);
        WorkItemManager workItemManager = mock(WorkItemManager.class);
        HumanTaskParameterResolver parameterResolver = mock(HumanTaskParameterResolver.class);
        HumanTask humanTask = mock(HumanTask.class);

        when(workItem.getParameter(anyString())).thenReturn("myTaskName");
        when(humanTask.getId()).thenReturn("task-123");
        when(parameterResolver.createTask(workItem, kieSession)).thenReturn(humanTask);

        TestHandler handler = new TestHandler(taskDefinitionRegistry, componentProvider, tasksManager, kieSession, parameterResolver, false);
        handler.executeWorkItem(workItem, workItemManager);

        InOrder order = inOrder(tasksManager);
        order.verify(tasksManager).addTask(humanTask);
        ArgumentCaptor<Map<String, Object>> decisionCaptor = ArgumentCaptor.forClass(Map.class);
        order.verify(tasksManager).resolve(eq("task-123"), decisionCaptor.capture());
        assertEquals("approve", decisionCaptor.getValue().get("decision"));
        verifyNoMoreInteractions(tasksManager);
    }

    @Test
    public void executeWorkItemRegistrationExceptionSkipsTaskInteraction() {
        TaskDefinitionRegistry taskDefinitionRegistry = mock(TaskDefinitionRegistry.class);
        ComponentProvider componentProvider = mock(ComponentProvider.class);
        TasksManager tasksManager = mock(TasksManager.class);
        KieSession kieSession = mock(KieSession.class);
        WorkItem workItem = mock(WorkItem.class);
        WorkItemManager workItemManager = mock(WorkItemManager.class);

        when(workItem.getParameter(anyString())).thenReturn("myTaskName");

        TestHandler handler = new TestHandler(taskDefinitionRegistry, componentProvider, tasksManager, kieSession, null, true);
        handler.executeWorkItem(workItem, workItemManager);

        verifyNoInteractions(tasksManager);
    }

    static class TestHandler extends AutoApproveHumanTaskWorkItemHandler {
        private final HumanTaskParameterResolver _resolver;
        private final boolean _throwRegistrationException;

        TestHandler(TaskDefinitionRegistry registry, ComponentProvider provider, TasksManager tasksManager, KieSession kieSession, HumanTaskParameterResolver resolver, boolean throwRegistrationException) {
            super(registry, provider, tasksManager, kieSession);
            _resolver = resolver;
            _throwRegistrationException = throwRegistrationException;
        }

        @Override
        protected HumanTaskParameterResolver getParameterResolver(String taskName, Object... parameters) throws RegistrationException {
            if (_throwRegistrationException) {
                throw new RegistrationException("not found");
            }
            return _resolver;
        }
    }
}
