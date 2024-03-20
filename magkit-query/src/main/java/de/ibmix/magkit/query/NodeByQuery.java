package de.ibmix.magkit.query;

/*-
 * #%L
 * magkit-query
 * %%
 * Copyright (C) 2023 - 2024 IBM iX
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

import de.ibmix.magkit.query.sql2.Sql2;
import de.ibmix.magkit.query.sql2.condition.Sql2StringCondition;
import de.ibmix.magkit.query.sql2.statement.Sql2Builder;
import de.ibmix.magkit.query.sql2.statement.Sql2Statement;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;
import java.util.List;
import java.util.function.Function;

/**
 * Retrieves node by query for a property.
 *
 * @author frank.sommer
 * @since 02.02.2024
 */
public class NodeByQuery implements Function<String, Node> {

    private final String _workspaceName;
    private final String _nodeType;
    private final String _propertyName;

    public NodeByQuery(String workspaceName, String nodeType, String propertyName) {
        _workspaceName = workspaceName;
        _nodeType = nodeType;
        _propertyName = propertyName;
    }

    @Override
    public Node apply(String value) {
        final Sql2Builder sql2Builder = Sql2Statement.select().from(_nodeType)
            .whereAll(Sql2StringCondition.property(_propertyName).equalsAny().values(value))
            .orderBy(NodeTypes.LastModified.NAME);
        final List<Node> resultNodes = Sql2.Query.nodesFrom(_workspaceName).withStatement(sql2Builder).withLimit(1).getResultNodes();
        return resultNodes.isEmpty() ? null : resultNodes.get(0);
    }
}
