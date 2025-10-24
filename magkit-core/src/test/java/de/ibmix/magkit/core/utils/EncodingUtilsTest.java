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

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static de.ibmix.magkit.core.utils.EncodingUtils.URL_HTML_ESCAPER;
import static de.ibmix.magkit.core.utils.EncodingUtils.getBase64Decoded;
import static de.ibmix.magkit.core.utils.EncodingUtils.getBase64Encoded;
import static de.ibmix.magkit.core.utils.EncodingUtils.getUrlDecoded;
import static de.ibmix.magkit.core.utils.EncodingUtils.getUrlEncoded;
import static de.ibmix.magkit.core.utils.EncodingUtils.getUrlEncodedValues;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the Encoding Utils.
 *
 * @author oliver.emke, Aperto AG
 * @since 14.03.11
 */
public class EncodingUtilsTest {

    @Test
    public void testGetBase64Encoded() {
        Map<String, String> testData = createBase64EncodeTestData();
        for (Map.Entry<String, String> entrySet : testData.entrySet()) {
            assertEquals(entrySet.getValue(), getBase64Encoded(entrySet.getKey()), "Testing [" + entrySet.getKey() + "] ...");
        }
    }

    @Test
    public void encodeDecodeUnterschleissheim() {
        String value = "Unterschlei\u00dfheim";
        String base64Encoded = getBase64Encoded(value);
        assertEquals(value, getBase64Decoded(base64Encoded));
    }

    @Test
    public void testGetBase64DecodedEdgeCases() {
        assertEquals("", getBase64Decoded(null));
        assertEquals("", getBase64Decoded(""));
        assertEquals("", getBase64Decoded("   "));
        assertEquals("hello world", getBase64Decoded("aGVsbG8gd29ybGQ="));
    }

    @Test
    public void testGetUrlEncoded() {
        Map<String, String> testData = createUrlEncodeTestData();
        for (Map.Entry<String, String> entrySet : testData.entrySet()) {
            assertEquals(entrySet.getValue(), getUrlEncoded(entrySet.getKey()), "Testing [" + entrySet.getKey() + "] ...");
        }
    }

    @Test
    public void testGetUrlEncodedBlankSpace() {
        assertEquals("+", getUrlEncoded(" "));
    }

    @Test
    public void testGetUrlDecoded() {
        assertEquals("", getUrlDecoded(null));
        assertEquals("", getUrlDecoded(""));
        assertEquals("unternehmensf?hrung", getUrlDecoded("unternehmensf%3Fhrung"));
        assertEquals("Candida & Terasuisse", getUrlDecoded("Candida+%26+Terasuisse"));
        assertEquals("\r\n", getUrlDecoded("%0D%0A"));
        assertEquals(" ", getUrlDecoded("+"));
    }

    @Test
    public void testUrlHtmlEscaper() {
        String input = "<&>\"'";
        // & is not escaped by the reduced escaper
        String expected = "&lt;&&gt;&quot;&#39;";
        assertEquals(expected, URL_HTML_ESCAPER.escape(input));
    }

    @Test
    public void testEncodeUrlParameters() {
        Map<String, String> testData = createUrlEncodeTestData();
        String[] keys = testData.keySet().toArray(new String[0]);
        String[] values = testData.values().toArray(new String[0]);
        String[] result = getUrlEncodedValues(keys);
        for (int i = 0; i < keys.length; i++) {
            assertEquals(values[i], result[i], "Testing [" + keys[i] + "] ...");
        }
    }

    /**
     * Returns testData map with to encode param and excepted value.
     *
     * @return testData
     */
    private Map<String, String> createBase64EncodeTestData() {
        Map<String, String> testData = new HashMap<>();
        testData.put(null, "");
        testData.put("fweoij+++weweofjweofwef+w+we+", "Zndlb2lqKysrd2V3ZW9mandlb2Z3ZWYrdyt3ZSs=");
        testData.put(getUrlEncoded("M?nchen"), "TSUzRm5jaGVu");
        testData.put(getUrlEncoded("unternehmensf?hrung"), "dW50ZXJuZWhtZW5zZiUzRmhydW5n");
        testData.put("", "");
        testData.put("FREETEXT=Unternehmensf%C3%BChrung&COUNTRY=FR&FUNC_AREA_NB", "RlJFRVRFWFQ9VW50ZXJuZWhtZW5zZiVDMyVCQ2hydW5nJkNPVU5UUlk9RlImRlVOQ19BUkVBX05C");
        testData.put(getUrlEncoded("??==`?)=)(?`()=$(?$`?$$?$`?$=?`$=?$$=???$"), "JTNGJTNGJTNEJTNEJTYwJTNGJTI5JTNEJTI5JTI4JTNGJTYwJTI4JTI5JTNEJTI0JTI4JTNGJTI0JTYwJTNGJTI0JTI0JTNGJTI0JTYwJTNGJTI0JTNEJTNGJTYwJTI0JTNEJTNGJTI0JTI0JTNEJTNGJTNGJTNGJTI0");
        return testData;
    }

    /**
     * Returns testData map with to encode param and excepted value.
     *
     * @return testData
     */
    private Map<String, String> createUrlEncodeTestData() {
        Map<String, String> testData = new HashMap<>();
        testData.put(null, "");
        testData.put("", "");
        testData.put("unternehmensf?hrung", "unternehmensf%3Fhrung");
        testData.put("M?nchen", "M%3Fnchen");
        testData.put("j??w +--   ///", "j%3F%3Fw+%2B--+++%2F%2F%2F");
        testData.put("#", "%23");
        testData.put("Candida & Terasuisse", "Candida+%26+Terasuisse");
        testData.put("\r\n", "%0D%0A");
        return testData;
    }
}
