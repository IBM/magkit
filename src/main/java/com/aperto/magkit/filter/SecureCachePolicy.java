package com.aperto.magkit.filter;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.module.cache.cachepolicy.Default;

/**
 * Extends the default cache policy by varying between http and https.
 *
 * @author frank.sommer
 * @since 17.08.2012
 */
public class SecureCachePolicy extends Default {

    /**
     * Builds an extended cache key. In case of https '_sec' is appended to cache key. 
     */
    @Override
    public Object retrieveCacheKey(final AggregationState aggregationState) {
        String secSuffix = aggregationState.getOriginalURL().startsWith("https") ? "_sec" : "";
        return aggregationState.getOriginalURI() + secSuffix;
    }
}