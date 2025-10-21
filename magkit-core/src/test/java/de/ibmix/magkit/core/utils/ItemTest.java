package de.ibmix.magkit.core.utils;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2025 IBM iX
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
 * #L% */

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link Item} to ensure correct parsing of keys, extraction of positions and comparison behaviour.
 * Tests focus on edge cases like missing or malformed numeric prefixes, delimiter at first character and tie breaking.
 * Additional tests cover multiple delimiters, zero/negative prefixes, mutator impact and mixed position/non-position comparisons.
 *
 * Contract summary:
 * - getKey strips first numeric prefix and delimiter if present.
 * - getPosition returns parsed int before first delimiter or -1 if none or delimiter at index 0 or malformed digits.
 * - compareTo uses raw key ordering only if THIS instance has a position (>=0), otherwise value then key ordering.
 *
 * @author GitHub Copilot, reviewed by wolf.bubenik@ibmix.de
 * @since 2025-10-20
 */
public class ItemTest {

    /**
     * Verifies that getKey returns the substring after the first delimiter and leaves plain keys unchanged.
     */
    @Test
    public void testGetKey() {
        Item withPosition = new Item("10#someKey", "value");
        assertEquals("someKey", withPosition.getKey());

        Item withoutPosition = new Item("plainKey", "value");
        assertEquals("plainKey", withoutPosition.getKey());

        Item delimiterFirst = new Item("#leading", "value");
        assertEquals("leading", delimiterFirst.getKey());
    }

    /**
     * Verifies that only the first delimiter is considered and the remainder (including further delimiters) is returned.
     */
    @Test
    public void testGetKeyMultipleDelimiters() {
        Item multi = new Item("12#part#rest#tail", "value");
        assertEquals("part#rest#tail", multi.getKey());
    }

    /**
     * Verifies getPosition parsing: valid numeric prefix, no delimiter, delimiter at start and malformed numeric part.
     */
    @Test
    public void testGetPosition() {
        Item withPosition = new Item("10#someKey", "value");
        assertEquals(10, withPosition.getPosition());

        Item noDelimiter = new Item("someKey", "value");
        assertEquals(-1, noDelimiter.getPosition());

        Item delimiterFirst = new Item("#key", "value");
        assertEquals(-1, delimiterFirst.getPosition());

        Item malformed = new Item("abc#key", "value");
        assertEquals(-1, malformed.getPosition());
    }

    /**
     * Verifies edge position cases: zero, leading zeros and negative prefix treated as no position.
     */
    @Test
    public void testGetPositionZeroLeadingAndNegative() {
        Item zero = new Item("0#zero", "v");
        assertEquals(0, zero.getPosition());

        Item leadingZeros = new Item("001#one", "v");
        assertEquals(1, leadingZeros.getPosition());

        Item negative = new Item("-5#neg", "v");
        assertEquals(-5, negative.getPosition());
    }

    /**
     * Verifies compareTo when this item has a position (numeric prefix). In that branch raw key string comparison is used.
     */
    @Test
    public void testCompareWithPositionUsesRawKeyOrdering() {
        Item first = new Item("10#title", "Title");
        Item second = new Item("2#alpha", "Alpha");
        assertTrue(first.compareTo(second) < 0);
    }

    /**
     * Verifies compareTo when left has position prefix and right has no prefix.
     */
    @Test
    public void testComparePositionVsPlain() {
        Item positioned = new Item("5#zeta", "ValZ");
        Item plain = new Item("alpha", "ValA");
        assertTrue(positioned.compareTo(plain) < 0);
    }

    /**
     * Verifies compareTo when only the other item has a position; branch should still use value/key because this has none.
     */
    @Test
    public void testComparePlainVsPositionedOther() {
        Item plain = new Item("plain", "Alpha");
        Item positionedOther = new Item("1#zzz", "Zulu");
        assertTrue(plain.compareTo(positionedOther) < 0);
    }

    /**
     * Verifies compareTo for items without position: compares by value first then by key to break ties.
     */
    @Test
    public void testCompareWithoutPositionValueThenKey() {
        Item a = new Item("keyA", "Same");
        Item b = new Item("keyB", "Same");
        assertTrue(a.compareTo(b) < 0);
    }

    /**
     * Verifies compareTo for different values when no positions are present.
     */
    @Test
    public void testCompareWithoutPositionDifferentValues() {
        Item lower = new Item("k1", "Alpha");
        Item higher = new Item("k2", "Zulu");
        assertTrue(lower.compareTo(higher) < 0);
    }

    /**
     * Verifies compareTo behaviour for negative prefixes: since getPosition() returns a negative value (< -1 is false),
     * the instance is treated as having no position and value comparison applies.
     */
    @Test
    public void testCompareNegativePrefixBehavesLikeNoPosition() {
        Item negative = new Item("-1#b", "Bravo");
        Item other = new Item("2#a", "Alpha");
        assertTrue(negative.compareTo(other) > 0);
    }

    /**
     * Verifies that compareTo throws NullPointerException for null argument as specified.
     */
    @Test
    public void testCompareNull() {
        Item item = new Item("key", "value");
        assertThrows(NullPointerException.class, () -> item.compareTo(null));
    }

    /**
     * Verifies mutators setKey and setValue affect subsequent getters and logic.
     */
    @Test
    public void testMutators() {
        Item item = new Item();
        item.setKey("3#abc");
        item.setValue("Val");
        assertEquals("abc", item.getKey());
        assertEquals(3, item.getPosition());
        assertEquals("Val", item.getValue());
    }
}
