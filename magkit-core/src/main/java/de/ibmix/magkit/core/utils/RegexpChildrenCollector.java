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
 * Collects child {@link Item} instances of a starting {@link Node} whose names match a provided regular expression.
 * This is an alternative to Jackrabbit's {@code org.apache.jackrabbit.util.ChildrenCollectorFilter} which uses a
 * proprietary wildcard matching; this implementation relies purely on standard {@link Pattern} regular expressions.
 * <p>Main functionality:</p>
 * <ul>
 *     <li>Traverses a node hierarchy either breadth-first or depth-first (configurable in constructor).</li>
 *     <li>Limits traversal depth via a {@code maxLevel} parameter (e.g. {@code 1} for direct children only).</li>
 *     <li>Filters collected items by both Java type ({@link Node} / {@link Property} / {@link Item}) and name pattern.</li>
 *     <li>Adds matching items to a caller-provided {@link Collection} instance.</li>
 * </ul>
 * <p>Usage preconditions:</p>
 * <ul>
 *     <li>The provided {@code collectedChildren} collection must not be {@code null}.</li>
 *     <li>The pattern (string or {@link Pattern}) should compile successfully; invalid regex will trigger a
 *     {@link java.util.regex.PatternSyntaxException} on construction.</li>
 *     <li>{@code maxLevel} should be {@code >= 1}; values {@code <= 0} would make traversal meaningless.</li>
 * </ul>
 * <p>
 * Side effects: The passed in collection instance is mutated by adding matching items during traversal.
 * </p>
 * <p>
 * Null and error handling: Constructor parameters are assumed non-null. Traversal may throw {@link RepositoryException}
 * from underlying JCR operations when accessing item names. Those exceptions are propagated.
 * </p>
 * <p>
 * Thread-safety: Instances are not thread-safe because they mutate the externally supplied collection. Use one
 * instance per traversal thread or provide appropriate external synchronization if sharing the collection.
 * </p>
 * <p>Example usage (multi select values):</p>
 * <pre>
 *     RegexpChildrenCollector&lt;Property&gt; collector = new RegexpChildrenCollector&lt;&gt;(new ArrayList&lt;&gt;(), "child-property-name", false, 1, Property.class);
 *     multiselectNode.accept(collector);
 *     Collection&lt;Property&gt; valueProperties = collector.getCollectedChildren();
 * </pre>
 *
 * @param <T> subclass of {@link Item} that will be collected
 * @author lars.gendner
 * @since ???
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
     * @param breadthFirst set to {@code true} for breadth-first traversal, {@code false} for depth-first
     * @param maxLevel maximum level of traversal (set to {@code 1} for direct children collection)
     * @param classToCollect only instances of this class will be collected (e.g. {@code Item.class}, {@code Property.class}, {@code Node.class})
     * @see Pattern#compile(String)
     */
    public RegexpChildrenCollector(Collection<T> collectedChildren, String childNamePatternString, boolean breadthFirst, int maxLevel, Class<? extends T> classToCollect) {
        this(collectedChildren, Pattern.compile(childNamePatternString), breadthFirst, maxLevel, classToCollect);
    }

    /**
     * Constructs an instance using a precompiled {@link Pattern}.
     *
     * @param collectedChildren collection in which items will be collected
     * @param childNamePattern pattern for children names
     * @param breadthFirst set to {@code true} for breadth-first traversal, {@code false} for depth-first
     * @param maxLevel maximum level of traversal (set to {@code 1} for direct children collection)
     * @param classToCollect only instances of this class will be collected (e.g. {@code Item.class}, {@code Property.class}, {@code Node.class})
     * @see Pattern#compile(String)
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
        if (isCollectableChild(node, level)) {
            getCollectedChildren().add(getClassToCollect().cast(node));
        }
    }

    @Override
    protected void entering(Property property, int level) throws RepositoryException {
        super.entering(property, level);
        if (isCollectableChild(property, level)) {
            getCollectedChildren().add(getClassToCollect().cast(property));
        }
    }

    private boolean isCollectableChild(Item item, int level) throws RepositoryException {
        return level > 0 && getClassToCollect().isInstance(item) && isItemNameMatching(item);
    }

    /**
     * Checks whether the name of the supplied {@link Item} matches the configured child name pattern.
     *
     * @param item the item whose JCR name is evaluated against the pattern
     * @return {@code true} if the item's name matches the pattern, otherwise {@code false}
     * @throws RepositoryException if retrieving the item name fails
     */
    private boolean isItemNameMatching(Item item) throws RepositoryException {
        return getChildNamePattern().matcher(item.getName()).matches();
    }

    /**
     * Returns the {@link Pattern} used to test child item names.
     *
     * @return the non-null compiled pattern
     */
    public Pattern getChildNamePattern() {
        return _childNamePattern;
    }

    /**
     * Returns the collection that holds all collected child items.
     *
     * @return mutable collection of collected items (never null)
     */
    public Collection<T> getCollectedChildren() {
        return _collectedChildren;
    }

    /**
     * Replaces the target collection used for storing collected items.
     * Existing collected references are discarded in favor of the new collection instance.
     *
     * @param collectedChildren new collection to store future collected items; must not be {@code null}
     */
    public void setCollectedChildren(Collection<T> collectedChildren) {
        _collectedChildren = collectedChildren;
    }

    /**
     * Returns the class object that determines which item types are collected.
     *
     * @return the class used for filtering collected items by type
     */
    public Class<? extends T> getClassToCollect() {
        return _classToCollect;
    }
}
