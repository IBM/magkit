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

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static de.ibmix.magkit.core.utils.EncodingUtils.getBase64Decoded;
import static de.ibmix.magkit.core.utils.EncodingUtils.getBase64Encoded;
import static de.ibmix.magkit.core.utils.EncodingUtils.getUrlEncoded;
import static de.ibmix.magkit.core.utils.EncodingUtils.getUrlEncodedValues;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

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
            assertEquals("Testing [" + entrySet.getKey() + "] ...", entrySet.getValue(), getBase64Encoded(entrySet.getKey()));
        }
    }

    @Test
    public void encodeDecodeUnterschleissheim() {
        String value = "Unterschlei\u00dfheim";
        String base64Encoded = getBase64Encoded(value);
        MatcherAssert.assertThat(value, equalTo(getBase64Decoded(base64Encoded)));
    }

    @Test
    public void testGetUrlEncoded() {
        Map<String, String> testData = createUrlEncodeTestData();
        for (Map.Entry<String, String> entrySet : testData.entrySet()) {
            assertEquals("Testing [" + entrySet.getKey() + "] ...", entrySet.getValue(), getUrlEncoded(entrySet.getKey()));
        }
    }

    @Test
    public void testEncodeUrlParameters() {
        Map<String, String> testData = createUrlEncodeTestData();
        String[] keys = testData.keySet().toArray(new String[testData.keySet().size()]);
        String[] values = testData.values().toArray(new String[testData.keySet().size()]);
        String[] result = getUrlEncodedValues(keys);
        for (int i = 0; i < keys.length; i++) {
            assertEquals("Testing [" + keys[i] + "] ...", values[i], result[i]);
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
