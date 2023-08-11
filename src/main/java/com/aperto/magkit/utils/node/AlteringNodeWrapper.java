package com.aperto.magkit.utils.node;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.wrapper.DelegateNodeWrapper;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * A read-only NodeWrapper that allows to override properties as desired by the user.
 *
 * @author wolf
 */
public class AlteringNodeWrapper extends DelegateNodeWrapper {
    private Map<String, Property> _overrides;
    private Map<String, String> _mapped;
    private Node _nodeToWrap;

    public AlteringNodeWrapper(Node nodeToWrap) {
        super(nodeToWrap);
        notNull(nodeToWrap, "The wrapped node must not be null, please.");
        _overrides = new HashMap<>();
        _mapped = new HashMap<>();
        _nodeToWrap = nodeToWrap;
    }

    protected Node getNodeToWrap() {
        return _nodeToWrap;
    }

    @SuppressWarnings("unchecked")
    public <R extends AlteringNodeWrapper> R withMappedProperty(String destName, String sourceName) {
        notEmpty(destName);
        notEmpty(sourceName);
        _mapped.put(destName, sourceName);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public <R extends AlteringNodeWrapper> R withProperty(String name, String... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(_nodeToWrap, name, value);
        _overrides.put(name, property);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public <R extends AlteringNodeWrapper> R withProperty(String name, Boolean... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(_nodeToWrap, name, value);
        _overrides.put(name, property);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public <R extends AlteringNodeWrapper> R withProperty(String name, Long... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(_nodeToWrap, name, value);
        _overrides.put(name, property);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public <R extends AlteringNodeWrapper> R withProperty(String name, Double... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(_nodeToWrap, name, value);
        _overrides.put(name, property);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public <R extends AlteringNodeWrapper> R withProperty(String name, Calendar... value) {
        notEmpty(name);
        StubbingProperty property = new StubbingProperty(_nodeToWrap, name, value);
        _overrides.put(name, property);
        return (R) this;
    }

    public <R extends AlteringNodeWrapper> R withTemplate(String templateId) {
        notEmpty(templateId);
        return withProperty(NodeTypes.Renderable.TEMPLATE, templateId);
    }

    @Override
    public Property getProperty(String relPath) throws RepositoryException {
        String key = getKey(relPath);
        Property result = _overrides.get(key);
        if (result == null) {
            result = super.getProperty(key);
        }
        return result;
    }

    @Override
    public boolean hasProperty(String relPath) throws RepositoryException {
        String key = getKey(relPath);
        return _overrides.containsKey(key) || super.hasProperty(key);
    }

    private String getKey(String relPath) {
        return _mapped.getOrDefault(relPath, relPath);
    }
}
