package com.aperto.magkit.query.sql2.query;

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

import com.aperto.magkit.query.sql2.statement.Sql2Builder;
import info.magnolia.context.MgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

/**
 * The base class for row and node query builders.
 *
 * @author wolf.bubenik@aperto.com
 * @since 28.02.2020
 **/
public abstract class Sql2QueryBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(Sql2QueryBuilder.class);

    private String _workspace;
    private long _limit = 0;
    private long _offset = 0;
    private Sql2Builder _statementBuilder;

    protected Sql2QueryBuilder(){}

    public static QueryWorkspace<QueryNodesStatement<NodesQueryBuilder>> forNodes() {
        return new Sql2NodesQueryBuilder();
    }

    public static QueryWorkspace<QueryRowsStatement<RowsQueryBuilder>> forRows() {
        return new Sql2RowsQueryBuilder();
    }

    protected void setWorkspace(String workspace) {
        _workspace = workspace;
    }

    protected void setLimit(long limit) {
        _limit = Math.max(limit, 0);
    }

    protected void setOffset(long offset) {
        _offset = Math.max(offset, 0);
    }

    public void setStatementBuilder(Sql2Builder statementBuilder) {
        _statementBuilder = statementBuilder;
    }

    protected Query getQuery() {
        Query query = null;
        try {
            final Session jcrSession = MgnlContext.getJCRSession(_workspace);
            final QueryManager queryManager = jcrSession.getWorkspace().getQueryManager();
            query = queryManager.createQuery(_statementBuilder.build(), Query.JCR_SQL2);
            if (_limit > 0) {
                query.setLimit(_limit);
            }
            if (_offset > 0) {
                query.setOffset(_offset);
            }
        } catch (RepositoryException e) {
            LOG.error("Could not create query object. Return NULL", e);
        }
        return query;
    }
}
