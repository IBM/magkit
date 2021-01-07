package com.aperto.magkit.query.sql2.condition;

import com.aperto.magkit.query.sql2.statement.Sql2SelectorNames;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Builder for a sql2 fulltext condition with contains().
 * Terms are processed as follows:
 * <ul>
 *     <li>trim: leading and tailing ' ' will be removed</li>
 *     <li>empty/missing: empty or null terms will be ignored</li>
 *     <li>escape: ' will be replaced by '' and XPath search chars at last position will be escaped by \</li>
 *     <li>phrases: terms containing blanks (' ') will be surrounded by "</li>
 *     <li>exclude: terms to exclude will be prefixed by -</li>
 *     <li>fuzzy: fuzzy terms will be suffixed by ~. Defining a "Damerau-Levenshtein Distance" is not supported by magnolia/Jackrabbit. Fuzzy search has no effect for phrases.</li>
 *     <li>boost: terms with a boosting value &gt; 1 will be suffixed by ^&lt;boost&gt; (^3 for a boost factor of 3)</li>
 *     <li>and: multiple mandatory terms will be concatenated by spaces (' ')</li>
 *     <li>or: multiple optional terms will be concatenated by OR</li>
 * </ul>
 *
 * @author wolf.bubenik@aperto.com
 * @since 3.5.2 (04.01.2021)
 */
public class Sql2ContainsCondition implements Sql2JoinConstraint, Sql2Constraint {

    private static final String METHOD_NAME = "contains";

    private final String _property;
    private boolean _forJoin;
    private final List<Term> _terms = new ArrayList<>();

    public Sql2ContainsCondition() {
        this("*");
    }

    public Sql2ContainsCondition(String property) {
        _property = isEmpty(property) ? "*" : property;
    }

    public Sql2ContainsCondition all(String... terms) {
        return all(1, false, terms);
    }

    public Sql2ContainsCondition all(int boost, final boolean isFuzzy, String... terms) {
        return addTerm(boost, false, false, isFuzzy, terms);
    }

    public Sql2ContainsCondition any(String... terms) {
        return any(1, false, terms);
    }

    public Sql2ContainsCondition any(int boost, final boolean isFuzzy, String... terms) {
        return addTerm(boost, false, true, isFuzzy, terms);
    }

    public Sql2ContainsCondition excludeAll(final String... terms) {
        return excludeAll(1, false, terms);
    }

    public Sql2ContainsCondition excludeAll(final int boost, final boolean isFuzzy, final String... terms) {
        return addTerm(boost, true, false, isFuzzy, terms);
    }

    public Sql2ContainsCondition excludeAny(final String... terms) {
        return excludeAny(1, false, terms);
    }

    public Sql2ContainsCondition excludeAny(final int boost, final boolean isFuzzy, final String... terms) {
        return addTerm(boost, true, true, isFuzzy, terms);
    }

    public Sql2ContainsCondition addTerm(final int boost, final boolean exclude, final boolean optional, final boolean isFuzzy, final String... terms) {
        _terms.addAll(Arrays.stream(terms).map(StringUtils::trim).filter(StringUtils::isNotEmpty).map(t -> new Term(t, boost, exclude, optional, isFuzzy)).collect(Collectors.toList()));
        return me();
    }


    /**
     * Append this condition to the query string.
     *
     * @param sql2 the sql2 query
     * @param selectorNames the selector names
     */
    @Override
    public void appendTo(StringBuilder sql2, Sql2SelectorNames selectorNames) {
        if (isNotEmpty()) {
            String selectorName = _forJoin ? selectorNames.getJoinSelectorName() : selectorNames.getFromSelectorName();
            sql2.append(METHOD_NAME).append('(');
            if (StringUtils.isNotEmpty(selectorName)) {
                sql2.append(selectorName).append('.');
            } else {
                throw new IllegalStateException("Missing selector name for contains() condition.");
            }
            sql2.append(_property).append(", '");
            appendTerms(sql2);
            sql2.append("')");
        }
    }

    private void appendTerms(final StringBuilder sql2) {
        boolean isFirst = true;
        for (Term term : _terms) {
            term.append(sql2, isFirst);
            isFirst = false;
        }
    }

    @Override
    public boolean isNotEmpty() {
        return !_terms.isEmpty();
    }

    @Override
    public Sql2JoinConstraint forJoin() {
        _forJoin = true;
        return me();
    }

    public Sql2ContainsCondition me() {
        return this;
    }

    private static class Term {
        private final String _term;
        private final int _boost;
        private final boolean _isExclude;
        private final boolean _isOptional;
        private final boolean _isFuzzy;
        private final boolean _isPhrase;

        Term(final String term, final int boost, final boolean exclude, final boolean optional, final boolean fuzzy) {
            Preconditions.checkArgument(!term.startsWith("?"), "Term may not start with '?'");
            Preconditions.checkArgument(!term.startsWith("*"), "Term may not start with '*'");
            _term = term;
            _boost = boost;
            _isExclude = exclude;
            _isOptional = optional;
            _isFuzzy = fuzzy;
            _isPhrase = _term.contains(" ");
        }

        private void append(final StringBuilder sql2, boolean isFirst) {
            if (!isFirst) {
                sql2.append(_isOptional ? Sql2Constraint.SQL2_OP_OR : " ");
            }
            if (_isExclude) {
                sql2.append('-');
            }
            if (_isPhrase) {
                sql2.append('"');
            }
            appendEscaped(_term, sql2);
            if (_isPhrase) {
                sql2.append('"');
            }
            if (_isFuzzy) {
                sql2.append('~');
            }
            if (_boost > 1) {
                sql2.append('^').append(_boost);
            }
        }

        private void appendEscaped(final String term, final StringBuilder sql2) {
            int last = term.length() - 1;
            // escape ' by ''
            for (int i = 0; i < last; i++) {
                char c = term.charAt(i);
                if (c == '\'') {
                    sql2.append('\'');
                }
                sql2.append(c);
            }
            // escape XPath search chars in last position
            // code taken from org.apache.jackrabbit.util.Text:escapeIllegalXpathSearchChars() and modified to avoid temp String creation.
            char c = term.charAt(last);
            // CHECKSTYLE:OFF
            if (c == '!' || c == '(' || c == ':' || c == '^' || c == '[' || c == ']' || c == '{' || c == '}' || c == '?') {
                sql2.append('\\');
            }
            // CHECKSTYLE:ON
            sql2.append(c);
        }
    }
}
