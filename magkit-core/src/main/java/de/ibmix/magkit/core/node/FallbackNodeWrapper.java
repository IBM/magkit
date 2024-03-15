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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FallbackNodeWrapper extends NullableDelegateNodeWrapper {

    private List<Node> _fallbackNodes;
    private Predicate<Property> _propertyCondition;
    private Predicate<Iterator> _iteratorCondition;
    private Map<String, String[]> _propertyNameFallbacks;

    public static FallbackNodeWrapper forNode(final Node wrapped) {
        return new FallbackNodeWrapper(wrapped);
    }

    public FallbackNodeWrapper(final Node wrapped) {
        super(wrapped);
        _propertyCondition = property -> StringUtils.isNotEmpty(PropertyUtils.getStringValue(property));
        _iteratorCondition = iterator -> Objects.nonNull(iterator) && iterator.hasNext();
        _propertyNameFallbacks = new HashMap<>();
    }

    public FallbackNodeWrapper(String name, String primaryNodeType) {
        super(name, primaryNodeType);
        _propertyCondition = property -> StringUtils.isNotEmpty(PropertyUtils.getStringValue(property));
        _iteratorCondition = iterator -> Objects.nonNull(iterator) && iterator.hasNext();
        _propertyNameFallbacks = new HashMap<>();
    }

    public FallbackNodeWrapper withPropertyCondition(Predicate<Property> predicate) {
        Validate.notNull(predicate);
        _propertyCondition = predicate;
        return this;
    }

    public FallbackNodeWrapper withIteratorCondition(Predicate<Iterator> predicate) {
        Validate.notNull(predicate);
        _iteratorCondition = predicate;
        return this;
    }

    public FallbackNodeWrapper withFallbackNodes(Node... fallbackNodes) {
        Validate.notNull(fallbackNodes);
        _fallbackNodes = Arrays.stream(fallbackNodes).filter(Objects::nonNull).collect(Collectors.toList());
        return this;
    }

    public FallbackNodeWrapper withPropertyNameFallbacks(String propertyName, String... fallbackPropertyNames) {
        Validate.notEmpty(propertyName);
        Validate.notEmpty(fallbackPropertyNames);
        _propertyNameFallbacks.put(propertyName, fallbackPropertyNames);
        return this;
    }

    @Override
    public Node getNode(String relPath) throws RepositoryException {
        Node result = super.getNode(relPath);
        if (result != null) {
            result = _fallbackNodes.stream().map(n -> NodeUtils.getChildNode(n, relPath)).filter(Objects::nonNull).findFirst().get();
        }
        return result;
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return getNodes(NodeUtils::getNodes);
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return getNodes(n -> NodeUtils.getNodes(n, namePattern));
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return getNodes(n -> NodeUtils.getNodes(n, nameGlobs));
    }

    NodeIterator getNodes(Function<Node, NodeIterator> iteratorFunction) {
        NodeIterator result = iteratorFunction.apply(getWrappedNode());
        if (!_iteratorCondition.test(result)) {
            // TODO: Support filtering out hidden nodes by their name
            result = _fallbackNodes.stream().map(iteratorFunction).filter(_iteratorCondition).findFirst().get();
        }
        return result;
    }

    @Override
    public Property getProperty(String relPath) throws RepositoryException {
        Property result = getPropertyWithNameFallbacks(getWrappedNode(), relPath);
        if (!_propertyCondition.test(result)) {
            result = _fallbackNodes.stream().map(n -> getPropertyWithNameFallbacks(n, relPath)).filter(_propertyCondition).findFirst().get();
        }
        return result;
    }

    Property getPropertyWithNameFallbacks(Node node, String relPath) {
        Property result = PropertyUtils.getProperty(node, relPath);
        if (!_propertyCondition.test(result) && _propertyNameFallbacks.containsKey(relPath)) {
            String[] fallbackNames = _propertyNameFallbacks.get(relPath);
            result = Arrays.stream(fallbackNames).map(name -> PropertyUtils.getProperty(node, name)).filter(_propertyCondition).findFirst().get();
        }
        return result;
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return getProperties(PropertyUtils::getProperties);
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return getProperties(n-> PropertyUtils.getProperties(n, namePattern));
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        return getProperties(n-> PropertyUtils.getProperties(n, nameGlobs));
    }

    PropertyIterator getProperties(Function<Node, PropertyIterator> iteratorFunction) {
        PropertyIterator result = iteratorFunction.apply(getWrappedNode());
        if (!_iteratorCondition.test(result)) {
            // TODO: Support filtering out hidden properties by their name
            result = _fallbackNodes.stream().map(iteratorFunction).filter(_iteratorCondition).findFirst().get();
        }
        return result;
    }
}
