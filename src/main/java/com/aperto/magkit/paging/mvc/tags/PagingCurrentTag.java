package com.aperto.magkit.paging.mvc.tags;

import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;

import javax.servlet.jsp.JspException;

/**
 * This tag must be nested whithin the pagingIterator tag
 * and should contain the HTML code for the current paging unit (e.g. not linked paging number) as body content.
 * Its body content will be evaluated only once for the actual paging unit, when <code> _currentPageIndex == _count</code>.
 *
 * @author wolf.bubenik (Aperto AG)
 * Date: 14.01.2009
 * Time: 13:41:58
 */
@Tag(name = "pagingCurrent", bodyContent = BodyContent.JSP)
public class PagingCurrentTag extends NestedPagingTag {

    private static final long serialVersionUID = 1L;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        return getIteratingAncestor().isRenderCurrentPage() ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }
}
