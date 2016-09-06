package com.aperto.magkit.workflow;

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
 * Auto approval human task handler.
 *
 * @author frank.sommer
 * @since 05.09.2016
 */
public class AutoApproveHumanTaskWorkItemHandler extends HumanTaskWorkItemHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoApproveHumanTaskWorkItemHandler.class);

    private TasksManager _tasksManager;
    private KieSession _kieSession;

    public AutoApproveHumanTaskWorkItemHandler(TaskDefinitionRegistry taskDefinitionRegistry, ComponentProvider componentProvider, TasksManager tasksManager, KieSession kieSession) {
        super(taskDefinitionRegistry, componentProvider, tasksManager, kieSession);

        _tasksManager = tasksManager;
        _kieSession = kieSession;
    }

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
