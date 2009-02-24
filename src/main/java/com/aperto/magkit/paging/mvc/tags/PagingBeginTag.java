package com.aperto.magkit.paging.mvc.tags;

import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;

import javax.servlet.jsp.JspException;


/**
 * This tag must be nested whithin the pagingIterator tag
 * and should contain the HTML code for the beginning of the paging section as body content.
 * Its body content will be evaluated only once at the beginning of the iteration of the collection when <code>count == 0</code>. 
 *
 * @author wolf.bubenik (Aperto AG)
 * Date: 14.01.2009
 */
@Tag(name = "pagingBegin", bodyContent = BodyContent.JSP)
public class PagingBeginTag extends NestedPagingTag {

    private static final long serialVersionUID = 1L;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        return getIteratingAncestor().isPagingBeginn() ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }
}
