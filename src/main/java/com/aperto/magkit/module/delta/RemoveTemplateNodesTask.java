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
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.length;

/**
 * Task to remove all nodes with a specific template.
 *
 * @author frank.sommer
 * @since 2.0.1
 */
public class RemoveTemplateNodesTask extends AbstractRepositoryTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveTemplateNodesTask.class);
    private static final int COUNTER_THRESHOLD_FOR_SESSION_SAVE = 100;

    private final String _currentTemplate;
    private final String _queryType;
    private final String _basePath;

    /**
     * Remove nodes with current template id in website workspace.
     *
     * @param currentTemplate template id to search for
     */
    public RemoveTemplateNodesTask(String currentTemplate) {
        this(currentTemplate, "/", null);
    }

    /**
     * Remove nodes with current template id in website workspace.
     *
     * @param currentTemplate template id to search for
     * @param basePath        base path for replacement
     * @param queryType       query node type
     */
    public RemoveTemplateNodesTask(String currentTemplate, String basePath, String queryType) {
        this(currentTemplate, basePath, queryType, createTaskName(currentTemplate));
    }

    /**
     * Remove nodes with current template id in website repository.
     *
     * @param currentTemplate template id to search for
     * @param basePath        base path for replacement
     * @param queryType       query node type
     * @param taskName        name of the task
     */
    public RemoveTemplateNodesTask(String currentTemplate, String basePath, String queryType, String taskName) {
        super(taskName, taskName);
        String message = createTaskMessage(taskName, basePath, queryType);
        LOGGER.info(message);

        _currentTemplate = currentTemplate;
        _queryType = queryType;
        _basePath = basePath;
    }

    protected String createTaskMessage(final String title, final String basePath, final String queryType) {
        return title + " (for node type " + queryType + " and below " + basePath + " in website workspace.";
    }

    private static String createTaskName(final String currentTemplate) {
        return "Removing nodes with template " + currentTemplate + ".";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        final String statement = buildQueryStatement();
        final Session session = installContext.getJCRSession(WEBSITE);
        final NodeIterator nodes = executeQuery(session, statement);
        doNodeOperations(session, nodes);
    }

    protected void doNodeOperations(final Session session, final NodeIterator nodes) throws RepositoryException {
        int counter = 0;
        while (nodes.hasNext()) {
            doNodeOperation(nodes.nextNode());

            counter++;
            if (counter >= COUNTER_THRESHOLD_FOR_SESSION_SAVE) {
                // Save the session here to prevent OutOfMemory exceptions when there is a large number of pages to change the template for
                // Yes, this slows down the task.
                session.save();
                counter = 0;
            }
        }

        // final session save
        if (counter != 0) {
            session.save();
        }
    }

    protected void doNodeOperation(final Node node) throws RepositoryException {
        try {
            node.remove();
        } catch (RepositoryException e) {
            // sometimes happens InvalidItemStateException by already removed items, catch them and proceed
            LOGGER.warn("Error removing node: {}. Skip this node.", NodeUtil.getPathIfPossible(node), e);
        }
    }

    protected NodeIterator executeQuery(final Session session, final String statement) throws RepositoryException {
        final Query query = session.getWorkspace().getQueryManager().createQuery(statement, Query.JCR_SQL2);
        final QueryResult result = query.execute();
        return result.getNodes();
    }

    protected String buildQueryStatement() {
        String queryNodeType = _queryType;
        if (isEmpty(queryNodeType)) {
            queryNodeType = _currentTemplate.contains(":components/") ? NodeTypes.Component.NAME : NodeTypes.Page.NAME;
        }
        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select * from [").append(queryNodeType).append("] where [" + NodeTypes.Renderable.TEMPLATE + "] = '").append(_currentTemplate).append("'");
        if (length(_basePath) > 1) {
            queryBuilder.append(" and ISDESCENDANTNODE([").append(_basePath).append("])");
        }
        return queryBuilder.toString();
    }
}
