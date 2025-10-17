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

import info.magnolia.jcr.nodebuilder.ErrorHandler;
import info.magnolia.jcr.nodebuilder.NodeBuilder;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.nodebuilder.NodeOperationException;
import info.magnolia.jcr.nodebuilder.StrictErrorHandler;
import info.magnolia.jcr.nodebuilder.task.ErrorHandling;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Repository task executing a sequence of {@link NodeOperation} instances via Magnolia's {@link NodeBuilder} starting
 * from a configured root path in a workspace. Designed for module install/update phases where idempotent or adaptive
 * JCR changes are required.
 * <p>Key features:
 * <ul>
 *     <li>Aggregates multiple node operations under one install task with centralized error handling.</li>
 *     <li>Supports configurable root path selection to scope modifications.</li>
 *     <li>Error handling strategy selectable (logging vs strict) through {@link ErrorHandling} enum.</li>
 * </ul>
 * Usage preconditions: Workspace and root path must exist prior to execution, otherwise underlying operations may
 * fail with {@link RepositoryException}. Side effects: Mutates JCR content; failures may partially apply changes
 * depending on error handler. Null and error handling: Constructor enforces non-null parameters indirectly through
 * task usage; operations array may be empty producing a no-op execution. Thread-safety: Instances are not thread-safe
 * for concurrent execute calls; Magnolia executes tasks sequentially during install/update. A single instance stores
 * immutable configuration after construction.
 *
 * @author frank.sommer
 * @since 2010
 */
public class NodeBuilderTask extends AbstractRepositoryTask {
    private final String _workspaceName;
    private final String _rootPath;
    private final ErrorHandling _errorHandling;
    private final NodeOperation[] _operations;

    /**
     * Constructs a task operating from the workspace root path "/".
     *
     * @param taskName human readable task name
     * @param description description of purpose
     * @param errorHandling error handling strategy (logging or strict)
     * @param workspaceName target JCR workspace name
     * @param operations ordered operations executed by NodeBuilder
     */
    public NodeBuilderTask(String taskName, String description, ErrorHandling errorHandling, String workspaceName, NodeOperation... operations) {
        this(taskName, description, errorHandling, workspaceName, "/", operations);
    }

    /**
     * Constructs a task operating from a specific root path.
     *
     * @param taskName human readable task name
     * @param description description of purpose
     * @param errorHandling error handling strategy (logging or strict)
     * @param workspaceName target JCR workspace name
     * @param rootPath existing root path in workspace from which operations start
     * @param operations ordered operations executed by NodeBuilder
     */
    public NodeBuilderTask(String taskName, String description, ErrorHandling errorHandling, String workspaceName, String rootPath, NodeOperation... operations) {
        super(taskName, description);
        _errorHandling = errorHandling;
        _operations = operations;
        _workspaceName = workspaceName;
        _rootPath = rootPath;
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final Node root = getRootNode(ctx);
        final ErrorHandler errorHandler = newErrorHandler(ctx);
        final NodeBuilder nodeBuilder = new NodeBuilder(errorHandler, root, _operations);
        try {
            nodeBuilder.exec();
        } catch (NodeOperationException e) {
            throw new TaskExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Obtains the root node for execution from the install context.
     *
     * @param ctx Magnolia install context
     * @return root node of workspace/path
     * @throws RepositoryException if session or path retrieval fails
     */
    protected Node getRootNode(InstallContext ctx) throws RepositoryException {
        Session session = ctx.getJCRSession(_workspaceName);
        return session.getNode(_rootPath);
    }

    /**
     * Creates an error handler instance based on configured strategy.
     *
     * @param ctx Magnolia install context (used for logging in logging mode)
     * @return error handler instance
     */
    protected ErrorHandler newErrorHandler(InstallContext ctx) {
        ErrorHandler errorHandler;
        if (_errorHandling == ErrorHandling.logging) {
            errorHandler = new TaskLogErrorHandler(ctx);
        } else {
            errorHandler = new StrictErrorHandler();
        }
        return errorHandler;
    }
}
