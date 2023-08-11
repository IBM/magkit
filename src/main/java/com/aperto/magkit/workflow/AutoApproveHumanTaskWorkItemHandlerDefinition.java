package com.aperto.magkit.workflow;

import info.magnolia.module.workflow.jbpm.humantask.handler.definition.HumanTaskWorkItemHandlerDefinition;

/**
 * Definition for human task handler with auto approval.
 *
 * @author frank.sommer
 * @since 05.09.2016
 */
public class AutoApproveHumanTaskWorkItemHandlerDefinition extends HumanTaskWorkItemHandlerDefinition {

    public AutoApproveHumanTaskWorkItemHandlerDefinition() {
        setImplementationClass(AutoApproveHumanTaskWorkItemHandler.class);
    }
}
