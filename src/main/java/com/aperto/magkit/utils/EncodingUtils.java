package com.aperto.magkit.utils;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.net.URLDecoder.decode;
import static java.net.URLEncoder.encode;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.codec.binary.StringUtils.newStringUtf8;
import static org.apache.commons.lang.CharEncoding.UTF_8;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

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
            try {
                base64Encoded = newStringUtf8(encodeBase64(value.getBytes(UTF_8)));
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Error on base64 encoding [{}].", value, e);
            }
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
            try {
                base64Decoded = newStringUtf8(decodeBase64(value.getBytes(UTF_8)));
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Error on base64 decoding [{}].", value, e);
            }
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
        try {
            if (isNotEmpty(value)) {
                parameter = encode(value, UTF_8);
                LOGGER.debug("UrlEncoded string [{}] to [{}].", value, parameter);
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error on url encoding [{}].", value, e);
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
        try {
            urlDecoded = decode(value, UTF_8);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error on decoding url [{}].", value, e);
        }
        return urlDecoded;
    }

    public static String[] getUrlEncodedValues(String[] parameters) {
        List<String> newList = Arrays.stream(parameters).map(EncodingUtils::getUrlEncoded).collect(Collectors.toList());
        return newList.toArray(new String[newList.size()]);
    }
}
