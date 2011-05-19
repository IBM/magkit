package com.aperto.magkit.utils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * A utility class for assertions.
 * Provides a simple flexible interface with methods that use org.hamcrest.Matcher for testing like in org.junit.Assert.assertThat(..).
 * In contrast to JUnit it rises IllegalArgumentException or IllegalStateException if matcher does not match.
 *
 *
 * @author wolf.bubenik
 * @since 19.05.11
 */
public final class Assertions {

    private Assertions() {
    }

    /**
     * Tests if the provided matcher matches for the given object. If not an java.lang.IllegalArgumentException will be thrown.
     *
     * @param object the object to be tested
     * @param matcher the org.hamcrest.Matcher to be used for testing
     * @param <T> the type of the object to be tested
     */
    public static <T> void assertArgument(T object, Matcher<T> matcher) {
        assertArgument(object, matcher, EMPTY);
    }

    /**
     * Tests if the provided matcher matches for the given object. If not an java.lang.IllegalArgumentException will be thrown.
     * The exception message will contain the String 'reason' at the beginning.
     *
     * @param object the object to be tested
     * @param matcher the org.hamcrest.Matcher to be used for testing
     * @param reason an additional String to be included into the exception message
     * @param <T> the type of the object to be tested
     */
    public static <T> void assertArgument(T object, Matcher<T> matcher, String reason) {
        if (!matcher.matches(object)) {
            throw new IllegalArgumentException(getMessage(object, matcher, reason));
        }
    }

    /**
     * Tests if the provided matcher matches for the given object. If not an java.lang.IllegalStateException will be thrown.
     *
     * @param object the object to be tested
     * @param matcher the org.hamcrest.Matcher to be used for testing
     * @param <T> the type of the object to be tested
     */
    public static <T> void assertState(T object, Matcher<T> matcher) {
        assertState(object, matcher, EMPTY);
    }

    /**
     * Tests if the provided matcher matches for the given object. If not an java.lang.IllegalStateException will be thrown.
     * The exception message will contain the String 'reason' at the beginning.
     *
     * @param object the object to be tested
     * @param matcher the org.hamcrest.Matcher to be used for testing
     * @param reason an additional String to be included into the exception message
     * @param <T> the type of the object to be tested
     */
    public static <T> void assertState(T object, Matcher<T> matcher, String reason) {
        if (!matcher.matches(object)) {
            throw new IllegalStateException(getMessage(object, matcher, reason));
        }
    }

    private static <T> String getMessage(T object, Matcher<T> matcher, String reason) {
        Description description = new StringDescription();
        description.appendText("[Assertion failed]: ");
        description.appendText(reason);
        description.appendText("\nExpected: ");
        matcher.describeTo(description);
        description.appendText("\n     got: ").appendValue(object).appendText("\n");
        return description.toString();
    }
}
