package com.aperto.magkit.error;

/**
 * Error mapping for not found redirect handling.
 *
 * @author frank.sommer
 * @since 12.05.14
 */
public class ErrorMapping {
    protected static final String DEF_SITE = "default";

    private String _siteName = DEF_SITE;
    private String _language;
    private String _errorPath;

    public String getSiteName() {
        return _siteName;
    }

    public void setSiteName(final String siteName) {
        _siteName = siteName;
    }

    public String getLanguage() {
        return _language;
    }

    public void setLanguage(final String language) {
        _language = language;
    }

    public String getErrorPath() {
        return _errorPath;
    }

    public void setErrorPath(final String errorPath) {
        _errorPath = errorPath;
    }
}
