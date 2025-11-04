package de.ibmix.magkit.core.node;

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

import de.ibmix.magkit.core.utils.NodeUtils;
import de.ibmix.magkit.core.utils.PropertyUtils;
import info.magnolia.jcr.util.NodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.PropertyIteratorAdapter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A {@link NullableDelegateNodeWrapper} that transparently resolves properties and child nodes from a primary wrapped
 * {@link Node} with graceful fallback to a chain of alternative nodes. This enables dynamic content resolution where
 * missing or empty values should automatically be substituted from ancestor or reference nodes.
 * <p>Key features:</p>
 * <ul>
 *   <li>Configurable fallback node chain (ordered) via {@link #withFallbackNodes(Node...)}.</li>
 *   <li>Conditional property evaluation using a predicate (non-empty by default).</li>
 *   <li>Conditional iterator evaluation (must have at least one element by default).</li>
 *   <li>Property name fallback mapping: attempt multiple alternative property names before giving up.</li>
 *   <li>Non-intrusive: no repository writes, purely read-overlay semantics.</li>
 * </ul>
 * <p>Usage example:</p>
 * <pre>{@code
 * FallbackNodeWrapper wrapper = FallbackNodeWrapper.forNode(primary)
 *     .withFallbackNodes(secondary, tertiary)
 *     .withPropertyNameFallbacks("title", "displayTitle", "altTitle")
 *     .withPropertyCondition(p -> StringUtils.isNotBlank(PropertyUtils.getStringValue(p)));
 * Property effectiveTitle = wrapper.getProperty("title");
 * }</pre>
 * <p>Null and error handling: Fallback arrays are filtered to non-null nodes. Repository exceptions from delegated calls
 * propagate unchanged. Missing properties or child nodes simply return null (or empty iterators).</p>
 * <p>Thread-safety: Not thread-safe; internal configuration maps and predicates are mutable. Confine usage to a single
 * request thread or externally synchronize.</p>
 * <p>Side effects: No modification of underlying nodes; all resolution is in-memory.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-17
 */
public class FallbackNodeWrapper extends NullableDelegateNodeWrapper {

    private static final NodeIterator EMPTY_NODE_ITERATOR = new NodeIteratorAdapter(Collections.emptyList());
    private static final PropertyIterator EMPTY_PROPERTY_ITERATOR = new PropertyIteratorAdapter(Collections.emptyList());

    private List<Node> _fallbackNodes;
    private Predicate<Property> _propertyCondition;
    private Predicate<Iterator<?>> _iteratorCondition;
    private Map<String, String[]> _propertyNameFallbacks;

    /**
     * Factory creating a fallback wrapper for a given primary node.
     *
     * @param wrapped the primary node to wrap
     * @return new instance with default predicates
     */
    public static FallbackNodeWrapper forNode(final Node wrapped) {
        return new FallbackNodeWrapper(wrapped);
    }

    /**
     * Construct with a real node as primary resolution source. Initializes default property and iterator predicates.
     *
     * @param wrapped primary node (must not be null)
     */
    public FallbackNodeWrapper(final Node wrapped) {
        super(wrapped);
        _propertyCondition = property -> StringUtils.isNotEmpty(PropertyUtils.getStringValue(property));
        _iteratorCondition = iterator -> Objects.nonNull(iterator) && iterator.hasNext();
        _propertyNameFallbacks = new HashMap<>();
        _fallbackNodes = Collections.emptyList();
    }

    /**
     * Construct a synthetic primary node wrapper.
     *
     * @param name synthetic node name
     * @param primaryNodeType primary node type name
     */
    public FallbackNodeWrapper(String name, String primaryNodeType) {
        super(name, primaryNodeType);
        _propertyCondition = property -> StringUtils.isNotEmpty(PropertyUtils.getStringValue(property));
        _iteratorCondition = iterator -> Objects.nonNull(iterator) && iterator.hasNext();
        _propertyNameFallbacks = new HashMap<>();
        _fallbackNodes = Collections.emptyList();
    }

    /**
     * Replace the property matching condition used before considering fallback nodes.
     *
     * @param predicate evaluation condition (must not be null)
     * @return fluent API instance
     */
    public FallbackNodeWrapper withPropertyCondition(Predicate<Property> predicate) {
        Validate.notNull(predicate);
        _propertyCondition = predicate;
        return this;
    }

    /**
     * Replace the iterator condition (used to decide whether to fallback to next node). Condition applied to the
     * iterator before evaluating fallback chain.
     *
     * @param predicate iterator evaluation function (must not be null)
     * @return fluent API instance
     */
    public FallbackNodeWrapper withIteratorCondition(Predicate<Iterator<?>> predicate) {
        Validate.notNull(predicate);
        _iteratorCondition = predicate;
        return this;
    }

    /**
     * Configure ordered list of fallback nodes (nulls filtered out).
     *
     * @param fallbackNodes candidate nodes in resolution order
     * @return fluent API instance
     */
    public FallbackNodeWrapper withFallbackNodes(Node... fallbackNodes) {
        Validate.notNull(fallbackNodes);
        _fallbackNodes = Arrays.stream(fallbackNodes).filter(Objects::nonNull).collect(Collectors.toList());
        return this;
    }

    /**
     * Add alternative property names to try if the primary name fails or is considered empty.
     *
     * @param propertyName primary property name
     * @param fallbackPropertyNames ordered alternative property names (must not be empty)
     * @return fluent API instance
     */
    public FallbackNodeWrapper withPropertyNameFallbacks(String propertyName, String... fallbackPropertyNames) {
        Validate.notEmpty(propertyName);
        Validate.notEmpty(fallbackPropertyNames);
        _propertyNameFallbacks.put(propertyName, fallbackPropertyNames);
        return this;
    }

    /**
     * Resolve child node, falling back to first alternative that contains it if missing on primary.
     *
     * @param relPath relative child path
     * @return resolved node or null if none found
     * @throws RepositoryException on repository access issues
     */
    @Override
    public Node getNode(String relPath) throws RepositoryException {
        Node result = super.getNode(relPath);
        if (result == null) {
            result = _fallbackNodes.stream().map(n -> NodeUtils.getChildNode(n, relPath)).filter(Objects::nonNull).findFirst().orElse(null);
        }
        return result;
    }

    /**
     * Return iterator of child nodes, falling back if primary iterator does not satisfy the iterator condition.
     *
     * @return node iterator (possibly empty)
     * @throws RepositoryException on repository access issues
     */
    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return getNodes(NodeUtils::getNodes);
    }

    /**
     * Return filtered iterator by name pattern with fallback support.
     *
     * @param namePattern JCR name pattern
     * @return node iterator (possibly empty)
     * @throws RepositoryException on repository access issues
     */
    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return getNodes(n -> NodeUtils.getNodes(n, namePattern));
    }

    /**
     * Return filtered iterator by glob patterns with fallback support.
     *
     * @param nameGlobs glob patterns
     * @return node iterator (possibly empty)
     * @throws RepositoryException on repository access issues
     */
    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return getNodes(n -> NodeUtils.getNodes(n, nameGlobs));
    }

    /**
     * Internal helper executing an iterator function on primary node and applying fallback chain if iterator
     * fails the iterator condition.
     *
     * @param iteratorFunction function producing an iterator from a node
     * @return primary iterator if condition passes else first fallback satisfying condition or empty iterator
     */
    NodeIterator getNodes(Function<Node, NodeIterator> iteratorFunction) {
        NodeIterator initial = iteratorFunction.apply(getWrappedNode());
        List<Node> primaryNodes = NodeUtil.asList(NodeUtil.asIterable(initial));
        NodeIterator result = new NodeIteratorAdapter(primaryNodes);
        if (!_iteratorCondition.test(new NodeIteratorAdapter(primaryNodes))) {
            for (Node fallback : _fallbackNodes) {
                NodeIterator fbIt = iteratorFunction.apply(fallback);
                List<Node> fbList = NodeUtil.asList(NodeUtil.asIterable(fbIt));
                if (_iteratorCondition.test(new NodeIteratorAdapter(fbList))) {
                    result = new NodeIteratorAdapter(fbList);
                    return result;
                }
            }
            result = EMPTY_NODE_ITERATOR;
        }
        return result;
    }

    /**
     * Resolve property, considering name fallbacks and node fallbacks based on the property condition.
     *
     * @param relPath property name
     * @return resolved property or null
     * @throws RepositoryException on repository access issues
     */
    @Override
    public Property getProperty(String relPath) throws RepositoryException {
        Property result = getPropertyWithNameFallbacks(getWrappedNode(), relPath);
        if (!_propertyCondition.test(result)) {
            result = _fallbackNodes.stream().map(n -> getPropertyWithNameFallbacks(n, relPath)).filter(_propertyCondition).findFirst().orElse(null);
        }
        return result;
    }

    /**
     * Attempt to resolve property from a given node; if empty and fallback names are configured, try those in order.
     *
     * @param node node to inspect
     * @param relPath primary property name
     * @return first non-empty property or null
     */
    Property getPropertyWithNameFallbacks(Node node, String relPath) {
        Property result = PropertyUtils.getProperty(node, relPath);
        if (!_propertyCondition.test(result) && _propertyNameFallbacks.containsKey(relPath)) {
            String[] fallbackNames = _propertyNameFallbacks.get(relPath);
            result = Arrays.stream(fallbackNames).map(name -> PropertyUtils.getProperty(node, name)).filter(_propertyCondition).findFirst().orElse(null);
        }
        return result;
    }

    /**
     * Return property iterator; fallback to next node if iterator fails condition.
     *
     * @return property iterator (possibly empty)
     * @throws RepositoryException on repository access issues
     */
    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return getProperties(PropertyUtils::getProperties);
    }

    /**
     * Return filtered property iterator by pattern with fallback support.
     *
     * @param namePattern property name pattern
     * @return property iterator (possibly empty)
     * @throws RepositoryException on repository access issues
     */
    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return getProperties(n-> PropertyUtils.getProperties(n, namePattern));
    }

    /**
     * Return filtered property iterator by glob names with fallback support.
     *
     * @param nameGlobs glob patterns
     * @return property iterator (possibly empty)
     * @throws RepositoryException on repository access issues
     */
    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        return getProperties(n-> PropertyUtils.getProperties(n, nameGlobs));
    }

    /**
     * Internal helper executing an iterator function on primary node and applying fallback chain if iterator
     * fails the iterator condition.
     *
     * @param iteratorFunction function producing a property iterator from a node
     * @return primary property iterator if condition passes else first fallback satisfying condition or empty iterator
     */
    PropertyIterator getProperties(Function<Node, PropertyIterator> iteratorFunction) {
        PropertyIterator initial = iteratorFunction.apply(getWrappedNode());
        List<Property> primaryProps = toPropertyList(initial);
        PropertyIterator result = new PropertyIteratorAdapter(primaryProps);
        if (!_iteratorCondition.test(new PropertyIteratorAdapter(primaryProps))) {
            for (Node fallback : _fallbackNodes) {
                PropertyIterator fbIt = iteratorFunction.apply(fallback);
                List<Property> fbList = toPropertyList(fbIt);
                if (_iteratorCondition.test(new PropertyIteratorAdapter(fbList))) {
                    result = new PropertyIteratorAdapter(fbList);
                    return result;
                }
            }
            result = EMPTY_PROPERTY_ITERATOR;
        }
        return result;
    }

    private List<Property> toPropertyList(PropertyIterator iterator) {
        List<Property> result = Collections.emptyList();
        if (iterator != null) {
            List<Property> list = new java.util.ArrayList<>();
            while (iterator.hasNext()) {
                list.add(iterator.nextProperty());
            }
            result = list;
        }
        return result;
    }
}
