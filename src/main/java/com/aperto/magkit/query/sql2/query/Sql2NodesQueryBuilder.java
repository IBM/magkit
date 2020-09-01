package com.aperto.magkit.query.sql2.query;

import com.aperto.magkit.query.sql2.query.jcrwrapper.NodesQuery;
import com.aperto.magkit.query.sql2.statement.Sql2Builder;
import info.magnolia.repository.RepositoryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;

/**
 * The builder class for Node queries.
 *
 * @author wolf.bubenik@aperto.com
 * @since (21.8.2020)
 */
public class Sql2NodesQueryBuilder extends Sql2QueryBuilder implements QueryWorkspace<QueryNodesStatement<NodesQueryBuilder>>, QueryNodesStatement<NodesQueryBuilder>, NodesQueryBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(Sql2NodesQueryBuilder.class);

    public Sql2NodesQueryBuilder() {
        super();
    }

    public QueryNodesStatement<NodesQueryBuilder> fromWebsite() {
        return fromWorkspace(RepositoryConstants.WEBSITE);
    }

    public QueryNodesStatement<NodesQueryBuilder> fromWorkspace(String workspace) {
        setWorkspace(workspace);
        return me();
    }

    public NodesQueryBuilder withLimit(long limit) {
        setLimit(limit);
        return me();
    }

    public NodesQueryBuilder withOffset(long offset) {
        setOffset(offset);
        return me();
    }

    public NodesQueryBuilder withStatement(Sql2Builder statementBuilder) {
        setStatementBuilder(statementBuilder);
        return me();
    }

    public NodesQueryBuilder withStatement(final String sql2) {
        return withStatement(() -> sql2);
    }

    public NodesQuery buildNodesQuery() {
        return new NodesQuery(getQuery());
    }

    public List<Node> getResultNodes() {
        List<Node> nodes = Collections.emptyList();
        try {
            nodes = buildNodesQuery().execute().getNodeList();
        } catch (RepositoryException e) {
            LOG.warn("Failed to get query result nodes. Returning empty list.", e);
        }
        return nodes;
    }

    public Sql2NodesQueryBuilder me() {
        return this;
    }
}