package com.aperto.magkit.query.sql2.query;

import com.aperto.magkit.query.sql2.query.jcrwrapper.RowsQuery;
import com.aperto.magkit.query.sql2.statement.Sql2Builder;
import info.magnolia.repository.RepositoryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.Row;
import java.util.Collections;
import java.util.List;

public class Sql2RowsQueryBuilder extends Sql2QueryBuilder implements QueryWorkspace<QueryRowsStatement<RowsQueryBuilder>>, QueryRowsStatement<RowsQueryBuilder>, RowsQueryBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(Sql2RowsQueryBuilder.class);

    public Sql2RowsQueryBuilder() {
        super();
    }

    public QueryRowsStatement<RowsQueryBuilder> fromWebsite() {
        return fromWorkspace(RepositoryConstants.WEBSITE);
    }

    public QueryRowsStatement<RowsQueryBuilder> fromWorkspace(String workspace) {
        setWorkspace(workspace);
        return me();
    }

    public RowsQueryBuilder withLimit(long limit) {
        setLimit(limit);
        return me();
    }

    public RowsQueryBuilder withOffset(long offset) {
        setOffset(offset);
        return me();
    }

    public RowsQueryBuilder withStatement(Sql2Builder statementBuilder) {
        setStatementBuilder(statementBuilder);
        return me();
    }

    public RowsQueryBuilder withStatement(final String sql2) {
        return withStatement(() -> sql2);
    }

    public RowsQuery buildRowsQuery() {
        return new RowsQuery(getQuery());
    }

    public List<Row> getResultRows() {
        List<Row> rows = Collections.emptyList();
        try {
            rows = buildRowsQuery().execute().getRowList();
        } catch (RepositoryException e) {
            LOG.warn("Failed to get query result rows. Returning empty list.", e);
        }
        return rows;
    }

    public Sql2RowsQueryBuilder me() {
        return this;
    }
}
