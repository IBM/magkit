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

import javax.jcr.Node;
import java.util.List;
import java.util.function.Function;

/**
 * Provides a convenience {@link Function} to retrieve a single JCR {@link Node} by querying a workspace for nodes
 * of a given primary type where a specific property equals a provided value.
 *
 * Main functionality:
 * <ul>
 *   <li>Executes a query via {@link NodesByQuery} and returns only the first result (if any).</li>
 *   <li>Abstracts away list handling when only a single match is desired.</li>
 * </ul>
 *
 * Key features:
 * <ul>
 *   <li>Returns {@code null} when no matching node is found (fails fast without exception).</li>
 *   <li>Immutable: workspace name, node type and property name are fixed after construction.</li>
 * </ul>
 *
 * Preconditions:
 * <ul>
 *   <li>The workspace must exist and be accessible.</li>
 *   <li>The property should be indexed or otherwise efficiently queryable for performance.</li>
 * </ul>
 *
 * Null & error handling:
 * <ul>
 *   <li>If the provided value is {@code null} or empty the underlying query may yield no results; {@code null} is returned.</li>
 *   <li>Repository related runtime exceptions are expected to be handled (or propagated) by {@link NodesByQuery}; this class adds no extra error handling.</li>
 * </ul>
 *
 * Usage example:
 * <pre>
 *   NodeByQuery byUuid = new NodeByQuery("magnolia", "mgnl:page", "uuid");
 *   Node page = byUuid.apply("1234-5678-90");
 *   if (page != null) {
 *       // operate on page
 *   }
 * </pre>
 *
 * Thread-safety:
 * <ul>
 *   <li>Instances are thread-safe assuming {@link NodesByQuery} usage is thread-safe for concurrent {@link #apply(String)} calls.</li>
 * </ul>
 *
 * @author frank.sommer
 * @since 02.02.2024
 */
public class NodeByQuery implements Function<String, Node> {

    private final String _workspaceName;
    private final String _nodeType;
    private final String _propertyName;

    /**
     * Creates a new instance with fixed query parameters.
     *
     * @param workspaceName the JCR workspace to search (must not be {@code null})
     * @param nodeType the primary node type to restrict the query (must not be {@code null})
     * @param propertyName the property name whose value must match the supplied argument in {@link #apply(String)} (must not be {@code null})
     */
    public NodeByQuery(String workspaceName, String nodeType, String propertyName) {
        _workspaceName = workspaceName;
        _nodeType = nodeType;
        _propertyName = propertyName;
    }

    /**
     * Executes the query for the configured workspace, node type and property name using the supplied value and
     * returns the first matching node or {@code null} if none is found.
     *
     * @param value the property value to match; may be {@code null}
     * @return the first matching {@link Node} or {@code null} when no match exists
     */
    @Override
    public Node apply(String value) {
        final List<Node> resultNodes = new NodesByQuery(_workspaceName, _nodeType, _propertyName).apply(value);
        return resultNodes.isEmpty() ? null : resultNodes.get(0);
    }
}
