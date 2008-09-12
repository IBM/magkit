package com.aperto.magkit.controller;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test of the search controller.
 * //TODO: test sth. more.
 *
 * @author frank.sommer (12.09.2008)
 */
public class SearchControllerTest {
    @Test
    public void testDeterminClosingTag() {
        String s = SearchController.determineClosingTag("<strong class=\"test\">");
        assertThat(s, is("</strong>"));
        s = SearchController.determineClosingTag("<strong>");
        assertThat(s, is("</strong>"));
        s = SearchController.determineClosingTag("strong");
        assertThat(s, is("strong"));
        s = SearchController.determineClosingTag("");
        assertThat(s, is(""));
    }
}
