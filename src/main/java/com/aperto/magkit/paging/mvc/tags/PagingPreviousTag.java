package com.aperto.magkit.paging.mvc.tags;

import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;

import javax.servlet.jsp.JspException;

import com.aperto.magkit.paging.mvc.tags.NestedPagingTag;

/**
 * This tag must be nested whithin the pagingIterator tag
 * and should contain the HTML code for the next page link as body content.
 *
 * @author wolf.bubenik (Aperto AG)
 * Date: 14.01.2009
 * Time: 13:41:58
 */
@Tag(name = "pagingPrevious", bodyContent = BodyContent.JSP)
public class PagingPreviousTag extends NestedPagingTag {

    private static final long serialVersionUID = 1L;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        return getIteratingAncestor().isRenderBack() ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }
}
