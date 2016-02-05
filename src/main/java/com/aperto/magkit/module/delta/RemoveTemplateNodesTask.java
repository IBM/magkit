package com.aperto.magkit.module.delta;

import info.magnolia.jcr.util.NodeTypes;
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
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.length;

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
        node.remove();
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
