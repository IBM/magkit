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

import com.google.common.base.Preconditions;
import de.ibmix.magkit.query.sql2.statement.Sql2SelectorNames;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Builder for a sql2 fulltext condition with contains().
 * Supports simple terms and phrases, exclusion, boosting as well as fuzzy, proximity and range queries.
 * Terms are processed as follows:
 * <ul>
 *     <li>trim: leading and tailing ' ' will be removed</li>
 *     <li>empty/missing: empty or null terms will be ignored</li>
 *     <li>phrases: terms containing blanks (' ') will be surrounded by " and treated as phrases</li>
 *     <li>escape: ' will be replaced by '' and in simple terms (no phrases) XPath search chars at last position will be escaped by \. The '?' will be escaped only if specified and treated as wildcard by default.</li>
 *     <li>exclude: terms to exclude will be prefixed by -</li>
 *     <li>fuzzy: fuzzy terms will be suffixed by ~. Defining a "Damerau-Levenshtein Distance" is not supported by magnolia/Jackrabbit. Fuzzy search is ignored for phrases.</li>
 *     <li>proximity: phrases with given distance d will be suffixed by ~d (\"two words\"~2). Will be ignored for simple terms (one word) and values lower than 1.</li>
 *     <li>boost: terms with a boosting value b&gt;1 will be suffixed by ^b; (^3 for a boost factor of 3)</li>
 *     <li>and: multiple mandatory terms will be concatenated by spaces (' ')</li>
 *     <li>or: multiple optional terms will be concatenated by OR</li>
 * </ul>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2021-01-04
 */
public class Sql2ContainsCondition implements Sql2JoinConstraint {

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
        return addTerm(boost, false, false, isFuzzy, 0, false, terms);
    }

    public Sql2ContainsCondition any(String... terms) {
        return any(1, false, terms);
    }

    public Sql2ContainsCondition any(int boost, final boolean isFuzzy, String... terms) {
        return addTerm(boost, false, true, isFuzzy, 0, false, terms);
    }

    public Sql2ContainsCondition excludeAll(final String... terms) {
        return excludeAll(1, false, terms);
    }

    public Sql2ContainsCondition excludeAll(final int boost, final boolean isFuzzy, final String... terms) {
        return addTerm(boost, true, false, isFuzzy, 0, false, terms);
    }

    public Sql2ContainsCondition excludeAny(final String... terms) {
        return excludeAny(1, false, terms);
    }

    public Sql2ContainsCondition excludeAny(final int boost, final boolean isFuzzy, final String... terms) {
        return addTerm(boost, true, true, isFuzzy, 0, false, terms);
    }

    public Sql2JoinConstraint range(final boolean inclusive, final String from, final String to) {
        _terms.add(new RangeTerm(false, inclusive, from, to));
        return me();
    }

    public Sql2ContainsCondition addTerm(final int boost, final boolean exclude, final boolean optional, final boolean isFuzzy, final int distance, final boolean escapeQuestionMark, final String... terms) {
        if (ArrayUtils.isNotEmpty(terms)) {
            _terms.addAll(Arrays.stream(terms).map(StringUtils::trim).filter(StringUtils::isNotEmpty).map(t -> new DefaultTerm(t, boost, exclude, optional, isFuzzy, distance, escapeQuestionMark)).collect(Collectors.toList()));
        }
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
            term.appendTerm(sql2, isFirst);
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

    private static class DefaultTerm implements Term {
        private final String _term;
        private final int _boost;
        private final int _distance;
        private final boolean _isExclude;
        private final boolean _isOptional;
        private final boolean _isFuzzy;
        private final boolean _isPhrase;
        private final boolean _escapeQuestionMark;

        DefaultTerm(final String term, final int boost, final boolean exclude, final boolean optional, final boolean fuzzy, final int distance, final boolean escapeQuestionMark) {
            Preconditions.checkArgument(!term.startsWith("?"), "Term may not start with '?'");
            Preconditions.checkArgument(!term.startsWith("*"), "Term may not start with '*'");
            Preconditions.checkArgument(boost >= 0, "Boost factor must be greater than 0.");
            _term = term;
            _boost = boost;
            _isExclude = exclude;
            _isOptional = optional;
            _isFuzzy = fuzzy;
            _isPhrase = _term.contains(" ");
            _distance = distance;
            _escapeQuestionMark = escapeQuestionMark;
        }

        @Override
        public void appendTerm(final StringBuilder sql2, boolean isFirst) {
            if (!isFirst) {
                sql2.append(_isOptional ? Sql2Constraint.SQL2_OP_OR : " ");
            }
            if (_isExclude) {
                sql2.append('-');
            }
            if (_isPhrase) {
                sql2.append('"');
            }
            appendEscaped(_term, sql2, _isPhrase, _escapeQuestionMark);
            if (_isPhrase) {
                sql2.append('"');
                if (_distance > 1) {
                    sql2.append('~').append(_distance);
                }
            }
            if (_isFuzzy && !_isPhrase) {
                sql2.append('~');
            }
            if (_boost != 1) {
                sql2.append('^').append(_boost);
            }
        }
    }

    private static class RangeTerm implements Term {
        private static final String TO = " TO ";
        private final boolean _isInclusive;
        private final boolean _isOptional;
        private final String _from;
        private final String _to;

        RangeTerm(final boolean isOptional, final boolean isInclusive, final String from, final String to) {
            _isOptional = isOptional;
            _isInclusive = isInclusive;
            _from = from;
            _to = to;
        }

        public void appendTerm(final StringBuilder sql2, boolean isFirst) {
            if (!isFirst) {
                sql2.append(_isOptional ? Sql2Constraint.SQL2_OP_OR : " ");
            }
            sql2.append(_isInclusive ? '[' : '{');
            appendEscaped(_from, sql2, false, false);
            sql2.append(TO);
            appendEscaped(_to, sql2, false, false);
            sql2.append(_isInclusive ? ']' : '}');
        }
    }

    private interface Term {
        void appendTerm(StringBuilder sql2, boolean isFirst);

        default void appendEscaped(final String term, final StringBuilder sql2, final boolean isPhrase, final boolean escapeQuestionMark) {
            int last = term.length() - 1;
            // escape ' by ''
            for (int i = 0; i < last; i++) {
                char c = term.charAt(i);
                if (c == '\'') {
                    sql2.append('\'');
                }
                sql2.append(c);
            }
            char c = term.charAt(last);
            if (!isPhrase) {
                // escape XPath search chars in last position of simple terms (not in phrases)
                // code taken from org.apache.jackrabbit.util.Text:escapeIllegalXpathSearchChars() and modified to avoid temp String creation.
                // CHECKSTYLE:OFF
                if (c == '!' || c == '(' || c == ':' || c == '^' || c == '[' || c == ']' || c == '{' || c == '}') {
                    sql2.append('\\');
                }
                // CHECKSTYLE:ON
                // allow ? wildcard at end of simple term
                if (escapeQuestionMark && c == '?') {
                    sql2.append('\\');
                }
                // don't forget to escape tailing '
                if (c == '\'') {
                    sql2.append('\'');
                }
            }
            sql2.append(c);
        }
    }
}
