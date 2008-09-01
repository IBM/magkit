package com.aperto.magkit.beans;

import com.aperto.magkit.controller.SearchController;
import java.util.List;

/**
 * Command object for a search.
 *
 * @author frank.sommer (22.05.2008)
 */
public class Search {
    private String _q;
    private List _hits;
    private int _numberOfHits;
    private int _actPage;
    private int _entriesPerPage;

    //CHECKSTYLE:OFF
    public String getQ() {
        return _q;
    }

    public void setQ(String q) {
        _q = q;
    }
    //CHECKSTYLE:ON

    public void setNumberOfHits(int numberOfHits) {
        _numberOfHits = numberOfHits;
    }

    public int getNumberOfHits() {
        return _numberOfHits;
    }

    public List getHits() {
        return _hits;
    }

    public void setHits(List hits) {
        _hits = hits;
    }

    public int getPages() {
        return ((_numberOfHits - 1) / _entriesPerPage) + 1;
    }

    public int getActPage() {
        return Math.min(_actPage, getPages());
    }

    public void setActPage(int actPage) {
        _actPage = actPage;
    }

    public int getEntriesPerPage() {
        return _entriesPerPage;
    }

    public void setEntriesPerPage(int entriesPerPage) {
        _entriesPerPage = entriesPerPage;
    }

    /**
     * constructor.
     */
    public Search() {
        _q = "";
        _hits = null;
        _actPage = 1;
        _numberOfHits = 0;
        _entriesPerPage = SearchController.DEFAULT_ENTRIES_PER_PAGE;
    }
}
