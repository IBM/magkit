package com.aperto.magkit.utils;

import org.junit.Before;
import org.junit.Test;

import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubCharacterEncoding;
import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubSelector;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockAggregationState;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test of the resource utils.
 *
 * @author frank.sommer (11.12.2008)
 * @see SelectorUtils
 */
public class SelectorUtilsTest {
    @Test
    public void retrieveValueOfSelector() {
        String value = SelectorUtils.retrieveValueOfSelector("pid");
        assertThat(value, is("2"));
        value = SelectorUtils.retrieveValueOfSelector("kid");
        assertThat(value, is("test"));
        value = SelectorUtils.retrieveValueOfSelector("sid");
        assertThat(value, is(""));
    }

    @Before
    public void createMgnlContext() {
        cleanContext();
        mockAggregationState(
            stubCharacterEncoding("UTF-8"),
            stubSelector("aid-6.pid-2.kid-test")
        );
    }
}
