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

import info.magnolia.module.workflow.jbpm.humantask.handler.definition.HumanTaskWorkItemHandlerDefinition;

/**
 * Work item handler definition that configures a human task handler to automatically approve tasks
 * without requiring manual user interaction. It simply sets the implementation class to {@link AutoApproveHumanTaskWorkItemHandler}.
 * <p>
 * Key features:
 * <ul>
 *     <li>Auto-approval of human tasks to streamline workflow execution.</li>
 *     <li>No additional configuration required beyond registration of this definition.</li>
 * </ul>
 * Usage preconditions: Magnolia workflow module must be present and the referenced handler class must be available.
 * Side effects: Human tasks using this handler are completed automatically; no user assignment or escalation occurs.
 * Null and error handling: This definition performs only a static class binding and does not execute logic that can produce runtime errors here.
 * Thread-safety: Instances are immutable after construction and thus thread-safe.
 *
 * @author frank.sommer
 * @since 2016-09-05
 */
public class AutoApproveHumanTaskWorkItemHandlerDefinition extends HumanTaskWorkItemHandlerDefinition {

    /**
     * Creates the definition and binds it to {@link AutoApproveHumanTaskWorkItemHandler} for auto approval behavior.
     */
    public AutoApproveHumanTaskWorkItemHandlerDefinition() {
        setImplementationClass(AutoApproveHumanTaskWorkItemHandler.class);
    }
}
