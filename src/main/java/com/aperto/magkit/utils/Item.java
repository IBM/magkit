package com.aperto.magkit.utils;

import org.apache.commons.lang.math.NumberUtils;

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
     *
     * @param key   Key of pair.
     * @param value Value of pair.
     */
    public Item(String key, String value) {
        _key = key;
        _value = value;
    }

    /**
     * if used with xxx# notation to enable sorting through ResourceBundles.
     *
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
     *
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
