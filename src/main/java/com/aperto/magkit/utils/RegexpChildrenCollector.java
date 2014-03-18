package com.aperto.magkit.utils;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.util.TraversingItemVisitor;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Used to collect child items of a node, matching a given regular expression.
 * This class exists because Jackrabbit's {@link org.apache.jackrabbit.util.ChildrenCollectorFilter}
 * uses a proprietary wildcard matching (which is stupid).
 * It implements the {@link javax.jcr.ItemVisitor} interface.
 * <p></p>
 * E.g.: for multi select values use the following call:
 * <code>
 * RegexpChildrenCollector<Property> collector = new RegexpChildrenCollector<Property>(new ArrayList<Property>(), "\\d+", false, 1, Property.class);
 * multiselectNode.accept(collector);
 * Collection<Property> valueProperties = collector.getCollectedChildren();
 * </code>
 *
 * @param <T> subclass of {@link javax.jcr.Item}
 * @author lars.gendner
 */
public class RegexpChildrenCollector<T extends Item> extends TraversingItemVisitor.Default {

    /**
     * Result of collection operation.
     */
    private Collection<T> _collectedChildren;

    /**
     * Pattern that children names must match to be collected.
     */
    private final Pattern _childNamePattern;

    private final Class<? extends T> _classToCollect;

    /**
     * Constructs an instance using a pattern string.
     *
     * @param collectedChildren      collection in which items will be collected
     * @param childNamePatternString regular expression for children names
     * @param breadthFirst           set to <code>true</code> if children hierarchy shall be traversed breadth-first, set to <code>false</code> if depth-first
     * @param maxLevel               maximum level of traversal (set to <code>1</code> for direct children collection)
     * @param classToCollect         only instances of this class will be collected (reasonable values: <code>Item.class</code>, <code>Property.class</code>, <code>Node.class</code>)
     * @see java.util.regex.Pattern#compile(String)
     */
    public RegexpChildrenCollector(Collection<T> collectedChildren, String childNamePatternString, boolean breadthFirst, int maxLevel, Class<? extends T> classToCollect) {
        this(collectedChildren, Pattern.compile(childNamePatternString), breadthFirst, maxLevel, classToCollect);
    }

    /**
     * Constructs an instance using a {@link java.util.regex.Pattern}.
     *
     * @param collectedChildren collection in which items will be collected
     * @param childNamePattern  pattern for children names
     * @param breadthFirst      set to <code>true</code> if children hierarchy shall be traversed breadth-first, set to <code>false</code> if depth-first
     * @param maxLevel          maximum level of traversal (set to <code>1</code> for direct children collection)
     * @param classToCollect    only instances of this class will be collected (reasonable values: <code>Item.class</code>, <code>Property.class</code>, <code>Node.class</code>)
     * @see java.util.regex.Pattern#compile(String)
     */
    public RegexpChildrenCollector(Collection<T> collectedChildren, Pattern childNamePattern, boolean breadthFirst, int maxLevel, Class<? extends T> classToCollect) {
        super(breadthFirst, maxLevel);
        _collectedChildren = collectedChildren;
        _childNamePattern = childNamePattern;
        _classToCollect = classToCollect;
    }

    @Override
    protected void entering(Node node, int level) throws RepositoryException {
        super.entering(node, level);
        if (level > 0) {
            if (getClassToCollect().isInstance(node)) {
                if (isItemNameMatching(node)) {
                    getCollectedChildren().add(getClassToCollect().cast(node));
                }
            }
        }
    }

    @Override
    protected void entering(Property property, int level) throws RepositoryException {
        super.entering(property, level);
        if (level > 0) {
            if (getClassToCollect().isInstance(property)) {
                if (isItemNameMatching(property)) {
                    getCollectedChildren().add(getClassToCollect().cast(property));
                }
            }
        }
    }

    /**
     * Checks if the name of an item matches the {@link #_childNamePattern}.
     *
     * @param item item to be checked
     * @return <code>true</code> if name matches, otherwise <code>false</code>
     * @throws javax.jcr.RepositoryException if an error occurs retrieving the name of the item
     */
    private boolean isItemNameMatching(Item item) throws RepositoryException {
        return getChildNamePattern().matcher(item.getName()).matches();
    }

    public Pattern getChildNamePattern() {
        return _childNamePattern;
    }

    public Collection<T> getCollectedChildren() {
        return _collectedChildren;
    }

    public void setCollectedChildren(Collection<T> collectedChildren) {
        _collectedChildren = collectedChildren;
    }

    public Class<? extends T> getClassToCollect() {
        return _classToCollect;
    }
}
