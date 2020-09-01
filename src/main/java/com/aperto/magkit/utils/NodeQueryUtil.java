package com.aperto.magkit.utils;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static info.magnolia.cms.core.MgnlNodeType.NT_COMPONENT;
import static info.magnolia.cms.util.QueryUtil.search;
import static info.magnolia.jcr.util.NodeUtil.asIterable;
import static info.magnolia.jcr.util.NodeUtil.asList;
import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static javax.jcr.query.Query.JCR_SQL2;
import static javax.jcr.query.Query.XPATH;
import static org.apache.commons.collections4.CollectionUtils.collect;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * Utility methods for common queries.
 *
 * @author angelika.foerst
 * @author frank.sommer
 */
public final class NodeQueryUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeQueryUtil.class);

    public static final String QUERY_SELECTOR_PAGE = "page";
    public static final String QUERY_SELECTOR_COMPONENT = "component";
    public static final String DUMMY_ORDERING = "@jcr:primaryType";

    /**
     * Executes simple query statements on the website repository.
     * This method can only deal with queries containing just one selector (no joins).
     * For more complex queries use {@link #createQuery(String, String, java.util.Map)}.
     *
     * @param sqlQueryStatement the query in JCR-SQL2 syntax
     * @return a list of nodes or null
     */
    public static List<Node> executeSqlQuery(final String sqlQueryStatement) {
        return executeQuery(sqlQueryStatement, JCR_SQL2, WEBSITE);
    }

    /**
     * Executes simple query statements on the given repository.
     * This method can only deal with queries containing just one selector (no joins).
     *
     * @param sqlQueryStatement the query in JCR-SQL2 syntax
     * @param repository        target repository
     * @return a list of nodes or null
     */
    public static List<Node> executeSqlQuery(final String sqlQueryStatement, final String repository) {
        return executeQuery(sqlQueryStatement, JCR_SQL2, repository);
    }

    /**
     * Executes a query and returns a list of nodes.
     *
     * @param queryStatement query statement
     * @param language       language of the query statement
     * @param repository     target repository
     * @return a list of nodes or null
     * @see info.magnolia.cms.util.QueryUtil
     */
    public static List<Node> executeQuery(final String queryStatement, final String language, final String repository) {
        List<Node> nodes = null;
        try {
            NodeIterator result = search(repository, queryStatement, language);
            nodes = asList(asIterable(result));
        } catch (RepositoryException e) {
            LOGGER.error("Error executing query with statement {}.", queryStatement, e);
        }
        return nodes;
    }

    /**
     * Returns pages having a given template. A root node for the search may be specified.
     *
     * @param templateName name of the page template. Fully qualified, e.g. 'rsmn-main:pages/rmArticle'
     * @param searchRoot   the search root or null
     * @return a list of nodes or null
     */
    public static List<Node> getPagesWithTemplate(final String templateName, final Node searchRoot) {
        return getPagesWithTemplate(templateName, searchRoot, null);
    }

    /**
     * Returns pages having a given template. A root node and additional constraints on the page may be specified.
     *
     * @param templateName       name of the page template. Fully qualified, e.g. 'rsmn-main:pages/rmArticle'
     * @param searchRoot         the search root or null
     * @param xPathPageCondition additional condition that wil be applied on the page. Uses XPath notation and is surrounded by square brackets.
     * @return a list of nodes or null
     */
    public static List<Node> getPagesWithTemplate(final String templateName, final Node searchRoot, final String xPathPageCondition) {
        final StringBuilder statement = new StringBuilder();
        statement.append("/jcr:root");
        if (searchRoot != null) {
            statement.append(getPathIfPossible(searchRoot));
        }
        statement.append("//element(*,mgnl:page)");
        statement.append("[MetaData/@mgnl:template='").append(templateName).append("']");
        statement.append(defaultString(xPathPageCondition));
        return executeQuery(statement.toString(), XPATH, WEBSITE);
    }

    /**
     * Returns components having a given template. A root node and additional constraints on the component may be specified.
     *
     * @param templateName   name of the component template. Fully qualified, e.g. 'rsmn-main:components/rmDynamicGuideList'
     * @param searchRoot     the search root or null
     * @param xPathCondition additional condition that wil be applied on the component. Uses XPath notation and is surrounded by square brackets.
     * @return a list of nodes or null
     */
    public static List<Node> getComponentsWithTemplate(final String templateName, final Node searchRoot, final String xPathCondition) {
        String rootPath = searchRoot != null ? getPathIfPossible(searchRoot) : EMPTY;
        return getComponentsWithTemplate(templateName, rootPath, xPathCondition);
    }

    /**
     * Returns components having a given template. A root node and additional constraints on the component may be specified.
     *
     * @param templateName   name of the component template. Fully qualified, e.g. 'rsmn-main:components/rmDynamicGuideList'
     * @param searchRoot     the search root or empty string
     * @param xPathCondition additional condition that wil be applied on the component. Uses XPath notation and is surrounded by square brackets.
     * @return a list of nodes or null
     */
    public static List<Node> getComponentsWithTemplate(final String templateName, final String searchRoot, final String xPathCondition) {
        XpathBuilder xpathBuilder = new XpathBuilder();

        if (isNotEmpty(searchRoot)) {
            xpathBuilder.path(searchRoot);
        }
        xpathBuilder.type(NT_COMPONENT);

        ConstraintBuilder constraintBuilder = new ConstraintBuilder().addTplNameConstraint(templateName);
        if (isNotEmpty(xPathCondition)) {
            constraintBuilder.add(ConstraintBuilder.Operator.AND, xPathCondition);
        }
        xpathBuilder.property(constraintBuilder.build());

        xpathBuilder.orderBy(DUMMY_ORDERING);
        return executeQuery(xpathBuilder.build(), XPATH, WEBSITE);
    }

    /**
     * Creates a query for a component inside a page.
     *
     * @param componentsTemplateName template name of the component
     * @param searchRoot             search root (path of the page node)
     * @return component node or null
     */
    public static Node findComponentOnPage(final String componentsTemplateName, final String searchRoot) {
        Node componentNode = null;
        List<String> areaRoots = retrieveChildAreas(searchRoot);
        final NodeIterator componentsIterator = findDescendantComponents(componentsTemplateName, areaRoots.toArray(new String[areaRoots.size()]));
        if (componentsIterator != null && componentsIterator.hasNext()) {
            componentNode = componentsIterator.nextNode();
        }
        return componentNode;
    }

    private static List<String> retrieveChildAreas(final String searchRoot) {
        final List<Node> nodes = executeSqlQuery("select * from [mgnl:area] where ischildnode('" + searchRoot + "')");
        return (List<String>) collect(nodes, NodeUtil::getPathIfPossible);
    }

    /**
     * Find a descendant component node.
     *
     * @param componentsTemplateName component template name
     * @param searchRoot             search root
     * @return component node
     */
    public static Node findDescendantComponent(final String componentsTemplateName, final String searchRoot) {
        Node componentNode = null;
        final NodeIterator componentsIterator = findDescendantComponents(componentsTemplateName, searchRoot);
        if (componentsIterator != null && componentsIterator.hasNext()) {
            componentNode = componentsIterator.nextNode();
        }
        return componentNode;
    }

    /**
     * Find descendant component nodes.
     *
     * @param componentsTemplateName component template name
     * @param searchRoots            search roots
     * @return node iterator
     */
    public static NodeIterator findDescendantComponents(final String componentsTemplateName, final String... searchRoots) {
        NodeIterator nodeIterator = null;

        StringBuilder statement = new StringBuilder();
        statement.append("select * from [mgnl:component] where [mgnl:template] = '").append(componentsTemplateName).append("'");
        if (ArrayUtils.isNotEmpty(searchRoots)) {
            statement.append(" and (");
            statement.append(join(Arrays.stream(searchRoots).map(searchRoot -> "ISDESCENDANTNODE('" + searchRoot + "')").toArray(), " or "));
            statement.append(")");
        }

        try {
            final Query sqlQuery = createSqlQuery(statement.toString(), null);
            final QueryResult queryResult = sqlQuery.execute();
            nodeIterator = queryResult.getNodes();
        } catch (RepositoryException e) {
            LOGGER.error("Error executing query for component {}.", componentsTemplateName, e);
        }

        return nodeIterator;
    }

    /**
     * Creates a {@link javax.jcr.query.Query}.
     *
     * @param queryStatement the query statement
     * @param language       the query language
     * @param bindValues     a mapping of parameter names to values
     * @param repository     the repository on which the query is executed
     * @return a query object
     */
    public static Query createQuery(final String queryStatement, final String language, final Map<String, Value> bindValues, final String repository) {
        Query query = null;
        try {
            final Session jcrSession = MgnlContext.getJCRSession(repository);
            final QueryManager queryManager = jcrSession.getWorkspace().getQueryManager();
            query = queryManager.createQuery(queryStatement, language);

            if (bindValues != null && !bindValues.isEmpty() && JCR_SQL2.equals(language)) {
                for (Map.Entry<String, Value> bindValue : bindValues.entrySet()) {
                    query.bindValue(bindValue.getKey(), bindValue.getValue());
                }
            }
        } catch (RepositoryException e) {
            LOGGER.error("Could not create query object for statement {}", queryStatement, e);
        }

        return query;
    }

    /**
     * @see #createQuery(String, String, java.util.Map, String)
     */
    public static Query createQuery(final String queryStatement, final String language, final Map<String, Value> bindValues) {
        return createQuery(queryStatement, language, bindValues, WEBSITE);
    }

    /**
     * @see #createQuery(String, String, java.util.Map, String)
     */
    public static Query createSqlQuery(final String queryStatement, final Map<String, Value> bindValues) {
        return createQuery(queryStatement, JCR_SQL2, bindValues, WEBSITE);
    }

    /**
     * @see #createQuery(String, String, java.util.Map, String)
     */
    public static Query createXPathQuery(final String queryStatement) {
        return createQuery(queryStatement, XPATH, null, WEBSITE);
    }

    /**
     * Executes a query and retrieves the results identified by a selector.
     *
     * @param query        the query object
     * @param selectorName the selector
     * @return a list of nodes or null
     */
    public static List<Node> executeQuery(final Query query, final String selectorName) {
        final List<Node> resultList = new ArrayList<>();
        try {
            if (query != null) {
                final QueryResult result = query.execute();
                final RowIterator rows = result.getRows();

                while (rows.hasNext()) {
                    final Row row = rows.nextRow();
                    resultList.add(row.getNode(selectorName));
                }
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error executing query with statement {}", query.getStatement(), e);
        }
        return resultList;
    }

    private NodeQueryUtil() {
        //private constructor
    }
}
