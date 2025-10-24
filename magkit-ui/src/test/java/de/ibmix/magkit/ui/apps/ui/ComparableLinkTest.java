package de.ibmix.magkit.ui.apps.ui;

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

import com.google.common.collect.ImmutableList;
import com.vaadin.ui.Link;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ComparableLink} ordering and equality behavior.
 *
 * @author oliver.emke
 * @since 2017-01-27
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

        assertTrue(labelOne.equals(labelOne));
        assertTrue(labelOneSameLabelText.equals(labelOneSameLabelText));
        assertFalse(labelOne.equals(labelTwo));
        assertFalse(labelTwo.equals(labelOne));
        assertTrue(labelEmpty.equals(labelEmpty));
        assertTrue(nullLabel.equals(nullLabel));
        assertFalse(nullLabel.equals(labelEmpty));
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
        assertEquals(nullCaptionLink, labels.get(0));
        assertEquals(first, labels.get(1));
        assertEquals(second, labels.get(2));
        assertEquals(third, labels.get(3));
    }

}
