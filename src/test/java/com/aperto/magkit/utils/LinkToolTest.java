package com.aperto.magkit.utils;

import static com.aperto.magkit.utils.LinkTool.isUuid;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Test the LinkTool class.
 *
 * @author frank.sommer
 * @since 17.01.13
 */
public class LinkToolTest {

    @Test
    public void testIsUuid() throws Exception {
        assertThat(isUuid(null), is(false));
        assertThat(isUuid(""), is(false));
        assertThat(isUuid("www.aperto.de"), is(false));
        assertThat(isUuid("12345-45454-54545"), is(false));
        assertThat(isUuid("dc307c08-5a19-4260-a304-a5611d1ca900 1"), is(false));
        assertThat(isUuid("dc307c08-5a19-4260-a304-a5611d1ca900"), is(true));
    }
}
