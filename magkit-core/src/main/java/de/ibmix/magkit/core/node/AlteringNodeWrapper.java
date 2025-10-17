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
import java.util.function.Predicate;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * A flexible {@link NullableDelegateNodeWrapper} allowing callers to overlay (override, hide, add) properties
 * and child nodes on top of a real wrapped JCR {@link Node} without persisting changes to the repository.
 * <p>Key features:</p>
 * <ul>
 *   <li>Programmatic stubbing of single- or multi-valued properties across supported JCR types.</li>
 *   <li>Ability to hide existing properties or child nodes from read operations.</li>
 *   <li>Injection of synthetic child nodes while preserving hierarchical semantics via {@link DefineParentNodeWrapper}.</li>
 *   <li>Fallback chaining to ancestor or referenced nodes for graceful content resolution.</li>
 *   <li>Conversion to immutable view via {@link #immutable()} for defensive exposure.</li>
 * </ul>
 * <p>Usage example:</p>
 * <pre>{@code
 * AlteringNodeWrapper wrapper = new AlteringNodeWrapper(node)
 *     .withProperty("title", "Custom Title")
 *     .withHiddenProperty("internalFlag")
 *     .withChildNode("virtual", someOtherNode)
 *     .withFallbackToPage()
 *     .immutable();
 * Property title = wrapper.getProperty("title");
 * }</pre>
 * <p>Null & error handling: Builder methods validate required arguments. Repository access exceptions from delegated
 * calls propagate unchanged. Hidden elements are simply excluded from merged iterators.</p>
 * <p>Thread-safety: Not thread-safe – internal maps and sets are mutable. Restrict usage to single-threaded request
 * scope or externally synchronize.</p>
 * <p>Side effects: No writes are performed to the underlying JCR repository; all alterations are in-memory only.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2019-05-19
 */
public class AlteringNodeWrapper extends NullableDelegateNodeWrapper {
    private final Map<String, Property> _properties;
    private final Map<String, Node> _childNodes;
    private final Set<String> _hiddenProperties;
    private final Set<String> _hiddenChildNodes;

    /**
     * Construct a wrapper overlaying an existing node.
     *
     * @param nodeToWrap the underlying JCR node (must not be null)
     */
    public AlteringNodeWrapper(Node nodeToWrap) {
        super(nodeToWrap);
        notNull(nodeToWrap);
        _properties = new LinkedHashMap<>();
        _childNodes = new LinkedHashMap<>();
        _hiddenProperties = new HashSet<>();
        _hiddenChildNodes = new HashSet<>();
    }

    /**
     * Construct a purely synthetic node wrapper using a given name and primary node type.
     *
     * @param name node name
     * @param primaryNodeType primary node type name
     */
    public AlteringNodeWrapper(String name, String primaryNodeType) {
        super(name, primaryNodeType);
        _properties = new LinkedHashMap<>();
        _childNodes = new LinkedHashMap<>();
        _hiddenProperties = new HashSet<>();
        _hiddenChildNodes = new HashSet<>();
    }

    /**
     * Stub a String property (single or multi valued).
     *
     * @param name property name (must not be blank)
     * @param value values to expose (multi-valued if length > 1)
     * @return this for fluent chaining
     */
    public AlteringNodeWrapper withProperty(String name, String... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(getWrappedNode(), name, value);
        _properties.put(name, property);
        return this;
    }

    /**
     * Stub a Boolean property (single or multi valued).
     *
     * @param name property name
     * @param value boolean values
     * @return fluent API instance
     */
    public AlteringNodeWrapper withProperty(String name, Boolean... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(getWrappedNode(), name, value);
        _properties.put(name, property);
        return this;
    }

    /**
     * Stub a Long property (single or multi valued).
     *
     * @param name property name
     * @param value long values
     * @return fluent API instance
     */
    public AlteringNodeWrapper withProperty(String name, Long... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(getWrappedNode(), name, value);
        _properties.put(name, property);
        return this;
    }

    /**
     * Stub a Double property (single or multi valued).
     *
     * @param name property name
     * @param value double values
     * @return fluent API instance
     */
    public AlteringNodeWrapper withProperty(String name, Double... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(getWrappedNode(), name, value);
        _properties.put(name, property);
        return this;
    }

    /**
     * Stub a Calendar property (single or multi valued).
     *
     * @param name property name
     * @param value calendar values
     * @return fluent API instance
     */
    public AlteringNodeWrapper withProperty(String name, Calendar... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(getWrappedNode(), name, value);
        _properties.put(name, property);
        return this;
    }

    /**
     * Convenience stub for the Magnolia template property.
     *
     * @param templateId template identifier value
     * @return fluent API instance
     */
    public AlteringNodeWrapper withTemplate(String templateId) {
        notEmpty(templateId);
        return withProperty(NodeTypes.Renderable.TEMPLATE, templateId);
    }

    /**
     * Hide one or more existing or stubbed properties from subsequent read access. Null names are ignored.
     *
     * @param names property names to hide
     * @return fluent API instance
     */
    public AlteringNodeWrapper withHiddenProperty(String... names) {
        notEmpty(names);
        Arrays.stream(names).filter(Objects::nonNull).forEach(_hiddenProperties::add);
        return this;
    }

    /**
     * Inject a synthetic child node (wrapped to maintain hierarchy semantics).
     *
     * @param name child node name
     * @param childNode real JCR node to wrap
     * @return fluent API instance
     */
    public AlteringNodeWrapper withChildNode(String name, Node childNode) {
        notEmpty(name);
        notNull(childNode);
        _childNodes.put(name, new DefineParentNodeWrapper(this, childNode));
        return this;
    }

    /**
     * Hide child nodes by name from iterators and direct resolution. Null names are ignored.
     *
     * @param names child node names to hide
     * @return fluent API instance
     */
    public AlteringNodeWrapper withHiddenNode(String... names) {
        notEmpty(names);
        Arrays.stream(names).filter(Objects::nonNull).forEach(_hiddenChildNodes::add);
        return this;
    }

    /**
     * Attach a generic fallback chain wrapper (initially containing only the current wrapped node).
     *
     * @return created fallback wrapper (also set as new wrapped node)
     */
    public FallbackNodeWrapper withFallback() {
        FallbackNodeWrapper result = new FallbackNodeWrapper(getWrappedNode());
        setWrappedNode(result);
        return result;
    }

    /**
     * Convenience: configure fallback chain to nearest ancestor page (Magnolia Page node).
     *
     * @return configured fallback wrapper
     */
    public FallbackNodeWrapper withFallbackToPage() {
        return withFallbackToAncestor(NodeUtils.IS_PAGE);
    }

    /**
     * Configure fallback chain to the first ancestor (or self) matching predicate.
     *
     * @param ancestorPredicate predicate selecting target ancestor
     * @return configured fallback wrapper
     */
    public FallbackNodeWrapper withFallbackToAncestor(final Predicate<Node> ancestorPredicate) {
        Node fallbackNode = NodeUtils.getAncestorOrSelf(getWrappedNode(), ancestorPredicate);
        return withFallback().withFallbackNodes(fallbackNode);
    }

    /**
     * Configure fallback chain using a referenced node id stored in a property.
     *
     * @param workspace Magnolia workspace name of the reference
     * @param linkPropertyName property holding the referenced node identifier
     * @return configured fallback wrapper
     */
    public FallbackNodeWrapper withFallbackToReference(final String workspace, final String linkPropertyName) {
        String nodeId = PropertyUtils.getStringValue(getWrappedNode(), linkPropertyName);
        return withFallback().withFallbackNodes(NodeUtils.getNodeByReference(workspace, nodeId));
    }

    /**
     * Convert underlying view to immutable wrapper (mutating operations will throw).
     *
     * @return fluent API instance
     */
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
        return mergeAndFilterNodes(super.getNodes());
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return mergeAndFilterNodes(super.getNodes(namePattern));
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return mergeAndFilterNodes(super.getNodes(nameGlobs));
    }

    /**
     * Merge original child node iterator with injected synthetic child nodes and filter out hidden ones.
     *
     * @param nodes base iterator from underlying wrapped node
     * @return merged iterator excluding hidden names
     * @throws RepositoryException propagation from iterator consumption
     */
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

    /**
     * Merge original property iterator with stubbed properties while removing hidden ones.
     *
     * @param properties base iterator from underlying wrapped node
     * @return merged iterator excluding hidden names
     * @throws RepositoryException propagation from iterator consumption
     */
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
