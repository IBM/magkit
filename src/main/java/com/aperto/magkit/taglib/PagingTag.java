package com.aperto.magkit.taglib;

import info.magnolia.cms.util.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import com.aperto.magkit.utils.ResourceUtils;

/**
 * Draws a paging div container.
 * Looks in the property files for naming the links, link titles and selector.
 * (common.paging.prefix, common.paging.prefixTitle, common.paging.selector,
 * common.paging.prevPageTitle, common.paging.prevPage, common.paging.nextPageTitle, common.paging.nextPage) 
 *
 * @author frank.sommer (15.11.2007)
 */
@Tag(name = "paging", bodyContent = BodyContent.JSP)
public class PagingTag extends TagSupport {
    private static final Logger LOGGER = Logger.getLogger(PagingTag.class);

    private String _prefix = "Seite";
    private String _prefixTitle = "zur Seite ";
    private String _selector = " ";
    private String _prevPageTitle = "zur vorherigen Seite";
    private String _prevPage = "zurück";
    private String _nextPageTitle = "zur nächsten Seite";
    private String _nextPage = "weiter";

    private int _pages;
    private int _actPage;
    private boolean _addQueryString = false;

    @TagAttribute(required = true)
    public void setPages(int pages) {
        _pages = pages;
    }

    @TagAttribute(required = true)
    public void setActPage(int actPage) {
        _actPage = actPage;
    }

    @TagAttribute
    public void setAddQueryString(String addQueryString) {
        _addQueryString = Boolean.valueOf(addQueryString);
    }

    /**
     * Produce the paging.
     *
     * @return jsp output
     * @throws JspException
     */
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();
        String completeHandle = getHandleFromActivePage();
        String queryString = "";
        init();
        if (_addQueryString) {
            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            queryString = "?" + request.getQueryString().replaceAll("&", "&amp;");
        }
        try {
            if (_pages > 1) {
                out.print("<div class=\"pager\">\n<ul>");

                if (_actPage > 1) {
                    out.print("<li class=\"previous\">");
                    out.print("<a href=\"" + completeHandle + "." + ResourceUtils.SELECTOR_PAGING_WITH_DELIMITER + (_actPage - 1) + ".html" + queryString + "\" title=\"" + _prevPageTitle + "\">");
                    out.print(_prevPage + "</a></li>");
                }

                out.print("<li><strong>" + _prefix + "</strong></li>");
                for (int i = 0; i < _pages; i++) {
                    int page = i + 1;

                    if (page == _actPage) {
                        out.print("<li class=\"aktiv\">" + page + "</li>");
                    } else {
                        out.print("<li><a href=\"" + completeHandle + "." + ResourceUtils.SELECTOR_PAGING_WITH_DELIMITER + page + ".html" + queryString + "\" title=\"" + _prefixTitle + page + "\" >");
                        out.print(page + "</a></li>");
                    }
                    if (page < _pages && StringUtils.isNotBlank(_selector)) {
                        out.print(" " + _selector + " ");
                    } else {
                        out.print(" ");
                    }
                }

                if (_actPage < _pages) {
                    out.print("<li class=\"next\">");
                    out.print("<a href=\"" + completeHandle + "." + ResourceUtils.SELECTOR_PAGING_WITH_DELIMITER + (_actPage + 1) + ".html" + queryString + "\" title=\"" + _nextPageTitle + "\">");
                    out.print(_nextPage + "</a></li>");
                }
                out.print("</ul>\n</div>");
            }
        } catch (IOException e) {
            throw new NestableRuntimeException(e);
        }
        return super.doEndTag();
    }

    protected String getHandleFromActivePage() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String handle = request.getContextPath();
        handle += Resource.getCurrentActivePage().getHandle();
        String selector = Resource.getSelector();
        if (!StringUtils.isBlank(selector)) {
            String[] strings = StringUtils.split(selector, '.');
            for (String s : strings) {
                if (!s.startsWith(ResourceUtils.SELECTOR_PAGING_WITH_DELIMITER)) {
                    handle += "." + s;
                }
            }
        }
        return handle;
    }

    private void init() {
        try {
            _prefix = ResourceBundle.getBundle("language").getString("common.paging.prefix");
            _prefixTitle = ResourceBundle.getBundle("language").getString("common.paging.prefixTitle");
            _selector = ResourceBundle.getBundle("language").getString("common.paging.selector");
            _prevPageTitle = ResourceBundle.getBundle("language").getString("common.paging.prevPageTitle");
            _prevPage = ResourceBundle.getBundle("language").getString("common.paging.prevPage");
            _nextPageTitle = ResourceBundle.getBundle("language").getString("common.paging.nextPageTitle");
            _nextPage = ResourceBundle.getBundle("language").getString("common.paging.nextPage");
        } catch (MissingResourceException mre) {
            LOGGER.info("Can not find resource key. Using default value.");
        }
    }
}
