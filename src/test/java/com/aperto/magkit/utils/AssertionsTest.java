package com.aperto.magkit.utils;

import org.hamcrest.Matcher;
import org.junit.Test;

import static com.aperto.magkit.utils.Assertions.assertArgument;
import static com.aperto.magkit.utils.Assertions.assertState;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik
 * @since 19.05.11
 */
public class AssertionsTest {

    @Test
    public void testAssertArgumentNoReason() throws Exception {
        Object probe = new Object();
        Matcher<Object> m = mock(Matcher.class);
        when(m.matches(probe)).thenReturn(true);
        assertArgument(probe, m);
        verify(m, times(1)).matches(probe);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAssertArgument() throws Exception {
        Object probe = new Object();
        Matcher<Object> m = mock(Matcher.class);
        when(m.matches(probe)).thenReturn(true);
        assertArgument(probe, m, "message");
        verify(m, times(1)).matches(probe);

        when(m.matches(probe)).thenReturn(false);
        assertArgument(probe, m, "message");
    }

    @Test
    public void testAssertStateNoReason() throws Exception {
        Object probe = new Object();
        Matcher<Object> m = mock(Matcher.class);
        when(m.matches(probe)).thenReturn(true);
        assertState(probe, m);
        verify(m, times(1)).matches(probe);
    }

    @Test(expected = IllegalStateException.class)
    public void testAssertState() throws Exception {
        Object probe = new Object();
        Matcher<Object> m = mock(Matcher.class);
        when(m.matches(probe)).thenReturn(true);
        assertState(probe, m, "message");
        verify(m, times(1)).matches(probe);

        when(m.matches(probe)).thenReturn(false);
        assertState(probe, m, "message");
    }
}
