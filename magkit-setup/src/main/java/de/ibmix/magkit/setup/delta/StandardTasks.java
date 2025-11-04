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
 * Utility class aggregating common Magnolia module version handler tasks.
 * Provides factory methods for frequently required configuration adjustments during module installation or updates:
 * <ul>
 *     <li>Workflow simplification (auto-approve human tasks, retry action availability)</li>
 *     <li>Filter bypass creation (URI or extension based)</li>
 *     <li>Security callback replacement</li>
 *     <li>Version revision classifier comparison helper</li>
 * </ul>
 * <p>Each method returns a {@link Task} ready to be added to a version handler. Where multiple changes need orchestration,
 * an {@link ArrayDelegateTask} sequence is returned.</p>
 * <p>Preconditions: Requires Magnolia configuration repository availability during execution.</p>
 * <p>Side Effects: Modifies nodes below /server/filters, /modules/&lt;module&gt;/workflow-jbpm, /server/filters/securityCallback.</p>
 * <p>Error Handling: Individual tasks encapsulate their own exception handling; this class only assembles tasks.</p>
 * <p>Thread-Safety: Stateless; all methods are safe for concurrent invocation.</p>
 * <p>Usage Example: {@code tasks.add(StandardTasks.setSimpleWorkflow());}</p>
 *
 * @author Norman Wiechmann (Aperto AG)
 * @since ??
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
     * Creates a task sequence configuring a simple workflow (auto approval and retry availability adjustments).
     *
     * @return composite task performing workflow simplification
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

    /**
     * Convenience helper adding a monitoring bypass if missing.
     *
     * @return task establishing filter bypass for /monitoring
     */
    public static Task addBypassForMonitoring() {
        return addFilteringBypassIfMissing("/monitoring", PATH_FILTER);
    }

    /**
     * Creates a task that adds a filter bypass configuration if it does not already exist.
     * <p>
     * Uses either {@link URIStartsWithVoter} (for absolute path beginning with '/') or {@link ExtensionVoter} for plain extensions.
     * </p>
     *
     * @param uriPattern URI path (starting with '/') or file extension pattern
     * @param filterPath path to filter configuration node
     * @return task validating presence and creating bypass config if absent
     */
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

    /**
     * Creates a task to set the author form client callback implementation.
     *
     * @return task adjusting security callback configuration
     */
    public static Task setSecurityCallback() {
        return selectServerConfig("Change callback", "Set the author form client callback.",
            getNode("filters/securityCallback/clientCallbacks/form").then(
                setProperty(PN_CLASS, AuthorFormClientCallback.class.getName())
            )
        );
    }

    /**
     * Compares versions including classifier to decide if an update should be triggered.
     * <p>
     * If numeric version parts are equivalent but the classifier differs (or a released version without classifier replaces a classified one) then update is indicated.
     * </p>
     *
     * @param fromVersion existing installed version
     * @param toVersion candidate version
     * @return true if classifier change suggests update
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
