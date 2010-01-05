package com.aperto.magkit.cache.browsercache;

import info.magnolia.voting.voters.VoterSet;

/**
 * Configuration of an specific expiration time for specific uris.
 *
 * @author diana.racho (05.01.2010)
 */
public class UriDependingConfig {
    private int _expirationMinutes = 30;
    private VoterSet _voters;

    public int getExpirationMinutes() {
        return _expirationMinutes;
    }

    public void setExpirationMinutes(int expirationMinutes) {
        _expirationMinutes = expirationMinutes;
    }

    public VoterSet getVoters() {
        return _voters;
    }

    public void setVoters(VoterSet voters) {
        _voters = voters;
    }
}