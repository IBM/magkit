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

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static java.net.URLDecoder.decode;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.codec.binary.StringUtils.newStringUtf8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Utility class offering helpers for common string encoding/decoding tasks used across Magnolia integration code.
 * <p>
 * Main features:
 * </p>
 * <ul>
 *   <li>Base64 encoding and decoding using UTF-8.</li>
 *   <li>URL encoding/decoding using UTF-8.</li>
 *   <li>Reduced HTML escaping for embedding values safely in Magnolia URL selectors without escaping ampersands.</li>
 * </ul>
 * <p>Important details:</p>
 * <ul>
 *   <li>All methods are null-safe: a {@code null}, empty or blank input results in an empty string.</li>
 *   <li>Debug logging is performed for successful (de-)encoding operations.</li>
 * </ul>
 * <p>Usage example:</p>
 * <pre>{@code
 * String encoded = EncodingUtils.getBase64Encoded("hello world");
 * String decoded = EncodingUtils.getBase64Decoded(encoded);
 * String urlParam = EncodingUtils.getUrlEncoded("a value with spaces");
 * String[] encodedParams = EncodingUtils.getUrlEncodedValues(new String[]{"v1", "v2"});
 * String htmlSafe = EncodingUtils.URL_HTML_ESCAPER.escape("&lt;tag&gt\"quote\"&lt;/tag&gt");
 * }</pre>
 * <p>Null & error handling: Invalid Base64 input will decode to an arbitrary string (no explicit validation performed).</p>
 * <p>Side effects: Only debug log statements; no external state modifications.</p>
 * <p>Thread-safety: All operations are pure and use only local variables.</p>
 *
 * @author oliver.emke, Aperto AG
 * @since 2011-03-14
 */
public final class EncodingUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncodingUtils.class);

    /**
     * Reduced XHTML encoding for URL selector and parameters. Does not escape ampersands because they may be part of URL parameters.
     */
    public static final Escaper URL_HTML_ESCAPER = Escapers.builder()
        .addEscape('"', "&quot;")
        .addEscape('\'', "&#39;")
        .addEscape('<', "&lt;")
        .addEscape('>', "&gt;")
        .build();

    private EncodingUtils() {
        //empty private constructor
    }

    /**
     * Returns the Base64 encoded representation of the given value using UTF-8.
     * Returns an empty string if the input is null, empty or blank.
     *
     * @param value the string to encode (may be null)
     * @return Base64 encoded string or empty string if input was null/blank
     */
    public static String getBase64Encoded(String value) {
        String base64Encoded = EMPTY;
        if (isNotBlank(value)) {
            base64Encoded = newStringUtf8(encodeBase64(value.getBytes(UTF_8)));
            LOGGER.debug("Encoded string [{}] to base64 [{}].", value, base64Encoded);
        }
        return base64Encoded;
    }

    /**
     * Decodes the given Base64 encoded value using UTF-8.
     * Returns an empty string if input is null, empty or blank. Invalid Base64 content is decoded without validation.
     *
     * @param value the Base64 encoded string to decode (may be null)
     * @return decoded string or empty string if input was null/blank
     */
    public static String getBase64Decoded(String value) {
        String base64Decoded = EMPTY;
        if (isNotBlank(value)) {
            base64Decoded = newStringUtf8(decodeBase64(value.getBytes(UTF_8)));
            LOGGER.debug("Decoded string [{}] to base64 [{}].", value, base64Decoded);
        }
        return base64Decoded;
    }

    /**
     * URL-encodes the given value using UTF-8. Returns an empty string if input is null or empty.
     * Spaces become {@code +}; reserved characters are percent-encoded.
     *
     * @param value the string to URL-encode (may be null)
     * @return URL-encoded string or empty string if input was null/empty
     */
    public static String getUrlEncoded(String value) {
        String parameter = EMPTY;
        if (isNotEmpty(value)) {
            parameter = encode(value, UTF_8);
            LOGGER.debug("UrlEncoded string [{}] to [{}].", value, parameter);
        }
        return parameter;
    }

    /**
     * URL-decodes the given value using UTF-8. Returns an empty string if input is null or empty.
     * Plus signs are converted back to spaces and percent-encoded sequences are resolved.
     *
     * @param value the URL-encoded string to decode (may be null)
     * @return decoded string or empty string if input was null/empty
     */
    public static String getUrlDecoded(final String value) {
        String urlDecoded = EMPTY;
        if (isNotEmpty(value)) {
            urlDecoded = decode(value, UTF_8);
        }
        return urlDecoded;
    }

    /**
     * URL-encodes each value in the provided array using UTF-8.
     * Null or empty elements yield empty strings in the resulting array.
     *
     * @param parameters array of raw parameter values (must not be null)
     * @return new array containing URL-encoded representations (never null)
     */
    public static String[] getUrlEncodedValues(String[] parameters) {
        return Arrays.stream(parameters).map(EncodingUtils::getUrlEncoded).toArray(String[]::new);
    }
}
