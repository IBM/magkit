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

import de.ibmix.magkit.setup.security.AuthorFormClientCallback;
import de.ibmix.magkit.setup.workflow.AutoApproveHumanTaskWorkItemHandlerDefinition;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.objectfactory.Components;
import info.magnolia.setup.initial.AddFilterBypassTask;
import info.magnolia.voting.voters.ExtensionVoter;
import info.magnolia.voting.voters.URIStartsWithVoter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.addOrGetContentNode;
import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.addOrSetProperty;
import static de.ibmix.magkit.setup.nodebuilder.task.NodeBuilderTaskFactory.selectConfig;
import static de.ibmix.magkit.setup.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static de.ibmix.magkit.setup.nodebuilder.task.NodeBuilderTaskFactory.selectServerConfig;
import static info.magnolia.jcr.nodebuilder.Ops.getNode;
import static info.magnolia.jcr.nodebuilder.Ops.setProperty;
import static info.magnolia.repository.RepositoryConstants.CONFIG;
import static org.apache.commons.lang3.StringUtils.strip;

/**
 * Collection of standard module version handler tasks.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public final class StandardTasks {
    private static final Logger LOGGER = LoggerFactory.getLogger(StandardTasks.class);

    public static final String PN_CLASS = "class";
    @SuppressWarnings("unused")
    public static final String PN_ENABLED = "enabled";
    public static final String PN_PATTERN = "pattern";
    private static final String NN_WORKFLOW = "workflow";
    @SuppressWarnings("unused")
    public static final String NN_CONFIG = "config";

    private static final String PATH_FILTER = "/server/filters";

    /**
     * Set for activation and deactivation the simple workflow.
     *
     * @return task to execute
     */
    public static Task setSimpleWorkflow() {
        return new ArrayDelegateTask("Set simple workflow", "Set all configurations simple workflow without approval step.",
            selectModuleConfig("Set auto approval", "Set auto approval human task handler.", "workflow-jbpm",
                getNode("workItemHandlers/humanTask").then(
                    setProperty(PN_CLASS, AutoApproveHumanTaskWorkItemHandlerDefinition.class.getName())
                )
            ),
            selectModuleConfig("Allow retry action", "Allow retry action on failure.", NN_WORKFLOW,
                getNode("messageViews/publish/actions/retry/availability/rules/CanDeleteTaskRule").then(
                    addOrSetProperty("assignee", false)
                )
            )
        );
    }

    public static Task addBypassForMonitoring() {
        return addFilteringBypassIfMissing("/monitoring", PATH_FILTER);
    }

    public static Task addFilteringBypassIfMissing(final String uriPattern, final String filterPath) {
        // for path pattern remove beginning or ending slashes before creating a valid node name
        String nodeName = Components.getComponent(NodeNameHelper.class).getValidatedName(strip(uriPattern, "/"));

        final Task creationTask;
        if (uriPattern.startsWith("/")) {
            creationTask = new AddFilterBypassTask(filterPath, nodeName, URIStartsWithVoter.class, uriPattern);
        } else {
            creationTask = selectConfig("Add extension bypass for " + uriPattern, "", getNode(filterPath).then(
                addOrGetContentNode("bypasses").then(
                    addOrGetContentNode(nodeName).then(
                        addOrSetProperty(PN_CLASS, ExtensionVoter.class.getName()),
                        addOrSetProperty("allow", uriPattern)
                    )
                )
            ));
        }

        return new NodeExistsDelegateTask("Check " + nodeName + " bypass", "Check " + nodeName + " bypass in " + filterPath + " config.", CONFIG, filterPath + "/bypasses/" + nodeName, null, creationTask);
    }

    public static Task setSecurityCallback() {
        return selectServerConfig("Change callback", "Set the author form client callback.",
            getNode("filters/securityCallback/clientCallbacks/form").then(
                setProperty(PN_CLASS, AuthorFormClientCallback.class.getName())
            )
        );
    }

    /**
     * Compares the versions and the revision classifier. If the version numbers are equal, a different classifier should trigger a module update.
     *
     * @param fromVersion from version
     * @param toVersion to version
     * @return true, if version classifier is different
     */
    public static boolean hasModuleNewRevision(final Version fromVersion, final Version toVersion) {
        boolean triggerUpdate = false;
        if (toVersion.isEquivalent(fromVersion)) {
            String toClassifier = toVersion.getClassifier();
            String fromClassifier = fromVersion.getClassifier();
            if (toClassifier == null && fromClassifier != null) {
                LOGGER.debug("A released version was found. Trigger module update.");
                triggerUpdate = true;
            } else if (fromClassifier != null && !fromClassifier.equals(toClassifier)) {
                LOGGER.debug("A new classifier version was found. Trigger module update.");
                triggerUpdate = true;
            }
        }
        return triggerUpdate;
    }

    private StandardTasks() {
        // hidden default constructor
    }
}
