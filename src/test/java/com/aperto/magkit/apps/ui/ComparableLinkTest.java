package com.aperto.magkit.apps.ui;

import com.google.common.collect.ImmutableList;
import com.vaadin.ui.Link;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests {@link ComparableLink}.
 *
 * @author oliver.emke
 * @since 27.01.17
 */
public class ComparableLinkTest {

    @Test
    public void testEquals() {
        final String labelValue1 = "This is a Test with a long value";
        final String labelValue2 = "labelValue2";
        final String labelValueEmpty = "";

        final Link labelOne = new ComparableLink(labelValue1, null);
        final Link labelOneSameLabelText = new ComparableLink(labelValue1, null);
        final Link labelTwo = new ComparableLink(labelValue2, null);
        final Link labelEmpty = new ComparableLink(labelValueEmpty, null);
        final Link nullLabel = new ComparableLink(null, null);

        assertThat(labelOne.equals(labelOne), is(true));
        assertThat(labelOneSameLabelText.equals(labelOneSameLabelText), is(true));
        assertThat(labelOne.equals(labelTwo), is(false));
        assertThat(labelTwo.equals(labelOne), is(false));
        assertThat(labelEmpty.equals(labelEmpty), is(true));
        assertThat(nullLabel.equals(nullLabel), is(true));
        assertThat(nullLabel.equals(labelEmpty), is(false));
    }

    @Test
    public void testComparing() {
        final String labelValueFirst = "oalli";
        final String labelValueSecond = "Oalli";
        final String labelValueThird = "olli";
        final ComparableLink first = new ComparableLink(labelValueFirst, null);
        final ComparableLink second = new ComparableLink(labelValueSecond, null);
        final ComparableLink third = new ComparableLink(labelValueThird, null);
        final ComparableLink nullCaptionLink = new ComparableLink(null, null);

        final List<ComparableLink> labels = new ArrayList<>();
        labels.addAll(ImmutableList.of(third, first, second, nullCaptionLink));
        Collections.shuffle(labels);
        Collections.sort(labels);
        assertThat(labels.get(0), is(nullCaptionLink));
        assertThat(labels.get(1), is(first));
        assertThat(labels.get(2), is(second));
        assertThat(labels.get(3), is(third));
    }

}