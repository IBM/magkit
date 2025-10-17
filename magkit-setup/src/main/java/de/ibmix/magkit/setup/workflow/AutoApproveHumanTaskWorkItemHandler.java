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
import info.magnolia.module.workflow.jbpm.humantask.handler.HumanTaskWorkItemHandler;
import info.magnolia.module.workflow.jbpm.humantask.parameter.HumanTaskParameterResolver;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.task.TasksManager;
import info.magnolia.task.definition.registry.TaskDefinitionRegistry;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler implementation that creates a Magnolia human task and immediately auto-approves it.
 * <p>
 * This class extends {@link HumanTaskWorkItemHandler} to intercept human task work items in a jBPM workflow
 * and resolve them without any manual user interaction. After creating the {@link HumanTask} from the provided
 * parameters, the task is added to the {@link TasksManager} and directly resolved with the decision "approve".
 * </p>
 * <p><strong>Main functionalities & key features:</strong></p>
 * <ul>
 *   <li>Creates a human task based on work item parameters.</li>
 *   <li>Registers the task in the Magnolia task management system.</li>
 *   <li>Automatically resolves (approves) the task to allow the workflow to proceed.</li>
 * </ul>
 * <p><strong>Important details:</strong></p>
 * <ul>
 *   <li>Any {@link RegistrationException} during task definition lookup is caught and logged; the work item will not be resolved in that case.</li>
 *   <li>No user assignment or escalation logic is applied because approval is unconditional.</li>
 * </ul>
 * <p><strong>Side effects:</strong> A new task is persisted and resolved right away; listeners observing task lifecycle events will receive both add and resolve events almost instantly.</p>
 * <p><strong>Null & error handling:</strong> Assumes the work item contains a non-null task name parameter mapped to {@link #TASK_NAME}. Missing or invalid task definitions lead to a logged error.</p>
 * <p><strong>Thread-safety:</strong> This handler is not explicitly synchronized. It relies on thread-safety guarantees of {@link TasksManager} and {@link KieSession}. Instances are typically used within the workflow engine's execution context.</p>
 * <p><strong>Usage example:</strong></p>
 * <pre>{@code
 * WorkItemHandler handler = new AutoApproveHumanTaskWorkItemHandler(taskDefinitionRegistry, componentProvider, tasksManager, kieSession);
 * // Registered with the jBPM session elsewhere; jBPM invokes executeWorkItem automatically.
 * }</pre>
 *
 * @author frank.sommer
 * @since 2016-09-05
 */
public class AutoApproveHumanTaskWorkItemHandler extends HumanTaskWorkItemHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoApproveHumanTaskWorkItemHandler.class);

    private TasksManager _tasksManager;
    private KieSession _kieSession;

    /**
     * Constructs a handler that auto-approves all human tasks created from incoming work items.
     *
     * @param taskDefinitionRegistry registry used to look up task definitions
     * @param componentProvider Magnolia component provider for dependency resolution
     * @param tasksManager manager used to persist and resolve tasks
     * @param kieSession active jBPM session used to create task instances
     */
    public AutoApproveHumanTaskWorkItemHandler(TaskDefinitionRegistry taskDefinitionRegistry, ComponentProvider componentProvider, TasksManager tasksManager, KieSession kieSession) {
        super(taskDefinitionRegistry, componentProvider, tasksManager, kieSession);

        _tasksManager = tasksManager;
        _kieSession = kieSession;
    }

    /**
     * Creates a human task from the provided work item and immediately resolves it with an "approve" decision.
     * The task is first added to the {@link TasksManager} so that normal task lifecycle listeners are triggered.
     * If the task definition cannot be found a logged error prevents auto-approval and the workflow may stall until addressed.
     *
     * @param workItem the jBPM work item containing task name and related parameters
     * @param manager the jBPM work item manager (unused directly here as completion is implicit through resolution)
     */
    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        try {
            HumanTaskParameterResolver parameterResolver = getParameterResolver((String) workItem.getParameter(TASK_NAME));
            HumanTask task = parameterResolver.createTask(workItem, _kieSession);

            _tasksManager.addTask(task);

            // Auto approve the task
            Map<String, Object> result = new HashMap<>();
            result.put("decision", "approve");
            _tasksManager.resolve(task.getId(), result);
        } catch (RegistrationException e) {
            LOGGER.error("Could not retrieve task definition.", e);
        }
    }
}
