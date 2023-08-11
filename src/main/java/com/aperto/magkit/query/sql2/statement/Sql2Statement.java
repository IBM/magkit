package com.aperto.magkit.query.sql2.statement;

import com.aperto.magkit.query.sql2.condition.Sql2ConstraintGroup;
import com.aperto.magkit.query.sql2.condition.Sql2JoinCondition;
import com.aperto.magkit.query.sql2.condition.Sql2JoinConstraint;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.jackrabbit.JcrConstants;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;


/**
 * The builder for a SQL2 query statement.
 * Note: DISTINCT, IN are not supported by jackrabbit JCR
 *
 * @author wolf.bubenik@aperto.com
 * @since 28.02.2020
 **/
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

    public static Sql2From select(String... attributes) {
        return new Sql2Statement(attributes);
    }

    public Sql2As from(String nodeType) {
        if (isNotBlank(nodeType)) {
            _nodeType = nodeType;
        }
        return this;
    }

    public Sql2Join as(String selectorName) {
        _fromSelectorName = trimToNull(selectorName);
        return this;
    }

    public Sql2JoinAs innerJoin(final String nodeType) {
        _joinMethod = INNER_JOIN;
        _joinNodeType = nodeType;
        return this;
    }

    public Sql2JoinAs leftOuterJoin(final String nodeType) {
        _joinMethod = LEFT_OUTER_JOIN;
        _joinNodeType = nodeType;
        return this;
    }

    public Sql2JoinAs rightOuterJoin(final String nodeType) {
        _joinMethod = RIGHT_OUTER_JOIN;
        _joinNodeType = nodeType;
        return this;
    }

    public Sql2JoinOn joinAs(String selectorName) {
        _joinSelectorName = trimToNull(selectorName);
        return this;
    }

    public Sql2Where on(Sql2JoinCondition joinCondition) {
        _joinCondition = joinCondition;
        return this;
    }

    public Sql2Order whereAll(Sql2JoinConstraint... constraints) {
        return where(Sql2ConstraintGroup.and().matches(constraints));
    }

    public Sql2Order whereAny(Sql2JoinConstraint... constraints) {
        return where(Sql2ConstraintGroup.or().matches(constraints));
    }

    private Sql2Order where(Sql2ConstraintGroup constraintGroup) {
        _constraintGroup = constraintGroup;
        return this;
    }

    public Sql2OrderDirection orderBy(String... attributes) {
        _orderAttributes = attributes;
        return this;
    }

    public Sql2OrderDirection orderByScore() {
        return orderBy(JcrConstants.JCR_SCORE);
    }

    public Sql2Builder descending() {
        _orderDirection = DESC;
        return this;
    }

    public Sql2Builder ascending() {
        _orderDirection = ASC;
        return this;
    }

    public String getFromSelectorName() {
        return _fromSelectorName;
    }

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
