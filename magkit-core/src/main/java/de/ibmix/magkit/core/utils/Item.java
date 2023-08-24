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
 * An item class to capsulate key value pairs.
 *
 * @author frank.sommer (11.12.2007)
 */
public class Item implements Comparable {
    private String _key;
    private String _value;

    public static final String KEY_SORTING_DELIMITER = "#";

    /**
     * Constructor.
     */
    public Item() {
        super();
    }

    /**
     * Constructor with key and value.
     * @param key Key of pair.
     * @param value Value of pair.
     */
    public Item(String key, String value) {
        _key = key;
        _value = value;
    }

    /**
     * if used with xxx# notation to enable sorting through ResourceBundles.
     * @return key
     */
    public String getKey() {
        String result = _key;
        if (_key.contains(KEY_SORTING_DELIMITER)) {
            result = _key.substring(_key.indexOf(KEY_SORTING_DELIMITER) + 1);
        }
        return result;
    }

    /**
     * Gets the position, if one is given in the key, else -1.
     * @return position
     */
    public int getPosition() {
        int position = -1;
        int index = _key.indexOf(KEY_SORTING_DELIMITER);
        if (index > 0) {
            position = NumberUtils.toInt(_key.substring(0, index), -1);
        }
        return position;
    }

    public void setKey(String key) {
        _key = key;
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    /**
     * Implementation for comparing.
     * Compares by position in the keys or alphabetically by value and key.
     */
    public int compareTo(Object o) {
        int returnValue = 0;
        if (Item.class.equals(o.getClass())) {
            Item item = (Item) o;
            if (getPosition() > -1) {
                returnValue = _key.compareTo(item._key);
            } else {
                returnValue = _value.compareTo(item._value);
                if (returnValue == 0) {
                    returnValue = _key.compareTo(item._key);
                }
            }
        }
        return returnValue;
    }
}
