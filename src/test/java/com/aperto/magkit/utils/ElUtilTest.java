package com.aperto.magkit.utils;

import java.util.Map;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import static com.aperto.magkit.utils.ElUtil.methodWrapper;
import org.apache.commons.el.ExpressionEvaluatorImpl;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Tests the implementation of {@link ElUtil}.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class ElUtilTest {

    protected static final String TEST_FIRST_PARAM = "first";
    protected static final Double TEST_SECOND_PARAM = 2.0;
    protected static final Object TEST_RESULT = 42;
    private static final String TEST_METHOD_NAME = "testMe";

    @Test(expected = RuntimeException.class)
    public void noParameters() {
        methodWrapper(this, TEST_METHOD_NAME);
    }

    @Test
    public void singleParameter() {
        Map foo = methodWrapper(this, TEST_METHOD_NAME, String.class);
        assertThat(foo.get(TEST_FIRST_PARAM), equalTo(TEST_RESULT));
    }

    @Test
    public void twoParameters() {
        Map foo = methodWrapper(this, TEST_METHOD_NAME, String.class, Double.class);
        Object bar = foo.get(TEST_FIRST_PARAM);
        assertThat(((Map) bar).get(TEST_SECOND_PARAM), equalTo(TEST_RESULT));
    }

    @Test
    public void expression() throws ELException {
        ExpressionEvaluator evaluator = new ExpressionEvaluatorImpl();
        final Object testInstance = this;
        assertThat(evaluator.evaluate("${myVariable.first[2.0]}", Object.class, new VariableResolver() {
            public Object resolveVariable(final String s) throws ELException {
                if ("myVariable".equals(s)) {
                    return methodWrapper(testInstance, TEST_METHOD_NAME, String.class, Double.class);
                } else {
                    throw new ELException();
                }
            }
        }, null), equalTo(TEST_RESULT));
    }

    public Object testMe() {
        return TEST_RESULT;
    }

    public Object testMe(final String first) {
        assertThat(first, equalTo(TEST_FIRST_PARAM));
        return testMe();
    }

    public Object testMe(final String first, final Double second) {
        assertThat(second, equalTo(TEST_SECOND_PARAM));
        return testMe(first);
    }
}