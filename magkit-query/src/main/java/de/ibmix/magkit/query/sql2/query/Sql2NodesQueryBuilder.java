package de.ibmix.magkit.query.sql2.query;

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

import de.ibmix.magkit.query.sql2.query.jcrwrapper.NodesQuery;
import de.ibmix.magkit.query.sql2.statement.Sql2Builder;
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
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-21
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
