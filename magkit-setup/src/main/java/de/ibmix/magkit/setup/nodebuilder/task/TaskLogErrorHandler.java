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
 * An ErrorHandler which logs handled errors to the InstallContext
 * as warnings, and wraps unhandled exceptions in NodeOperationException.
 *
 * @author frank.sommer
 * @see info.magnolia.jcr.nodebuilder.task.TaskLogErrorHandler
 */
public class TaskLogErrorHandler extends AbstractErrorHandler {
    private final InstallContext _installCtx;

    public TaskLogErrorHandler(InstallContext installCtx) {
        _installCtx = installCtx;
    }

    @Override
    public void report(String message) {
        _installCtx.warn(message);
    }
}
