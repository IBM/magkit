package com.aperto.magkit.error;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration of the 404 not found redirects.
 *
 * @author frank.sommer
 * @since 12.05.14
 */
public class NotFoundConfig {
    private String _default = "/en/toolbox/404";
    private List<ErrorMapping> _errorMappings = new ArrayList<ErrorMapping>();


    public String getDefault() {
        return _default;
    }

    public void setDefault(final String defaultPath) {
        _default = defaultPath;
    }

    public List<ErrorMapping> getErrorMappings() {
        return _errorMappings;
    }

    public void setErrorMappings(final List<ErrorMapping> errorMappings) {
        _errorMappings = errorMappings;
    }
}
