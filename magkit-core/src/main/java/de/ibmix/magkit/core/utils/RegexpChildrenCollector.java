package de.ibmix.magkit.core.utils;

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
 * <br>
 * E.g.: for multi select values use the following call:
 * <code>
 *      RegexpChildrenCollector&lt;Property&gt; collector = new RegexpChildrenCollector&lt;Property&gt;(new ArrayList&lt;Property&gt;(), "\\d+", false, 1, Property.class);
 *      multiselectNode.accept(collector);
 *      Collection&lt;Property&gt; valueProperties = collector.getCollectedChildren();
 * </code>
 *
 * @param <T> subclass of {@link javax.jcr.Item}
 *
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
     * @param collectedChildren collection in which items will be collected
     * @param childNamePatternString regular expression for children names
     * @param breadthFirst set to <code>true</code> if children hierarchy shall be traversed breadth-first, set to <code>false</code> if depth-first
     * @param maxLevel maximum level of traversal (set to <code>1</code> for direct children collection)
     * @param classToCollect only instances of this class will be collected (reasonable values: <code>Item.class</code>, <code>Property.class</code>, <code>Node.class</code>)
     *
     * @see java.util.regex.Pattern#compile(String)
     */
    public RegexpChildrenCollector(Collection<T> collectedChildren, String childNamePatternString, boolean breadthFirst, int maxLevel, Class<? extends T> classToCollect) {
        this(collectedChildren, Pattern.compile(childNamePatternString), breadthFirst, maxLevel, classToCollect);
    }

    /**
     * Constructs an instance using a {@link java.util.regex.Pattern}.
     *
     * @param collectedChildren collection in which items will be collected
     * @param childNamePattern pattern for children names
     * @param breadthFirst set to <code>true</code> if children hierarchy shall be traversed breadth-first, set to <code>false</code> if depth-first
     * @param maxLevel maximum level of traversal (set to <code>1</code> for direct children collection)
     * @param classToCollect only instances of this class will be collected (reasonable values: <code>Item.class</code>, <code>Property.class</code>, <code>Node.class</code>)
     *
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
