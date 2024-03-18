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

import info.magnolia.jcr.util.NodeTypes;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.PropertyIteratorAdapter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * A NodeWrapper that allows to override properties and child nodes as desired by the user.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2019-05-19
 */
public class AlteringNodeWrapper extends NullableDelegateNodeWrapper {
    private final Map<String, Property> _properties;
    private final Map<String, Node> _childNodes;
    private final Set<String> _hiddenProperties;
    private final Set<String> _hiddenChildNodes;



    public AlteringNodeWrapper(Node nodeToWrap) {
        super(nodeToWrap);
        notNull(nodeToWrap);
        _properties = new LinkedHashMap<>();
        _childNodes = new LinkedHashMap<>();
        _hiddenProperties = new HashSet<>();
        _hiddenChildNodes = new HashSet<>();
    }

    public AlteringNodeWrapper(String name, String primaryNodeType) {
        super(name, primaryNodeType);
        _properties = new LinkedHashMap<>();
        _childNodes = new LinkedHashMap<>();
        _hiddenProperties = new HashSet<>();
        _hiddenChildNodes = new HashSet<>();
    }

    public AlteringNodeWrapper withProperty(String name, String... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(getWrappedNode(), name, value);
        _properties.put(name, property);
        return this;
    }

    public AlteringNodeWrapper withProperty(String name, Boolean... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(getWrappedNode(), name, value);
        _properties.put(name, property);
        return this;
    }

    public AlteringNodeWrapper withProperty(String name, Long... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(getWrappedNode(), name, value);
        _properties.put(name, property);
        return this;
    }

    public AlteringNodeWrapper withProperty(String name, Double... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(getWrappedNode(), name, value);
        _properties.put(name, property);
        return this;
    }

    public AlteringNodeWrapper withProperty(String name, Calendar... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(getWrappedNode(), name, value);
        _properties.put(name, property);
        return this;
    }

    public AlteringNodeWrapper withTemplate(String templateId) {
        notEmpty(templateId);
        return withProperty(NodeTypes.Renderable.TEMPLATE, templateId);
    }

    public AlteringNodeWrapper withHiddenProperty(String... names) {
        notEmpty(names);
        Arrays.stream(names).filter(Objects::isNull).forEach(_hiddenProperties::add);
        return this;
    }

    public AlteringNodeWrapper withChildNode(String name, Node childNode) {
        notEmpty(name);
        notNull(childNode);
        _childNodes.put(name, new DefineParentNodeWrapper(this, childNode));
        return this;
    }

    public AlteringNodeWrapper withHiddenNode(String... names) {
        notEmpty(names);
        Arrays.stream(names).filter(Objects::isNull).forEach(_hiddenChildNodes::add);
        return this;
    }

    public FallbackNodeWrapper withFallbacks() {
        FallbackNodeWrapper result = new FallbackNodeWrapper(getWrappedNode());
        setWrappedNode(result);
        return result;
    }

    public AlteringNodeWrapper immutable() {
        setWrappedNode(new ImmutableNodeWrapper(getWrappedNode()));
        return this;
    }

    @Override
    public Property getProperty(String relPath) throws RepositoryException {
        Property result = null;
        if (!_hiddenProperties.contains(relPath)) {
            result = _properties.getOrDefault(relPath, super.getProperty(relPath));
        }
        return result;
    }

    @Override
    public boolean hasProperty(String relPath) throws RepositoryException {
        return !_hiddenProperties.contains(relPath) && (_properties.containsKey(relPath) || super.hasProperty(relPath));
    }

    @Override
    public boolean hasNode(String relPath) throws RepositoryException {
        return !_hiddenChildNodes.contains(relPath) && (_childNodes.containsKey(relPath) || super.hasNode(relPath));
    }

    @Override
    public Node getNode(String relPath) throws RepositoryException {
        Node result = null;
        if (!_hiddenChildNodes.contains(relPath)) {
            result = _childNodes.getOrDefault(relPath, super.getNode(relPath));
        }
        return result;
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return mergeAndFilterProperties(super.getProperties());
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return mergeAndFilterProperties(super.getProperties(namePattern));
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        return mergeAndFilterProperties(super.getProperties(nameGlobs));
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return new NodeIteratorAdapter(mergeAndFilterNodes(super.getNodes()));
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return new NodeIteratorAdapter(mergeAndFilterNodes(super.getNodes(namePattern)));
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return new NodeIteratorAdapter(mergeAndFilterNodes(super.getNodes(nameGlobs)));
    }

    // TODO: add filter predicate to method (name patterns) and filter custom nodes accordingly
    private NodeIterator mergeAndFilterNodes(NodeIterator nodes) throws RepositoryException {
        Map<String, Node> mergedNodes = new LinkedHashMap<>();
        while (nodes.hasNext()) {
            Node n = nodes.nextNode();
            if (!_hiddenChildNodes.contains(n.getName())) {
                mergedNodes.put(n.getName(), n);
            }
        }

        mergedNodes.putAll(_childNodes);
        return new NodeIteratorAdapter(mergedNodes.values());
    }

    // TODO: add filter predicate to method (name patterns) and filter custom properties accordingly
    private PropertyIterator mergeAndFilterProperties(final PropertyIterator properties) throws RepositoryException {
        Map<String, Property> nodeProperties = new LinkedHashMap<>();
        while (properties.hasNext()) {
            Property property = properties.nextProperty();
            if (!_hiddenProperties.contains(property.getName())) {
                nodeProperties.put(property.getName(), property);
            }
        }
        nodeProperties.putAll(_properties);
        return new PropertyIteratorAdapter(nodeProperties.values());
    }

}
