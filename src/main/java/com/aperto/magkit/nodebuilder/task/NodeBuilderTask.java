package com.aperto.magkit.nodebuilder.task;

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
 * A task using the NodeBuilder API, applying operations on a given path.
 * Adaption from Magnolias {@link info.magnolia.jcr.nodebuilder.task.NodeBuilderTask} of Content API base.
 *
 * @author frank.sommer
 */
public class NodeBuilderTask extends AbstractRepositoryTask {
    private final String _workspaceName;
    private final String _rootPath;
    private final ErrorHandling _errorHandling;
    private final NodeOperation[] _operations;

    public NodeBuilderTask(String taskName, String description, ErrorHandling errorHandling, String workspaceName, NodeOperation... operations) {
        this(taskName, description, errorHandling, workspaceName, "/", operations);
    }

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

    protected Node getRootNode(InstallContext ctx) throws RepositoryException {
        Session session = ctx.getJCRSession(_workspaceName);
        return session.getNode(_rootPath);
    }

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