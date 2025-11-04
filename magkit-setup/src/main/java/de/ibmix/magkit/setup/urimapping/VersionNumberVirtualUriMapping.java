package de.ibmix.magkit.setup.urimapping;

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

import info.magnolia.virtualuri.VirtualUriMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.removeEnd;

/**
 * Maps versioned URIs (containing a build or release version segment) to target URIs by stripping the version part
 * from the path. This provides a readable alternative to Magnolia's original regexp based virtual URI mappings.
 * <p>
 * Core functionality:
 * <ul>
 *   <li>Identifies a leading version segment below a configured {@code fromPrefix}.</li>
 *   <li>Validates the version segment against a configurable pattern (defaults to {@link #GIT_PATTERN}).</li>
 *   <li>Builds a target URI either via a provided {@code toUri} format string or by reusing the original prefix.</li>
 *   <li>Exposes a mapping result with a configurable level influencing Magnolia's mapping resolution order.</li>
 * </ul>
 * Key features and details:
 * <ul>
 *   <li>Supports both git style (e.g. {@code 1.0.0-master-4-d760a70}) and legacy SVN style (e.g. {@code 1.0.1234}) patterns.</li>
 *   <li>Pattern is fully customizable through {@link #setPattern(String)} (falls back to {@link #GIT_PATTERN} if {@code null}).</li>
 *   <li>Graceful handling of incomplete configuration: emits a warning if {@code fromPrefix} is missing.</li>
 *   <li>Thread-safety: This class is NOT thread-safe for concurrent reconfiguration (setters) while mappings are executed. Safe for read-only usage after initialization.</li>
 * </ul>
 * Usage preconditions:
 * <ul>
 *   <li>{@code fromPrefix} must be configured and end with a slash (enforced internally).</li>
 *   <li>{@code toUri} should contain a single {@code %s} placeholder if a custom target format is desired.</li>
 * </ul>
 * Null and error handling:
 * <ul>
 *   <li>{@code pattern == null} resets to the default {@link #GIT_PATTERN}.</li>
 *   <li>Invalid or absent version segments result in an empty mapping {@link Optional}.</li>
 * </ul>
 * Side effects: Reconfiguration (invoking setters) replaces the compiled regex instance used for validation.
 * <p>
 * Example:
 * <pre>{@code
 * VersionNumberVirtualUriMapping m = new VersionNumberVirtualUriMapping();
 * m.setFromPrefix("/app/releases");
 * m.setToUri("/app/%s");
 * // URI: /app/releases/1.2.3-master-5-abcdef0/home => /app/home
 * Optional<VirtualUriMapping.Result> r = m.mapUri(URI.create("/app/releases/1.2.3-master-5-abcdef0/home"));
 * }</pre>
 *
 * @author daniel.kasmeroglu@aperto.de
 */
public class VersionNumberVirtualUriMapping implements VirtualUriMapping {
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionNumberVirtualUriMapping.class);

    /**
     * Default git version pattern, like 1.0.0-master-4-d760a70.
     */
    public static final String GIT_PATTERN = "[0-9]+\\.[0-9]+(\\.[0-9]+)?-[^/]+";

    /**
     * Old subversion version pattern, like 1.0.1234.
     */
    public static final String SVN_PATTERN = "[\\.0-9]+";

    private int _level = 1;
    private String _fromPrefix = null;
    private String _toUri = null;
    private String _pattern = GIT_PATTERN;
    private Pattern _regexp = Pattern.compile(_pattern);

    /**
     * Returns the currently configured version validation pattern.
     *
     * @return the regexp string used to validate version segments
     */
    public String getPattern() {
        return _pattern;
    }

    /**
     * Sets the version validation pattern. If {@code pattern} is {@code null}, the default {@link #GIT_PATTERN} is applied.
     * Rebuilds the internal compiled {@link Pattern} instance.
     *
     * @param pattern the new regexp pattern or {@code null} to reset to default
     */
    public void setPattern(String pattern) {
        _pattern = pattern;
        if (_pattern == null) {
            _pattern = GIT_PATTERN;
        }
        _regexp = Pattern.compile(_pattern);
    }

    /**
     * Returns the mapping level influencing Magnolia's resolution order.
     *
     * @return the level value
     */
    public int getLevel() {
        return _level;
    }

    /**
     * Sets the mapping level influencing Magnolia's resolution order.
     *
     * @param level the level value
     */
    public void setLevel(int level) {
        _level = level;
    }

    /**
     * Returns the configured source prefix (always ending with a slash) from which versioned paths are expected.
     *
     * @return the source prefix or {@code null} if not configured
     */
    public String getFromPrefix() {
        return _fromPrefix;
    }

    /**
     * Sets the source prefix. Ensures the stored value ends with a trailing slash.
     *
     * @param fromPrefix the source prefix (may end with or without slash)
     */
    public void setFromPrefix(String fromPrefix) {
        _fromPrefix = removeEnd(fromPrefix, "/") + "/";
    }

    /**
     * Returns the target URI format string (may include a single {@code %s} placeholder) or {@code null} for default behaviour.
     *
     * @return the target URI format or {@code null}
     */
    public String getToUri() {
        return _toUri;
    }

    /**
     * Sets the target URI format string. Should include a single {@code %s} placeholder for the remainder path.
     *
     * @param toUri the target URI format string
     */
    public void setToUri(String toUri) {
        _toUri = toUri;
    }

    /**
     * Attempts to map the given {@link URI}. If the URI starts with the configured {@code fromPrefix}, contains a valid
     * version segment and a remainder path, returns a mapping {@link Result} with the version removed.
     *
     * @param uri the input URI
     * @return an {@link Optional} containing the mapping result or empty if no mapping applies
     */
    @Override
    public Optional<Result> mapUri(URI uri) {
        Optional<Result> result = Optional.empty();
        String url = uri.toString();
        if (_fromPrefix == null) {
            LOGGER.warn("Incomplete configuration. fromPrefix is not set.");
        } else if (url.startsWith(_fromPrefix)) {
            // we gotta prefix, so check if we've got a version candidate
            String suffix = url.substring(_fromPrefix.length());
            int idx = suffix.indexOf('/');
            if (idx > 0) {
                // we gotta version candidate, so check if it's supported by us
                String versionPart = suffix.substring(0, idx);
                if (isValidVersion(versionPart)) {
                    // we support this version pattern, so it can be dropped
                    String rest = suffix.substring(idx + 1);
                    String toUri = buildToUri(rest);
                    result = Optional.of(new Result(toUri, _level, this));
                }
            }
        }
        return result;
    }

    /**
     * Indicates whether this mapping instance is properly configured to perform mappings.
     *
     * @return {@code true} if both {@code fromPrefix} and {@code toUri} are non-blank, otherwise {@code false}
     */
    @Override
    public boolean isValid() {
        return isNotBlank(getFromPrefix()) && isNotBlank(getToUri());
    }

    /**
     * Builds the target URI using the configured {@code toUri} format or the original prefix as a fallback.
     *
     * @param rest the remainder path after stripping the version segment
     * @return the computed target URI
     */
    private String buildToUri(final String rest) {
        String toUri;
        if (_toUri != null) {
            toUri = String.format(_toUri, rest);
        } else {
            toUri = String.format("%s%s", _fromPrefix, rest);
        }
        return toUri;
    }

    /**
     * Validates the supplied version string against the compiled pattern.
     *
     * @param version the version segment candidate
     * @return {@code true} if the version matches the pattern, otherwise {@code false}
     */
    private boolean isValidVersion(String version) {
        return _regexp.matcher(version).matches();
    }

    /**
     * Returns a human readable representation containing pattern and mapping overview.
     *
     * @return a descriptive string for debugging/logging
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[pattern=" + _pattern + ": " + _fromPrefix + " --> " + buildToUri("...") + "]";
    }
}
