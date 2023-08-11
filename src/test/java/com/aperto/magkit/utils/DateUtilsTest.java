package com.aperto.magkit.utils;

import static com.aperto.magkit.utils.DateUtils.createQueryDate;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Test;

/**
 * Test for DateUtils.
 *
 * @author frank.sommer
 * @since 08.04.13
 */
public class DateUtilsTest {

    @Test
    public void testCreateQueryDate() throws Exception {
        Date date = new Date(1234567890);
        String queryDate = createQueryDate(date);
        assertThat(queryDate, equalTo("1970-01-15T07:56:07.890+01:00"));
    }
}
