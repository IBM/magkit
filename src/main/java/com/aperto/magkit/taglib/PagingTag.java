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
    private static final String PADDING_SEQUENZ = "...";
    
    private String _prefix = "Seite";
    private String _prefixTitle = "zur Seite ";
    private String _selector = " ";
    private String _prevPageTitle = "zur vorherigen Seite";
    private String _prevPage = "zurück";
    private String _nextPageTitle = "zur nächsten Seite";
    private String _nextPage = "weiter";

    private int _pages;
    private int _actPage;
    private int _linkedPages = 5;
    private boolean _addQueryString = false;

    @TagAttribute
    public void setLinkedPages(int linkedPages) {
        _linkedPages = linkedPages;
    }

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
                out.print(determinePrevious(completeHandle, queryString));
                out.print("<li><strong>" + _prefix + "</strong></li>");
                int startPage = 1;
                if (_pages > _linkedPages && _actPage > ((_linkedPages / 2) + 1)) {
                    out.print("<li>" + PADDING_SEQUENZ + "</li>");
                    startPage = _actPage - (_linkedPages / 2);
                }
                int lastPage = Math.min(startPage + _linkedPages - 1, _pages);
                for (int page = startPage; page <= lastPage; page++) {
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
                if (lastPage < _pages) {
                    out.print("<li>" + PADDING_SEQUENZ + "</li>");    
                }
                out.print(determineNext(completeHandle, queryString));
                out.print("</ul>\n</div>");
            }
        } catch (IOException e) {
            throw new NestableRuntimeException(e);
        }
        return super.doEndTag();
    }

    private String determineNext(String completeHandle, String queryString) throws IOException {
        StringBuffer out = new StringBuffer();
        if (_actPage < _pages) {
            out.append("<li class=\"next\">");
            out.append("<a href=\"").append(completeHandle).append("." + ResourceUtils.SELECTOR_PAGING_WITH_DELIMITER).append(_actPage + 1).append(".html").append(queryString).append("\" title=\"").append(_nextPageTitle).append("\">");
            out.append(_nextPage).append("</a></li>");
        }
        return out.toString();
    }

    private String determinePrevious(String completeHandle, String queryString) throws IOException {
        StringBuffer out = new StringBuffer();
        if (_actPage > 1) {
            out.append("<li class=\"previous\">");
            out.append("<a href=\"").append(completeHandle).append("." + ResourceUtils.SELECTOR_PAGING_WITH_DELIMITER).append(_actPage - 1).append(".html").append(queryString).append("\" title=\"").append(_prevPageTitle).append("\">");
            out.append(_prevPage).append("</a></li>");
        }
        return out.toString();
    }

    private String getHandleFromActivePage() {
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

    /**
     * Overriden method.
     */
    @Override
    public void release() {
        super.release();
        _actPage = 1;
        _pages = 1;
        _linkedPages = 5;
        _addQueryString = false;
    }
}
