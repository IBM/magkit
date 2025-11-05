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

import info.magnolia.jcr.nodebuilder.AbstractErrorHandler;
import info.magnolia.module.InstallContext;

/**
 * Error handler implementation forwarding non-fatal NodeBuilder issues as WARN log entries into the Magnolia
 * {@link InstallContext}. Used to continue execution while surfacing problems to administrators during module
 * installation or update. Unhandled exceptions are escalated by base class behavior (see {@link AbstractErrorHandler}).
 * <p>Key features:
 * <ul>
 *     <li>Converts reported errors into install context warnings.</li>
 *     <li>Simple, stateless handling relying on Magnolia task logging pipeline.</li>
 * </ul>
 * Usage preconditions: A non-null {@link InstallContext} must be provided. Thread-safety: Instances hold a reference
 * to the context but perform no mutable shared state changes; suitable for single-threaded task execution.
 *
 * @author frank.sommer
 * @since 2010
 * @see info.magnolia.jcr.nodebuilder.task.TaskLogErrorHandler
 */
public class TaskLogErrorHandler extends AbstractErrorHandler {
    private final InstallContext _installCtx;

    /**
     * Creates a logging error handler writing warnings to the given install context.
     *
     * @param installCtx current Magnolia install context
     */
    public TaskLogErrorHandler(InstallContext installCtx) {
        _installCtx = installCtx;
    }

    /**
     * Reports a handled issue as a warning.
     *
     * @param message warning text
     */
    @Override
    public void report(String message) {
        _installCtx.warn(message);
    }
}
