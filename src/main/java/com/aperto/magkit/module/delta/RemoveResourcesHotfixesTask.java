package com.aperto.magkit.module.delta;

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

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import static info.magnolia.resourceloader.jcr.JcrResourceOrigin.RESOURCES_WORKSPACE;

/**
 * A update task for removing all hotfixes in module specific resources.
 *
 * @author frank.sommer
 * @since 18.08.2016
 */
public class RemoveResourcesHotfixesTask extends AbstractRepositoryTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveResourcesHotfixesTask.class);

    private final String _basePath;

    /**
     * Constructor for task.
     *
     * @param basePath base path for module resources
     */
    public RemoveResourcesHotfixesTask(String basePath) {
        super("Remove hotfixes for module resources", "Remove hotfixes for module resources below " + basePath + ".");
        _basePath = basePath;
    }

    @Override
    protected void doExecute(final InstallContext installContext) throws RepositoryException, TaskExecutionException {
        String moduleName = installContext.getCurrentModuleDefinition().getName();
        LOGGER.info("Remove hotfixes for module {} below {}.", moduleName, _basePath);

        final String statement = "select * from [" + NodeTypes.Content.NAME + "] where [" + NodeTypes.Renderable.TEMPLATE + "] IS NULL and ISDESCENDANTNODE([" + _basePath + "])";
        final Session session = installContext.getJCRSession(RESOURCES_WORKSPACE);
        final Query query = session.getWorkspace().getQueryManager().createQuery(statement, Query.JCR_SQL2);
        final QueryResult result = query.execute();
        final NodeIterator nodes = result.getNodes();
        while (nodes.hasNext()) {
            nodes.nextNode().remove();
        }
    }
}
