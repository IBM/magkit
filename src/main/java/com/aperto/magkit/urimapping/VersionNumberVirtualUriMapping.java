package com.aperto.magkit.urimapping;

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
 * This might be a more readable alternative to the original regexp uri mapping. It uses git version numbering as default.
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

    public String getPattern() {
        return _pattern;
    }

    public void setPattern(String pattern) {
        _pattern = pattern;
        if (_pattern == null) {
            _pattern = GIT_PATTERN;
        }
        _regexp = Pattern.compile(_pattern);
    }

    public int getLevel() {
        return _level;
    }

    public void setLevel(int level) {
        _level = level;
    }

    public String getFromPrefix() {
        return _fromPrefix;
    }

    public void setFromPrefix(String fromPrefix) {
        _fromPrefix = removeEnd(fromPrefix, "/") + "/";
    }

    public String getToUri() {
        return _toUri;
    }

    public void setToUri(String toUri) {
        _toUri = toUri;
    }

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

    @Override
    public boolean isValid() {
        return isNotBlank(getFromPrefix()) && isNotBlank(getToUri());
    }

    private String buildToUri(final String rest) {
        String toUri;
        if (_toUri != null) {
            toUri = String.format(_toUri, rest);
        } else {
            toUri = String.format("%s%s", _fromPrefix, rest);
        }
        return toUri;
    }

    private boolean isValidVersion(String version) {
        return _regexp.matcher(version).matches();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[pattern=" + _pattern + ": " + _fromPrefix + " --> " + buildToUri("...") + "]";
    }
}
