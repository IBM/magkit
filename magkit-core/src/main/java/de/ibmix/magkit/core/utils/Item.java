package de.ibmix.magkit.core.utils;

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

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Represents a simple key/value pair that optionally encodes a sortable numeric position in the key.
 * <p>
 * A key may start with an integer followed by {@link #KEY_SORTING_DELIMITER} (e.g. "10#myKey"). If present, the integer
 * portion defines the position for ordering. The {@code getKey()} method returns the textual part after the delimiter,
 * while {@code getPosition()} returns the numeric prefix or {@code -1} if none exists. Instances can be compared using
 * {@link #compareTo(Item)}: if a position is encoded, comparison is done by raw key (thus by position first then the
 * remainder); otherwise comparison falls back to value, then key for tie-breaking.
 * </p>
 * <p>Usage Preconditions</p>
 * Keys and values should be non-null. Supplying a null key will lead to a {@link NullPointerException} in several
 * accessor methods.
 * <p>Null Handling</p>
 * This class does not internally guard against null keys or values. Callers must ensure non-null inputs.
 * <p>Side Effects</p>
 * The class is mutable via {@link #setKey(String)} and {@link #setValue(String)}; mutating after insertion into a sorted
 * collection may invalidate ordering assumptions.
 * <p>Thread-Safety</p>
 * Not thread-safe; synchronize externally if instances are shared across threads and mutated.
 * <p>Example</p>
 * <pre>{@code
 * Item positioned = new Item("10#title", "Title");
 * Item plain = new Item("identifier", "Display");
 * List<Item> list = Arrays.asList(positioned, plain);
 * Collections.sort(list); // positioned will come first due to numeric prefix
 * }</pre>
 *
 * @author frank.sommer (11.12.2007)
 * @since 2007-12-11
 */
public class Item implements Comparable<Item> {
    /**
     * Delimiter separating an optional numeric position prefix from the logical key text.
     */
    public static final String KEY_SORTING_DELIMITER = "#";

    private String _key;
    private String _value;

    /**
     * Default constructor creating an empty Item. Key and value must be set before meaningful use.
     */
    public Item() {
        super();
    }

    /**
     * Constructs an Item with the provided key and value.
     * The key may optionally contain a numeric prefix followed by {@link #KEY_SORTING_DELIMITER} to define ordering.
     *
     * @param key the key, optionally with position prefix (must be non-null)
     * @param value the value (must be non-null)
     */
    public Item(String key, String value) {
        _key = key;
        _value = value;
    }

    /**
     * Returns the logical key without any leading position prefix when the {@code position#key} notation is used.
     * If no delimiter is present the raw key is returned.
     *
     * @return the logical key text (never null if key was non-null)
     */
    public String getKey() {
        String result = _key;
        if (_key.contains(KEY_SORTING_DELIMITER)) {
            result = _key.substring(_key.indexOf(KEY_SORTING_DELIMITER) + 1);
        }
        return result;
    }

    /**
     * Returns the numeric position encoded at the start of the key or {@code -1} if none exists.
     * The position must be a valid integer prior to the first {@link #KEY_SORTING_DELIMITER}.
     *
     * @return the position or -1 if no position is encoded
     */
    public int getPosition() {
        int position = -1;
        int index = _key.indexOf(KEY_SORTING_DELIMITER);
        if (index > 0) {
            position = NumberUtils.toInt(_key.substring(0, index), -1);
        }
        return position;
    }

    /**
     * Sets the raw key. May include a position prefix. Must be non-null.
     *
     * @param key the new key
     */
    public void setKey(String key) {
        _key = key;
    }

    /**
     * Returns the stored value.
     *
     * @return the value (may be null if not yet assigned)
     */
    public String getValue() {
        return _value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(String value) {
        _value = value;
    }

    /**
     * Compares this item to another. If this item encodes a position (numeric prefix), comparison is performed on the
     * raw key strings (thereby using the numeric ordering then lexicographic for the remainder). Otherwise comparison
     * uses the value lexicographically; ties are broken by the key. A {@code null} argument will cause a
     * {@link NullPointerException}.
     *
     * @param item the other item (must be non-null)
     * @return negative, zero, or positive per standard {@link Comparable} contract
     * @throws NullPointerException if item is null
     */
    @Override
    public int compareTo(Item item) {
        if (item == null) {
            throw new NullPointerException("Item to compare must not be null");
        }
        int returnValue;
        if (getPosition() > -1) {
            returnValue = _key.compareTo(item._key);
        } else {
            returnValue = _value.compareTo(item._value);
            if (returnValue == 0) {
                returnValue = _key.compareTo(item._key);
            }
        }
        return returnValue;
    }
}
