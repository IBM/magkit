package com.aperto.magkit.paging.mvc.tags;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;

/**
 * @author wolf.bubenik (Aperto AG)
 * Date: 14.01.2009
 * Time: 13:58:51
 */
public abstract class NestedPagingTag extends TagSupport {

    private static final long serialVersionUID = 1L;

    protected PagingIteratorTag getIteratingAncestor() throws JspException {
        javax.servlet.jsp.tagext.Tag result = getParent();
        if (result == null || !(result instanceof PagingIteratorTag)) {
            throw new JspException("Invalid nesting of Tags: " + getClass().getName() + "must be nested into a PagingIteratorTag.");
        }
        return (PagingIteratorTag) result;
    }
}
