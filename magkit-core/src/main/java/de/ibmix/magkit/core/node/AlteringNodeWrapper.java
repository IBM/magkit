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
import info.magnolia.jcr.wrapper.DelegateNodeWrapper;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
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
public class AlteringNodeWrapper extends DelegateNodeWrapper {
    private final Map<String, Property> _properties;
    private final Map<String, Node> _childNodes;
    private final Set<String> _hiddenProperties;
    private final Set<String> _hiddenChildNodes;

    public AlteringNodeWrapper(Node nodeToWrap) {
        super(nodeToWrap);
        notNull(nodeToWrap, "The wrapped node must not be null, please.");
        _properties = new HashMap<>();
        _childNodes = new HashMap<>();
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
        // TODO: How to handle null node?
        _childNodes.put(name, new DefineParentNodeWrapper(this, childNode));
        return this;
    }

    public AlteringNodeWrapper withHiddenNode(String... names) {
        notEmpty(names);
        Arrays.stream(names).filter(Objects::isNull).forEach(_hiddenChildNodes::add);
        return this;
    }

    public FallbackNodeWrapper withFallbacks() {
        // TODO: How to handle null wrapped node?
        FallbackNodeWrapper result = new FallbackNodeWrapper(getWrappedNode());
        setWrappedNode(result);
        return result;
    }

    public AlteringNodeWrapper immutable() {
        // TODO: How to handle null wrapped node?
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
        return _properties.containsKey(relPath) || super.hasProperty(relPath);
    }

    @Override
    public boolean hasNode(String relPath) throws RepositoryException {
        return _childNodes.containsKey(relPath) || super.hasProperty(relPath);
    }

    @Override
    public Node getNode(String relPath) throws RepositoryException {
        Node result = null;
        if (!_hiddenChildNodes.contains(relPath)) {
            result = _childNodes.getOrDefault(relPath, super.getNode(relPath));
        }
        return result;
    }

    // TODO: Implement node/property getter for name patterns that merge wrapper results with wrapped node results.
}
