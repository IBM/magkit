package com.aperto.magkit.nodebuilder.task;

import info.magnolia.jcr.nodebuilder.AbstractErrorHandler;
import info.magnolia.module.InstallContext;

/**
 * An ErrorHandler which logs handled errors to the InstallContext
 * as warnings, and wraps unhandled exceptions in NodeOperationException.
 * @see info.magnolia.nodebuilder.task.TaskLogErrorHandler
 *
 * @author frank.sommer
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
