package com.aperto.magkit.utils;

/**
 * A simple xpath query builder for jcr.
 *
 * @author philipp.guettler
 * @since 16.09.13
 */
public class XpathBuilder {

    public static final String JCR_ROOT_PATH = "/jcr:root";

    public static final String SELECTOR_ALL = "*";
    public static final String SELECTOR_TYPE_EMPTY = "//" + SELECTOR_ALL;
    public static final String SELECTOR_TYPE_ELEMENT = "//element";
    public static final String SELECTOR_ORDER_BY = " order by ";

    public static final String SEPARATOR = ",";

    public static final String PARENTHESES_OPEN = "(";
    public static final String PARENTHESES_CLOSE = ")";

    public static final String BRACKETS_OPEN = "[";
    public static final String BRACKETS_CLOSE = "]";

    private StringBuilder _query = new StringBuilder(JCR_ROOT_PATH);

    /**
     * Creates new {@link XpathBuilder} instance.
     *
     * @return an instance of XpathBuilder
     */
    public static XpathBuilder xPathBuilder() {
        return new XpathBuilder();
    }

    public XpathBuilder path(final String path) {
        _query.append(path);
        return this;
    }

    public XpathBuilder emptyType() {
        _query.append(SELECTOR_TYPE_EMPTY);
        return this;
    }

    /**
     * Short form for <code>type(XpathBuilder.SELECTOR_ALL, nodeType)</code>.
     *
     * @param nodeType the node type
     * @return the builder
     */
    public XpathBuilder type(final String nodeType) {
        return type(SELECTOR_ALL, nodeType);
    }

    public XpathBuilder type(final String nodeName, final String nodeType) {
        _query.append(SELECTOR_TYPE_ELEMENT);
        _query.append(PARENTHESES_OPEN);
        _query.append(nodeName);
        _query.append(SEPARATOR);
        _query.append(nodeType);
        _query.append(PARENTHESES_CLOSE);
        return this;
    }

    public XpathBuilder property(final String constraint) {
        appendProperty(constraint);
        return this;
    }

    public XpathBuilder property(final ConstraintBuilder constraint) {
        appendProperty(constraint);
        return this;
    }

    private void appendProperty(final Object constraint) {
        _query.append(BRACKETS_OPEN);
        _query.append(String.valueOf(constraint));
        _query.append(BRACKETS_CLOSE);
    }

    public XpathBuilder orderBy(final String constraint) {
        _query.append(SELECTOR_ORDER_BY);
        _query.append(constraint);
        return this;
    }

    /**
     * Append a custom xPathExpression.
     *
     * @param xPathExpression a valid xpath expression
     * @return the {@link XpathBuilder}
     */
    public XpathBuilder append(final String xPathExpression) {
        _query.append(xPathExpression);
        return this;
    }

    /**
     * Short form for <code>append(builder.build())</code>.
     *
     * @param builder the {@link XpathBuilder}
     * @return the {@link XpathBuilder} who was appended to
     */
    public XpathBuilder append(final XpathBuilder builder) {
        return append(String.valueOf(builder));
    }

    /**
     * Builds this query and sets it to null internally. Any attempt to modify the query afterwards will result in a {@link NullPointerException}.
     *
     * @return the query as string
     */
    public String build() {
        String result = _query.toString();
        _query = null;
        return result;
    }
}
