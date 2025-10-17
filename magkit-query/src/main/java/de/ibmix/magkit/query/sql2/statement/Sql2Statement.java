package de.ibmix.magkit.query.sql2.statement;

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

import de.ibmix.magkit.query.sql2.condition.Sql2ConstraintGroup;
import de.ibmix.magkit.query.sql2.condition.Sql2JoinCondition;
import de.ibmix.magkit.query.sql2.condition.Sql2JoinConstraint;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.jackrabbit.JcrConstants;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * Builder implementation for constructing JCR SQL2 query statements via a fluent API.
 * <p>Features:
 * <ul>
 *   <li>SELECT with explicit attribute list or wildcard (*)</li>
 *   <li>Optional FROM node type (defaults to nt:base)</li>
 *   <li>Optional selector aliasing via AS for the primary and joined node types</li>
 *   <li>INNER, LEFT OUTER and RIGHT OUTER JOIN with custom ON conditions</li>
 *   <li>WHERE constraints grouped by logical AND (whereAll) or OR (whereAny)</li>
 *   <li>ORDER BY on properties or JCR score with ascending/descending direction</li>
 * </ul>
 * Limitations: DISTINCT and IN are not supported by Jackrabbit and thus omitted. Ordering on join attributes is not yet implemented
 * (see inline TODO comments). Multiple selectors are supported but some convenience handling (e.g. join attribute names) is pending.
 * <p>Usage example:
 * <pre>
 * String q = Sql2Statement.select("title", "date")
 *     .from("mgnl:page")
 *     .as("p")
 *     .whereAll(Sql2.Condition.String.property("title").startsWithAny().values("Test"))
 *     .orderBy("date").ascending()
 *     .build();
 * </pre>
 * <p>Preconditions: Start with {@link #select(String...)}. Subsequent steps are optional. Null or blank inputs for node types or selector
 * names are safely ignored. Passing null where a condition object is required will cause exceptions during build.</p>
 * <p>Side effects: Mutates internal state on each fluent step until {@link #build()} is invoked.</p>
 * <p>Null handling: Attribute arrays may be null/empty (treated as wildcard). Node type defaults to nt:base if blank. Selector names become null if blank.
 * Constraint arrays may be null/empty (no WHERE clause). Join attributes ordering currently falls back to from-selector prefix only.</p>
 * <p>Thread-safety: Not thread-safe; use per-thread instances.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-02-28
 */
public final class Sql2Statement implements Sql2From, Sql2As, Sql2Join, Sql2JoinAs, Sql2JoinOn, Sql2Where, Sql2Order, Sql2OrderDirection, Sql2Builder, Sql2SelectorNames {

    private static final String DESC = " DESC";
    private static final String ASC = " ASC";
    private static final String SELECT = "SELECT ";
    private static final String FROM = " FROM ";
    private static final String AS = " AS ";
    private static final String WHERE = " WHERE ";
    private static final String INNER_JOIN = " INNER JOIN ";
    private static final String LEFT_OUTER_JOIN = " LEFT OUTER JOIN ";
    private static final String RIGHT_OUTER_JOIN = " RIGHT OUTER JOIN ";
    private static final String ON = " ON ";
    private static final String ORDER_BY = " ORDER BY ";

    private final String[] _attributes;
    private String _nodeType = JcrConstants.NT_BASE;
    private String _fromSelectorName;
    private String _joinSelectorName;
    private Sql2ConstraintGroup _constraintGroup;
    private String[] _orderAttributes;
    private String _orderDirection = DESC;
    private Sql2JoinCondition _joinCondition;
    private String _joinNodeType;
    private String _joinMethod;

    private Sql2Statement(String... attributes) {
        _attributes = attributes;
    }

    /**
     * Start building a SQL2 statement selecting the given attributes (or all when empty).
     * @param attributes zero or more attribute (property) names
     * @return builder step allowing optional FROM specification
     */
    public static Sql2From select(String... attributes) {
        return new Sql2Statement(attributes);
    }

    /**
     * Define the primary node type for the FROM clause (ignored if blank).
     * @param nodeType JCR node type name
     * @return next step allowing selector aliasing
     */
    public Sql2As from(String nodeType) {
        if (isNotBlank(nodeType)) {
            _nodeType = nodeType;
        }
        return this;
    }

    /**
     * Assign a selector name to the primary node type (ignored if blank).
     * @param selectorName selector alias
     * @return next step allowing joins or where clauses
     */
    public Sql2Join as(String selectorName) {
        _fromSelectorName = trimToNull(selectorName);
        return this;
    }

    /**
     * Declare an INNER JOIN on the given node type.
     * @param nodeType target join node type
     * @return step to assign join selector name
     */
    public Sql2JoinAs innerJoin(final String nodeType) {
        _joinMethod = INNER_JOIN;
        _joinNodeType = nodeType;
        return this;
    }

    /**
     * Declare a LEFT OUTER JOIN on the given node type.
     * @param nodeType target join node type
     * @return step to assign join selector name
     */
    public Sql2JoinAs leftOuterJoin(final String nodeType) {
        _joinMethod = LEFT_OUTER_JOIN;
        _joinNodeType = nodeType;
        return this;
    }

    /**
     * Declare a RIGHT OUTER JOIN on the given node type.
     * @param nodeType target join node type
     * @return step to assign join selector name
     */
    public Sql2JoinAs rightOuterJoin(final String nodeType) {
        _joinMethod = RIGHT_OUTER_JOIN;
        _joinNodeType = nodeType;
        return this;
    }

    /**
     * Assign selector name for the joined node type (ignored if blank).
     * @param selectorName join selector alias
     * @return step to define ON condition
     */
    public Sql2JoinOn joinAs(String selectorName) {
        _joinSelectorName = trimToNull(selectorName);
        return this;
    }

    /**
     * Define the join ON condition relating the two selectors.
     * @param joinCondition the condition object (must be non-null)
     * @return next step allowing WHERE clauses
     */
    public Sql2Where on(Sql2JoinCondition joinCondition) {
        _joinCondition = joinCondition;
        return this;
    }

    /**
     * Add constraints that must all match (logical AND group).
     * @param constraints constraints combined with AND
     * @return next step allowing ordering
     */
    public Sql2Order whereAll(Sql2JoinConstraint... constraints) {
        return where(Sql2ConstraintGroup.and().matches(constraints));
    }

    /**
     * Add constraints where any may match (logical OR group).
     * @param constraints constraints combined with OR
     * @return next step allowing ordering
     */
    public Sql2Order whereAny(Sql2JoinConstraint... constraints) {
        return where(Sql2ConstraintGroup.or().matches(constraints));
    }

    private Sql2Order where(Sql2ConstraintGroup constraintGroup) {
        _constraintGroup = constraintGroup;
        return this;
    }

    /**
     * Specify attributes to order by (overrides previous ordering attributes).
     * @param attributes one or more property names
     * @return next step allowing direction selection
     */
    public Sql2OrderDirection orderBy(String... attributes) {
        _orderAttributes = attributes;
        return this;
    }

    /**
     * Convenience ordering by JCR score.
     * @return next step allowing direction selection
     */
    public Sql2OrderDirection orderByScore() {
        return orderBy(JcrConstants.JCR_SCORE);
    }

    /**
     * Set direction to descending (default).
     * @return final builder step
     */
    public Sql2Builder descending() {
        _orderDirection = DESC;
        return this;
    }

    /**
     * Set direction to ascending.
     * @return final builder step
     */
    public Sql2Builder ascending() {
        _orderDirection = ASC;
        return this;
    }

    /**
     * Get the selector name of the FROM part or null.
     * @return primary selector alias or null
     */
    public String getFromSelectorName() {
        return _fromSelectorName;
    }

    /**
     * Get the selector name of the JOIN part or null.
     * @return join selector alias or null
     */
    public String getJoinSelectorName() {
        return _joinSelectorName;
    }

    private boolean hasFromSelector() {
        return isNotBlank(_fromSelectorName);
    }

    private boolean hasJoinSelector() {
        return isNotBlank(_joinSelectorName);
    }

    private boolean hasTwoSelectors() {
        return hasFromSelector() && hasJoinSelector();
    }

    /**
     * Render the final SQL2 statement.
     * @return SQL2 query string
     */
    public String build() {
        StringBuilder result = new StringBuilder();
        result.append(SELECT);
        if (ArrayUtils.isEmpty(_attributes)) {
            appendAllAttributes(result);
        } else {
            appendAttributes(result);
        }
        result.append(FROM).append('[').append(_nodeType).append(']');
        if (hasFromSelector()) {
            result.append(AS).append(_fromSelectorName);
        }

        if (isNotBlank(_joinMethod) && isNotBlank(_joinNodeType) && hasJoinSelector()) {
            result.append(_joinMethod).append('[').append(_joinNodeType).append(']').append(AS).append(_joinSelectorName).append(ON);
            _joinCondition.appendTo(result, this);
        }

        if (_constraintGroup != null && _constraintGroup.isNotEmpty()) {
            result.append(WHERE);
            _constraintGroup.appendTo(result, this);
        }
        if (ArrayUtils.isNotEmpty(_orderAttributes)) {
            result.append(ORDER_BY);
            for (String attribute : _orderAttributes) {
                if (hasTwoSelectors()) {
                    //TODO: handle ordering on join attributes
                    result.append(_fromSelectorName).append('.');
                }
                result.append('[').append(attribute).append(']').append(_orderDirection);
                if (ArrayUtils.indexOf(_orderAttributes, attribute) < _orderAttributes.length - 1) {
                    result.append(", ");
                }
            }
        }
        return result.toString();
    }

    private void appendAttributes(final StringBuilder result) {
        String sep = EMPTY;
        for (String attribute: _attributes) {
            result.append(sep);
            if (hasTwoSelectors()) {
                result.append(_fromSelectorName).append('.');
            }
            result.append('[').append(attribute).append(']');
            sep = ",";
        }
        if (hasTwoSelectors()) {
            result.append(sep).append(_joinSelectorName).append('.');
            // TODO: handle join attribute names
            result.append('*');
        }
    }

    private void appendAllAttributes(final StringBuilder result) {
        if (hasFromSelector()) {
            if (hasTwoSelectors()) {
                result.append(_fromSelectorName).append('.');
            }
            result.append('*');
        }
        if (hasJoinSelector()) {
            if (hasFromSelector()) {
                result.append(',');
            }
            result.append(_joinSelectorName).append('.');
            result.append('*');
        } else if (!hasFromSelector()) {
            result.append('*');
        }
    }

    @Override
    public String toString() {
        return build();
    }
}
