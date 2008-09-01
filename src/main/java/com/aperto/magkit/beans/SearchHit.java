package com.aperto.magkit.beans;

import org.apache.log4j.Logger;

/**
 * Bean for a search hit.
 *
 * @author frank.sommer (22.05.2008)
 */
public class SearchHit {
    private static final Logger LOGGER = Logger.getLogger(SearchHit.class);

    private String _title;
    private String _abstract;
    private String _handle;

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }

    public String getAbstract() {
        return _abstract;
    }

    public void setAbstract(String anAbstract) {
        _abstract = anAbstract;
    }

    public String getHandle() {
        return _handle;
    }

    public void setHandle(String handle) {
        _handle = handle;
    }
}
