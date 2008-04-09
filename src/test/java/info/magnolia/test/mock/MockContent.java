/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.test.mock;

import info.magnolia.cms.core.*;
import info.magnolia.cms.security.AccessDeniedException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;


/**
 * @author philipp
 * @version $Id: MockContent.java 11314 2007-09-14 14:39:46Z gjoseph $
 */
public class MockContent extends DefaultContent {

    private String _uuid;

    private Content _parent;

    private HierarchyManager _hierarchyManager;

    private String _name;

    private OrderedMap _nodeDatas = new ListOrderedMap();

    private OrderedMap _children = new ListOrderedMap();

    private String _nodeTypeName = ItemType.CONTENTNODE.getSystemName();

    private String _template;

    public MockContent(String name) {
        _name = name;
    }

    public MockContent(String name, ItemType contentType) {
        this(name);
        this.setNodeTypeName(contentType.getSystemName());
    }

    public MockContent(String name, OrderedMap nodeDatas, OrderedMap children) {
        this(name);
        for (Object o : children.values()) {
            MockContent c = (MockContent) o;
            addContent(c);
        }
        for (Object obj : nodeDatas.values()) {
            MockNodeData nd = (MockNodeData) obj;
            addNodeData(nd);
        }
    }

    public void addNodeData(MockNodeData nd) {
        nd.setParent(this);
        _nodeDatas.put(nd.getName(), nd);
    }

    public NodeData createNodeData(String name, int type) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        final MockNodeData nd = new MockNodeData(name, type);
        addNodeData(nd);
        return nd;
    }

    public NodeData createNodeData(String name, Object obj) throws RepositoryException {
        final MockNodeData nd = new MockNodeData(name, obj);
        addNodeData(nd);
        return nd;
    }

    public MockMetaData createMetaData() {
        addContent(new MockContent("MetaData"));//, ItemType."mgnl:metaData"));
        return getMetaData();
    }

    public Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return createContent(name, new ItemType(contentType));
    }

    public Content createContent(String name, ItemType contentType) {
        MockContent c = new MockContent(name, contentType);
        addContent(c);
        return c;
    }

    public void addContent(MockContent child) {
        child.setParent(this);
        _children.put(child.getName(), child);
    }

    public Content getContent(String name) throws RepositoryException {
        Content c;
        if (name.contains("/")) {
            c = getContent(StringUtils.substringBefore(name, "/"));
            if (c != null) {
                return c.getContent(StringUtils.substringAfter(name, "/"));
            }
        }
        else {
            c = (Content) _children.get(name);
        }
        if (c == null) {
            throw new PathNotFoundException(name);
        }
        return c;
    }

    public boolean hasContent(String name) throws RepositoryException {
        return _children.containsKey(name);
    }

    public String getHandle() {
        if (this.getParent() != null && !this.getParent().getName().equals("jcr:root")) {
            return getParent().getHandle() + "/" + this.getName();
        }
        return "/" + this.getName();
    }

    public int getLevel() throws PathNotFoundException, RepositoryException {
        if (this.getParent() == null) {
            return 0;
        }
        return getParent().getLevel() + 1;
    }

    public Collection getNodeDataCollection() {
        return this._nodeDatas.values();
    }

    public NodeData getNodeData(String name) {
        final MockNodeData nodeData = (MockNodeData) this._nodeDatas.get(name);
        if (nodeData != null) {
            return nodeData;
        } else {
            final MockNodeData fakeNodeData= new MockNodeData(name, null);
            fakeNodeData.setParent(this);
            return fakeNodeData;
        }
    }

    public boolean hasNodeData(String name) throws RepositoryException {
        return _nodeDatas.containsKey(name);
    }

    // TODO : use the given Comparator
    public Collection getChildren(final ContentFilter filter, Comparator orderCriteria) {
        // copy
        List children = new ArrayList(this._children.values());

        CollectionUtils.filter(children, new Predicate() {

            public boolean evaluate(Object object) {
                return filter.accept((Content) object);
            }
        });

        return children;
    }

    public Collection getChildren(final String contentType, final String namePattern) {
//        if (!"*".equals(namePattern)) {
//            throw new IllegalStateException("Only the \"*\" name pattern is currently supported in MockContent.");
//        }
        return getChildren(new ContentFilter() {
            public boolean accept(Content content) {
                return (contentType == null || content.isNodeType(contentType)) && ("*".equals(namePattern) || (content.getName() != null && content.getName().matches(namePattern)));
            }
        });

    }

    public Content getChildByName(String namePattern) {
        return (Content) _children.get(namePattern);
    }

    public void orderBefore(String srcName, String beforeName) throws RepositoryException {
        Content movedNode = (Content) _children.get(srcName);
        List tmp = new ArrayList(_children.values());
        tmp.remove(movedNode);
        tmp.add(tmp.indexOf(_children.get(beforeName)), movedNode);
        _children.clear();
        for (Object aTmp : tmp) {
            Content child = (Content) aTmp;
            _children.put(child.getName(), child);
        }
    }

    public void save() throws RepositoryException {
        // nothing to do
    }

    public String getName() {
        return this._name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getNodeTypeName() throws RepositoryException {
        return this._nodeTypeName;
    }

    public void setNodeTypeName(String nodeTypeName) {
        this._nodeTypeName = nodeTypeName;
    }

    public void delete() throws RepositoryException {
        final MockContent parent = (MockContent) getParent();
        final boolean removed = parent._children.values().remove(this);
        if (!removed) {
            throw new RepositoryException("MockContent could not delete itself");
        }
    }

    public Content getParent() {
        return this._parent;
    }

    public void setParent(Content parent) {
        this._parent = parent;
    }

    public HierarchyManager getHierarchyManager() {
        if (this._hierarchyManager == null && getParent() != null) {
            return ((MockContent) getParent()).getHierarchyManager();
        }
        return this._hierarchyManager;
    }

    /**
     * @param hm the hm to set
     */
    public void setHierarchyManager(HierarchyManager hm) {
        this._hierarchyManager = hm;
    }

    public String getUUID() {
        return this._uuid;
    }

    public void setUUID(String uuid) {
        this._uuid = uuid;
    }

    public MockMetaData getMetaData() {
        try {
            return new MockMetaData((MockContent) getContent("MetaData"));
        } catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public String toString() {
        return super.toString() + ": " + this.getHandle();
    }

    public void setTemplate(String template) {
        this._template = template;
    }

    public String getTemplate() {
        return this._template;
    }
}