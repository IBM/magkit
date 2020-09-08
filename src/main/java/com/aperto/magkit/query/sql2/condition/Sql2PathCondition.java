package com.aperto.magkit.query.sql2.condition;

import com.aperto.magkit.query.sql2.statement.Sql2SelectorNames;

import javax.jcr.Node;

import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * The builder for a sql2 path condition.
 *
 * @author wolf.bubenik@aperto.com
 * @since 01.04.2020
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

    public static Sql2PathCondition is() {
        return new Sql2PathCondition();
    }

    @Override
    public boolean isNotEmpty() {
        return isNotBlank(_path) && isNotBlank(_method);
    }

    public Sql2PathCondition not() {
        _not = true;
        return this;
    }

    public Sql2JoinConstraint child(final String path) {
        _path = trim(path);
        _method = SQL2_METHOD_CHILD;
        return this;
    }

    public Sql2JoinConstraint child(final Node parent) {
        return child(toPath(parent));
    }

    public Sql2JoinConstraint descendant(final String path) {
        _path = trim(path);
        _method = SQL2_METHOD_DESCENDANT;
        return this;
    }

    public Sql2JoinConstraint descendant(final Node parent) {
        return descendant(toPath(parent));
    }

    public Sql2JoinConstraint same(final String path) {
        _path = trim(path);
        _method = SQL2_METHOD_SAME;
        return this;
    }

    public Sql2JoinConstraint same(final Node parent) {
        return same(toPath(parent));
    }

    private String toPath(final Node node) {
        return node != null ? getPathIfPossible(node) : EMPTY;
    }

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

    @Override
    public Sql2JoinConstraint forJoin() {
        _forJoin = true;
        return this;
    }
}
