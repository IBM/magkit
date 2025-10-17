package de.ibmix.magkit.query;

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

import de.ibmix.magkit.query.xpath.ConstraintBuilder;
import de.ibmix.magkit.query.xpath.XpathBuilder;
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

import static info.magnolia.cms.util.QueryUtil.search;
import static info.magnolia.jcr.util.NodeTypes.Component;
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
 * Utility class offering convenience methods to build and execute common Magnolia JCR queries (SQL2 and XPath)
 * against repositories (primarily the {@code website} repository). It focuses on single-selector queries and
 * template-based lookup of pages and components.
 *
 * Key features:
 * <ul>
 *   <li>Execution of simple JCR-SQL2 and XPath queries returning {@link Node} lists.</li>
 *   <li>Lookup of pages/components by template name with optional additional XPath constraints.</li>
 *   <li>Retrieval of descendant components across multiple area roots.</li>
 *   <li>Factory methods to create {@link Query} instances with optional bind values (SQL2 only).</li>
 *   <li>Selector-based result extraction for multi-selector queries.</li>
 * </ul>
 *
 * Usage preconditions:
 * <ul>
 *   <li>A valid Magnolia context must provide an active JCR {@link Session} for the specified repository.</li>
 *   <li>Methods expecting paths (e.g. {@code searchRoot}) assume absolute repository paths.</li>
 * </ul>
 *
 * Null and error handling:
 * <ul>
 *   <li>Query creation or execution errors are logged; methods then return {@code null}, an empty list, or
 *       {@code null} iterator depending on the method signature.</li>
 *   <li>Callers should defensively handle {@code null} results.</li>
 * </ul>
 *
 * Side effects: Read-only; no repository modifications are performed.
 *
 * Thread-safety: Stateless and composed entirely of {@code static} methods. Safe for concurrent use; underlying
 * Magnolia infrastructure manages session lifecycles.
 *
 * Usage example:
 * <pre>{@code
 * List<Node> articlePages = NodeQueryUtil.getPagesWithTemplate("my-module:pages/article", null);
 * Node teaser = NodeQueryUtil.findDescendantComponent("my-module:components/teaser", "/website/path/to/page");
 * }</pre>
 *
 * @author angelika.foerst
 * @author frank.sommer
 * @since 2023-01-01
 */
public final class NodeQueryUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeQueryUtil.class);

    public static final String QUERY_SELECTOR_PAGE = "page";
    public static final String QUERY_SELECTOR_COMPONENT = "component";
    public static final String DUMMY_ORDERING = "@jcr:primaryType";

    /**
     * Executes simple query statements on the website repository using JCR-SQL2 for a single selector.
     * Primarily intended for straightforward node retrieval without joins.
     *
     * @param sqlQueryStatement the query in JCR-SQL2 syntax
     * @return list of matching nodes or {@code null} on error
     */
    public static List<Node> executeSqlQuery(final String sqlQueryStatement) {
        return executeQuery(sqlQueryStatement, JCR_SQL2, WEBSITE);
    }

    /**
     * Executes simple query statements on the given repository using JCR-SQL2 for a single selector.
     *
     * @param sqlQueryStatement the query in JCR-SQL2 syntax
     * @param repository target repository name
     * @return list of matching nodes or {@code null} on error
     */
    public static List<Node> executeSqlQuery(final String sqlQueryStatement, final String repository) {
        return executeQuery(sqlQueryStatement, JCR_SQL2, repository);
    }

    /**
     * Executes a query and returns all matched nodes for single-selector statements.
     *
     * @param queryStatement query statement (SQL2 or XPath)
     * @param language query language identifier
     * @param repository target repository name
     * @return list of matching nodes or {@code null} on error
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
     * Returns pages having the given template, optionally scoped to a search root.
     *
     * @param templateName fully qualified page template name
     * @param searchRoot optional root node limiting the search scope (may be {@code null})
     * @return list of page nodes matching the template or {@code null} on error
     */
    public static List<Node> getPagesWithTemplate(final String templateName, final Node searchRoot) {
        return getPagesWithTemplate(templateName, searchRoot, null);
    }

    /**
     * Returns pages having the given template, optionally scoped to a search root and filtered by an additional
     * XPath condition applied to the page node.
     *
     * @param templateName fully qualified page template name
     * @param searchRoot optional root node limiting the search scope (may be {@code null})
     * @param xPathPageCondition optional additional XPath condition (without surrounding brackets); may be {@code null}
     * @return list of page nodes matching the criteria or {@code null} on error
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
     * Returns components having a given template, optionally scoped to a search root and filtered by an additional
     * XPath condition.
     *
     * @param templateName fully qualified component template name
     * @param searchRoot optional root node limiting the search scope (may be {@code null})
     * @param xPathCondition optional additional XPath condition (without surrounding brackets); may be {@code null}
     * @return list of component nodes matching the criteria or {@code null} on error
     */
    public static List<Node> getComponentsWithTemplate(final String templateName, final Node searchRoot, final String xPathCondition) {
        String rootPath = searchRoot != null ? getPathIfPossible(searchRoot) : EMPTY;
        return getComponentsWithTemplate(templateName, rootPath, xPathCondition);
    }

    /**
     * Returns components having a given template, optionally scoped to a search root path and filtered by an
     * additional XPath condition.
     *
     * @param templateName fully qualified component template name
     * @param searchRoot optional root path limiting the search scope (may be empty)
     * @param xPathCondition optional additional XPath condition (without surrounding brackets); may be {@code null}
     * @return list of component nodes matching the criteria or {@code null} on error
     */
    public static List<Node> getComponentsWithTemplate(final String templateName, final String searchRoot, final String xPathCondition) {
        XpathBuilder xpathBuilder = new XpathBuilder();

        if (isNotEmpty(searchRoot)) {
            xpathBuilder.path(searchRoot);
        }
        xpathBuilder.type(Component.NAME);

        ConstraintBuilder constraintBuilder = new ConstraintBuilder().addTplNameConstraint(templateName);
        if (isNotEmpty(xPathCondition)) {
            constraintBuilder.add(ConstraintBuilder.Operator.AND, xPathCondition);
        }
        xpathBuilder.property(constraintBuilder.build());

        xpathBuilder.orderBy(DUMMY_ORDERING);
        return executeQuery(xpathBuilder.build(), XPATH, WEBSITE);
    }

    /**
     * Finds the first component node with the specified template on a page identified by its path.
     *
     * @param componentsTemplateName component template name
     * @param searchRoot page node path used as search root
     * @return first matching component node or {@code null} if none found or on error
     */
    public static Node findComponentOnPage(final String componentsTemplateName, final String searchRoot) {
        Node componentNode = null;
        List<String> areaRoots = retrieveChildAreas(searchRoot);
        final NodeIterator componentsIterator = findDescendantComponents(componentsTemplateName, areaRoots.toArray(new String[0]));
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
     * Finds the first descendant component node matching the given template below the provided search root path.
     *
     * @param componentsTemplateName component template name to match
     * @param searchRoot root path used to limit the search
     * @return first matching component node or {@code null} if none found or on error
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
     * Finds descendant component nodes matching the given template below one or more search root paths.
     *
     * @param componentsTemplateName component template name to match
     * @param searchRoots one or more root paths used to limit the search
     * @return iterator over matching component nodes or {@code null} on error
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
     * Creates a {@link Query} for the given statement, language and repository, optionally binding values (SQL2 only).
     *
     * @param queryStatement the query statement
     * @param language the query language identifier (e.g. {@code JCR_SQL2}, {@code XPATH})
     * @param bindValues mapping of parameter names to values (SQL2 only); may be {@code null} or empty
     * @param repository repository name
     * @return newly created query object or {@code null} on error
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
     * Convenience overload creating a {@link Query} on the default {@code website} repository.
     *
     * @param queryStatement the query statement
     * @param language query language identifier
     * @param bindValues mapping of parameter names to values (SQL2 only); may be {@code null} or empty
     * @return query object or {@code null} on error
     * @see #createQuery(String, String, Map, String)
     */
    public static Query createQuery(final String queryStatement, final String language, final Map<String, Value> bindValues) {
        return createQuery(queryStatement, language, bindValues, WEBSITE);
    }

    /**
     * Convenience factory for a SQL2 {@link Query} on the default repository.
     *
     * @param queryStatement the SQL2 query statement
     * @param bindValues mapping of parameter names to values; may be {@code null} or empty
     * @return query object or {@code null} on error
     * @see #createQuery(String, String, Map, String)
     */
    public static Query createSqlQuery(final String queryStatement, final Map<String, Value> bindValues) {
        return createQuery(queryStatement, JCR_SQL2, bindValues, WEBSITE);
    }

    /**
     * Convenience factory for an XPath {@link Query} on the default repository.
     *
     * @param queryStatement the XPath query statement
     * @return query object or {@code null} on error
     * @see #createQuery(String, String, Map, String)
     */
    public static Query createXPathQuery(final String queryStatement) {
        return createQuery(queryStatement, XPATH, null, WEBSITE);
    }

    /**
     * Executes a multi-selector query and retrieves nodes identified by the given selector name.
     *
     * @param query the prepared query object (may be {@code null})
     * @param selectorName selector name used to extract the node from each row
     * @return list of nodes matching the selector or empty list if query is {@code null}; {@code null} entries are not added
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

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private NodeQueryUtil() {
        //private constructor
    }
}
