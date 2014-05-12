package com.aperto.magkit.module;

import com.aperto.magkit.error.NotFoundConfig;

/**
 * Module class of the Magkit module.
 *
 * @author frank.sommer
 * @since 12.05.14
 */
public class MagkitModule {

    private NotFoundConfig _notFoundConfig;

    public NotFoundConfig getNotFoundConfig() {
        return _notFoundConfig;
    }

    public void setNotFoundConfig(final NotFoundConfig notFoundConfig) {
        _notFoundConfig = notFoundConfig;
    }
}
