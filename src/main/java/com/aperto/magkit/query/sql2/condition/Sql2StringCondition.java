package com.aperto.magkit.query.sql2.condition;

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

import info.magnolia.jcr.util.NodeTypes;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;

/**
 * The builder for a sql2 String condition.
 *
 * @author wolf.bubenik@aperto.com
 * @since 01.04.2020
 **/
public class Sql2StringCondition extends Sql2PropertyCondition<Sql2StringCondition, String> implements Sql2DynamicOperand {

    public static final String SQL2_OP_LIKE = " LIKE ";
    public static final String METHOD_LOWER = "lower";
    public static final String METHOD_UPPER = "upper";
    public static final String METHOD_LENGTH = "length";

    private boolean _startsWith;
    private boolean _endsWith;
    private boolean _contains;
    private boolean _isLike = false;
    private String _operandMethod;

    protected Sql2StringCondition(final String property) {
        super(property);
    }

    public static Sql2DynamicOperand property(final String name) {
        return new Sql2StringCondition(name);
    }

    public static Sql2DynamicOperand template() {
        return property(NodeTypes.Renderable.TEMPLATE);
    }

    public static Sql2DynamicOperand identifier() {
        return property(JcrConstants.JCR_UUID);
    }

    // Implement the abstract-me()-trick to have special return types on methods of parent class.
    @Override
    protected Sql2StringCondition me() {
        return this;
    }

    public Sql2CompareString not() {
        super.not();
        return me();
    }

    public Sql2CompareStringNot lowerCase() {
        _operandMethod = METHOD_LOWER;
        return me();
    }

    public Sql2CompareStringNot upperCase() {
        _operandMethod = METHOD_UPPER;
        return me();
    }

    public Sql2CompareNot<Long> length() {
        _operandMethod = METHOD_LENGTH;
        // todo: what to return??
        return null;
    }

    public Sql2StaticOperandMultiple<String> startsWithAny() {
        _startsWith = true;
        _isLike = true;
        return me();
    }

    public Sql2StaticOperandMultiple<String> endsWithAny() {
        _endsWith = true;
        _isLike = true;
        return me();
    }

    public Sql2StaticOperandMultiple<String> likeAny() {
        _contains = true;
        _isLike = true;
        return me();
    }

    public Sql2StaticOperandMultiple<String> likeAll() {
        _contains = true;
        _isLike = true;
        withOperator(SQL2_OP_AND);
        return me();
    }

    @Override
    protected void appendValueConstraint(final StringBuilder sql2, final String selectorName, final String name, final String value) {
        if (value != null && (!_isLike || StringUtils.isNotEmpty(value))) {
            final String end = (_startsWith || _contains) ? "%'" : "'";
            final String begin = (_endsWith || _contains) ? "'%" : "'";
            final String cleanValue = _isLike ? value.replaceAll("'", "''").replaceAll("%", "\\\\%").replaceAll("_", "\\\\_") : value.replaceAll("'", "''");

            if (StringUtils.isNotEmpty(_operandMethod)) {
                sql2.append(_operandMethod).append('(');
            }

            if (StringUtils.isNotEmpty(selectorName)) {
                sql2.append(selectorName).append('.');
            }
            sql2.append('[').append(name).append(']');

            if (StringUtils.isNotEmpty(_operandMethod)) {
                sql2.append(')');
            }

            sql2.append(getCompareOperator()).append(begin).append(cleanValue).append(end);
        }
    }

    @Override
    protected String getCompareOperator() {
        return _isLike ? SQL2_OP_LIKE : super.getCompareOperator();
    }
}
