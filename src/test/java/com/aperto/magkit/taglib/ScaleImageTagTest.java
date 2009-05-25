package com.aperto.magkit.taglib;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Tests the implementation of {@link ScaleImageTag}.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class ScaleImageTagTest {

    @Test
    public void scale() {
        assertThat(ScaleImageTag.scale(100, 2), equalTo(200));
        assertThat(ScaleImageTag.scale(100, 0.5), equalTo(50));
    }
}