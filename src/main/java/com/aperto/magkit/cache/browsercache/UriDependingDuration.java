package com.aperto.magkit.cache.browsercache;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.cache.BrowserCachePolicy;
import info.magnolia.module.cache.BrowserCachePolicyResult;
import info.magnolia.module.cache.CachePolicyResult;
import org.apache.commons.lang.ArrayUtils;

/**
 * Uses a specific expiration time (in minutes) for specific uris. Default to 30 minutes.
 *
 * @author diana.racho (05.01.2010)
 */
public class UriDependingDuration implements BrowserCachePolicy {

    private static final int MINUTE_IN_MILLIS = 60 * 1000;

    private long _expirationMinutes = 30;
    private UriDependingConfig[] _uriDependingConfigs = new UriDependingConfig[0];

    public void addConfig(UriDependingConfig config) {
        _uriDependingConfigs = (UriDependingConfig[]) ArrayUtils.add(_uriDependingConfigs, config);
    }

    public UriDependingConfig[] getConfigs() {
        return _uriDependingConfigs;
    }

    public void setExpirationMinutes(long expirationMinutes) {
        _expirationMinutes = expirationMinutes;
    }

    public long getExpirationMinutes() {
        long expirationMin = _expirationMinutes;
        if (_uriDependingConfigs.length != 0) {
            AggregationState aggregationState = MgnlContext.getAggregationState();
            final String uri = aggregationState.getOriginalURI();
            for (UriDependingConfig config : _uriDependingConfigs) {
                if (config.getVoters().vote(uri) <= 0) {
                    expirationMin = config.getExpirationMinutes();
                    break;
                }
            }
        }
        return expirationMin;
    }

    public BrowserCachePolicyResult canCacheOnClient(CachePolicyResult cachePolicyResult) {
        return new BrowserCachePolicyResult(System.currentTimeMillis() + getExpirationMinutes() * MINUTE_IN_MILLIS);
    }
}