package de.ibmix.magkit.query.sql2.condition;

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

import de.ibmix.magkit.query.sql2.statement.Sql2SelectorNames;

import javax.jcr.Node;

import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Builder for a SQL2 path relation condition using JCR path functions (issamenode, ischildnode, isdescendantnode).
 * Supports negation via {@link #not()} and choosing one of the path relation methods against either an absolute
 * path string or a JCR Node (path extracted best effort). The builder stays empty unless both path and method are set.
 *
 * Features:
 * <ul>
 *   <li>Same node, child node, descendant node checks</li>
 *   <li>Negation support wrapping the function call in {@code not(...)}</li>
 *   <li>Join selector awareness via {@link #forJoin()}</li>
 * </ul>
 * Thread-safety: Not thread safe.
 * Null handling: Null / blank paths result in an empty condition (ignored when rendering).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-04-01
 **/
public final class Sql2PathCondition implements Sql2JoinConstraint {

    static final String SQL2_METHOD_CHILD = "ischildnode";
    static final String SQL2_METHOD_DESCENDANT = "isdescendantnode";
    static final String SQL2_METHOD_SAME = "issamenode";

    private String _path;
    private String _method;
    private boolean _not;
    private boolean _forJoin;


    private Sql2PathCondition() {
    }

    /**
     * Start building a path condition (configure relation and path afterwards).
     * @return new empty path condition instance
     */
    public static Sql2PathCondition is() {
        return new Sql2PathCondition();
    }

    @Override
    public boolean isNotEmpty() {
        return isNotBlank(_path) && isNotBlank(_method);
    }

    /**
     * Negate the path constraint (wrap in not()).
     * @return this
     */
    public Sql2PathCondition not() {
        _not = true;
        return this;
    }

    /**
     * Constrain to child relationship using a String path.
     * @param path parent path
     * @return this
     */
    public Sql2JoinConstraint child(final String path) {
        _path = trim(path);
        _method = SQL2_METHOD_CHILD;
        return this;
    }

    /**
     * Constrain to child relationship using a Node (path extracted).
     * @param parent parent node
     * @return this
     */
    public Sql2JoinConstraint child(final Node parent) {
        return child(toPath(parent));
    }

    /**
     * Constrain to descendant relationship using a String path.
     * @param path ancestor path
     * @return this
     */
    public Sql2JoinConstraint descendant(final String path) {
        _path = trim(path);
        _method = SQL2_METHOD_DESCENDANT;
        return this;
    }

    /**
     * Constrain to descendant relationship using a Node (path extracted).
     * @param parent ancestor node
     * @return this
     */
    public Sql2JoinConstraint descendant(final Node parent) {
        return descendant(toPath(parent));
    }

    /**
     * Constrain to same node relationship using a String path.
     * @param path node path
     * @return this
     */
    public Sql2JoinConstraint same(final String path) {
        _path = trim(path);
        _method = SQL2_METHOD_SAME;
        return this;
    }

    /**
     * Constrain to same node relationship using a Node (path extracted).
     * @param parent node
     * @return this
     */
    public Sql2JoinConstraint same(final Node parent) {
        return same(toPath(parent));
    }

    private String toPath(final Node node) {
        return node != null ? getPathIfPossible(node) : EMPTY;
    }

    /**
     * Append the configured path relation function call to the buffer if non-empty.
     * @param sql2 target buffer (never null)
     * @param selectorNames selector provider determining which selector name to include
     */
    @Override
    public void appendTo(StringBuilder sql2, final Sql2SelectorNames selectorNames) {
        if (isNotEmpty()) {
            if (_not) {
                sql2.append("not(");
            }
            String selectorName = _forJoin ? selectorNames.getJoinSelectorName() : selectorNames.getFromSelectorName();
            sql2.append(_method).append('(');
            if (isNotBlank(selectorName)) {
                sql2.append(selectorName).append(',').append(' ');
            }
            sql2.append('\'');
            if (_path.charAt(0) != '/') {
                sql2.append('/');
            }
            sql2.append(_path).append("')");
            if (_not) {
                sql2.append(')');
            }
        }
    }

    /**
     * Use the join selector name instead of the from selector when rendering.
     * @return this for fluent chaining
     */
    @Override
    public Sql2JoinConstraint forJoin() {
        _forJoin = true;
        return this;
    }
}
