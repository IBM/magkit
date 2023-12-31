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
 * Encoding Utils for string encoding.
 *
 * @author oliver.emke, Aperto AG
 * @since 14.03.11
 */
public final class EncodingUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncodingUtils.class);

    /**
     * Reduced XHTML encoding for URL selector and parameters. Does not escape &amp; (ampersands) because it may be part of URL parameters.
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
     * Encodes given string base64.
     *
     * @param value string to encode
     * @return encoded string or <code>StringUtils.EMPTY</code>
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
     * Decodes given string base64.
     *
     * @param value string to decode
     * @return decoded string or <code>StringUtils.EMPTY</code>
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
     * URL encodes given string.
     *
     * @param value string to encode
     * @return encoded string or <code>StringUtils.EMPTY</code>
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
     * URL decode for given string.
     *
     * @param value string to encode
     * @return decoded string or <code>StringUtils.EMPTY</code>
     */
    public static String getUrlDecoded(final String value) {
        String urlDecoded = EMPTY;
        if (isNotEmpty(value)) {
            urlDecoded = decode(value, UTF_8);
        }
        return urlDecoded;
    }

    public static String[] getUrlEncodedValues(String[] parameters) {
        return Arrays.stream(parameters).map(EncodingUtils::getUrlEncoded).toArray(String[]::new);
    }
}
