package com.aperto.magkit.urimapping;

import info.magnolia.cms.beans.config.QueryAwareVirtualURIMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.removeEnd;

/**
 * This might be a more readable alternative to the original regexp uri mapping. It uses git version numbering as default.
 *
 * @author daniel.kasmeroglu@aperto.de
 */
public class VersionNumberVirtualUriMapping implements QueryAwareVirtualURIMapping {
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

    //CHECKSTYLE:OFF
    @Override
    public MappingResult mapURI(String uri) {
        return mapURI(uri, null);
        //CHECKSTYLE:ON
    }

    //CHECKSTYLE:OFF
    @Override
    public MappingResult mapURI(String uri, String queryString) {
        //CHECKSTYLE:ON
        MappingResult result = null;
        if (_fromPrefix == null) {
            LOGGER.warn("Incomplete configuration. fromPrefix is not set.");
        } else if (uri.startsWith(_fromPrefix)) {
            // we gotta prefix, so check if we've got a version candidate
            String suffix = uri.substring(_fromPrefix.length());
            int idx = suffix.indexOf('/');
            if (idx > 0) {
                // we gotta version candidate, so check if it's supported by us
                String versionPart = suffix.substring(0, idx);
                if (isValidVersion(versionPart)) {
                    // we support this version pattern, so it can be dropped
                    result = newResult();
                    String rest = suffix.substring(idx + 1);
                    String toUri = buildToUri(rest, queryString);
                    result.setToURI(toUri);
                }
            }
        }
        return result;
    }

    private String buildToUri(final String rest, final String queryString) {
        String toUri;
        if (_toUri != null) {
            toUri = String.format(_toUri, rest);
        } else {
            toUri = String.format("%s%s", _fromPrefix, rest);
        }
        if (queryString != null) {
            toUri += "?" + queryString;
        }
        return toUri;
    }

    private boolean isValidVersion(String version) {
        return _regexp.matcher(version).matches();
    }

    private MappingResult newResult() {
        MappingResult result = new MappingResult();
        result.setLevel(_level);
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[pattern=" + _pattern + ": " + _fromPrefix + " --> " + buildToUri("...", null) + "]";
    }

}
