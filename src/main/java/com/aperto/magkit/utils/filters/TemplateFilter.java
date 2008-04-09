package com.aperto.magkit.utils.filters;

import org.apache.log4j.Logger;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Rule;

/**
 * Template filter.
 *
 * @author frank.sommer (13.11.2007)
 */
public class TemplateFilter implements Content.ContentFilter {
    private static final Logger LOGGER = Logger.getLogger(TemplateFilter.class);

    /**
     * Rule on which this filter works.
     */
    private Rule _rule;

    /**
     * Konstruktor.
     * @param rule of magnolia
     */
    public TemplateFilter(Rule rule) {
        _rule = rule;
    }

    /**
     * Test if this content should be included in a resultant collection.
     * @param content node for filtering
     * @return if true this will be a part of collection
     */
    public boolean accept(Content content) {
        String template = content.getTemplate();
        return _rule.isAllowed(template);
    }
}
