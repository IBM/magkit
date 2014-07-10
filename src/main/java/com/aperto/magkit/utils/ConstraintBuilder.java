package com.aperto.magkit.utils;

/**
 * A simple builder for constraints in xpath queries. Use this with the {@link XpathBuilder}.
 *
 * @author philipp.guettler
 * @since 27.09.13
 */
public class ConstraintBuilder {

    private static final char SPACE = ' ';
    private static final char PARENTHESES_OPEN = '(';
    private static final char PARENTHESES_CLOSE = ')';

    private final StringBuilder _constraint = new StringBuilder();

    /**
     * Creates a new {@link ConstraintBuilder} instance.
     * @return an instance of ConstraintBuilder
     */
    public static ConstraintBuilder constraintBuilder() {
        return new ConstraintBuilder();
    }

    /**
     * Open a group of constraints within parentheses. One should call <code>closeGroup()</code> before calling
     * <code>build()</code>, otherwise the constraint is not valid.
     *
     * @return the {@link ConstraintBuilder}
     */
    public ConstraintBuilder openGroup() {
        return openGroup(null);
    }

    /**
     * Open a group of constraints within parentheses and a {@link Operator} in front. One should call
     * <code>closeGroup()</code> before calling <code>build()</code>, otherwise the constraint is not valid.
     *
     * @return the {@link ConstraintBuilder}
     */
    public ConstraintBuilder openGroup(final Operator operator) {
        if (isNotBlank()) {
            _constraint.append(SPACE);
        }
        conditionalOperator(operator);
        _constraint.append(PARENTHESES_OPEN);
        return this;
    }

    /**
     * Closes a constraint group separated by parentheses.
     *
     * @return the {@link ConstraintBuilder}
     */
    public ConstraintBuilder closeGroup() {
        _constraint.append(PARENTHESES_CLOSE);
        return this;
    }

    /**
     * Add a constraint expression.
     *
     * @param expression the expression
     * @return the {@link ConstraintBuilder}
     */
    public ConstraintBuilder add(final String expression) {
        return add(null, expression);
    }

    /**
     * Short form for <code>add(builder.build())</code>.
     *
     * @param builder a {@link ConstraintBuilder}
     * @return the builder
     */
    public ConstraintBuilder add(final ConstraintBuilder builder) {
        return add(null, String.valueOf(builder));
    }

    /**
     * Add a constraint expression with an operator in front.
     *
     * @param operator   the {@link Operator} or null if none
     * @param expression the expression
     * @return the {@link ConstraintBuilder}
     */
    public ConstraintBuilder add(final Operator operator, final String expression) {
        conditionalSpace();
        conditionalOperator(operator);
        _constraint.append(expression);
        return this;
    }

    /**
     * Add a uuid constraint expression. The expression uses <code>@jcr:uuid='*'</code>.
     *
     * @param id the id to use
     * @return the {@link ConstraintBuilder}
     */
    public ConstraintBuilder addUuidConstraint(final String id) {
        return addUuidConstraint(null, id);
    }

    /**
     * Add a uuid constraint expression with an operator in front. The expression uses <code>@jcr:uuid='*'</code>.
     *
     * @param operator the {@link Operator} or null if none
     * @param id       the id to use
     * @return the {@link ConstraintBuilder}
     */
    public ConstraintBuilder addUuidConstraint(final Operator operator, final String id) {
        conditionalSpace();
        conditionalOperator(operator);
        _constraint.append(String.format("@jcr:uuid='%s'", id));
        return this;
    }

    /**
     * Add a template name constraint. The constraint uses <code>@mgnl:template='*'</code> if no placeholders
     * are found. Otherwise the builder uses <code>jcr:like(@mgnl:template, '*')</code>.
     *
     * @param templateName the template name to use
     * @return the {@link ConstraintBuilder}
     */
    public ConstraintBuilder addTplNameConstraint(final String templateName) {
        return addTplNameConstraint(null, templateName);
    }

    /**
     * Add a template name constraint with an operator in front. The constraint uses
     * <code>@mgnl:template='*'</code> if no placeholders are found. Otherwise the builder uses
     * <code>jcr:like(@mgnl:template, '*')</code>.
     *
     * @param operator     the {@link Operator} or null if none
     * @param templateName the template name to use
     * @return the {@link ConstraintBuilder}
     */
    public ConstraintBuilder addTplNameConstraint(final Operator operator, final String templateName) {
        conditionalSpace();
        conditionalOperator(operator);

        if (templateName != null && (templateName.contains("%") || templateName.contains("_"))) {
            _constraint.append(String.format("jcr:like(@mgnl:template, '%s')", templateName));
        } else {
            _constraint.append(String.format("@mgnl:template='%s'", templateName));
        }

        return this;
    }

    public ConstraintBuilder addJcrContains(final String nodeName, final String nodeValue) {
        return addJcrContains(null, nodeName, nodeValue);
    }

    public ConstraintBuilder addJcrContains(final Operator operator, final String nodeName, final String nodeValue) {
        conditionalSpace();
        conditionalOperator(operator);

        _constraint.append(String.format("jcr:contains(%s,'%s')", nodeName, nodeValue));

        return this;
    }

    public String build() {
        return toString();
    }

    @Override
    public String toString() {
        return _constraint.toString();
    }

    private void conditionalOperator(final Operator operator) {
        if (!isGroupOpen() && operator != null) {
            _constraint.append(operator.toXpathString());
            _constraint.append(SPACE);
        }
    }

    private void conditionalSpace() {
        if (isNotBlank() && !isGroupOpen()) {
            _constraint.append(SPACE);
        }
    }

    private boolean isNotBlank() {
        return _constraint.length() != 0;
    }

    private boolean isGroupOpen() {
        return isNotBlank() && _constraint.charAt(_constraint.length() - 1) == PARENTHESES_OPEN;
    }

    /**
     * Simple enums for logic links.
     */
    public enum Operator {
        AND, OR, NOT;

        public String toXpathString() {
            return toString().replaceAll("_", " ").toLowerCase();
        }
    }
}
