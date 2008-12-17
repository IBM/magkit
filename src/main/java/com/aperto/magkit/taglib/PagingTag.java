package com.aperto.magkit.taglib;

import com.aperto.magkit.utils.ResourceUtils;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.util.Resource;
import static org.apache.commons.lang.StringUtils.*;
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
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
    private String _prevPageTitle = "zur vorherigen Seite";
    private String _prevPage = "zurück";
    private String _nextPageTitle = "zur nächsten Seite";
    private String _nextPage = "weiter";
    private String _actPageTitle = "Sie sind hier : ";

    private int _pages;
    private int _actPage;
    private int _linkedPages = 5;
    private boolean _addQueryString = false;
    private boolean _showPrefix = true;
    private boolean _showTitle = true;
    private boolean _encapsulate = true;
    private String _includeHeadline = "";
    private String _activeClass = "";

    /**
     * set linked page.
     *
     * @param linkedPages linked page
     */
    @TagAttribute
    public void setLinkedPages(int linkedPages) {
        _linkedPages = linkedPages;
    }

    /**
     * set count of pages.
     *
     * @param pages count of pages
     */
    @TagAttribute(required = true)
    public void setPages(int pages) {
        _pages = pages;
    }

    /**
     * set count of active page.
     *
     * @param actPage count of active page
     */
    @TagAttribute(required = true)
    public void setActPage(int actPage) {
        _actPage = actPage;
    }

    /**
     * set add query string.
     *
     * @param addQueryString add query string
     */
    @TagAttribute
    public void setAddQueryString(String addQueryString) {
        _addQueryString = Boolean.valueOf(addQueryString);
    }

    /**
     * set show prefix.
     *
     * @param showPrefix show prefix
     */
    @TagAttribute
    public void setShowPrefix(String showPrefix) {
        _showPrefix = Boolean.valueOf(showPrefix);
    }

    /**
     * set show title.
     *
     * @param showTitle show title
     */
    @TagAttribute
    public void setShowTitle(String showTitle) {
        _showTitle = Boolean.valueOf(showTitle);
    }

    /**
     * Set encapsulate. If encapsulate the paging list is encapsulated in a div container.
     */
    @TagAttribute
    public void setEncapsulate(String encapsulate) {
        _encapsulate = Boolean.valueOf(encapsulate);
    }

    /**
     * set headline.
     *
     * @param headline - headline
     */
    @TagAttribute
    public void setIncludeHeadline(String headline) {
        _includeHeadline = headline;
    }

    /**
     * Set active class. default is empty.
     * @param activeClass - class of active li
     */
    @TagAttribute
    public void setActiveClass(String activeClass) {
        _activeClass = activeClass;
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
                if (_encapsulate) {
                    out.print("<div class=\"pager\">\n");
                }
                if (isNotEmpty(_includeHeadline)) {
                    out.print("<h4>" + _includeHeadline + "</h4>\n");
                }
                out.print("<ul");
                if (!_encapsulate) {
                    out.print(" class=\"pager\"");
                }
                out.print(">\n");
                out.print(determinePrevious(getLink(completeHandle, queryString, _actPage - 1)));
                if (_showPrefix) {
                    out.print("<li><strong>" + _prefix + "</strong></li>");
                }
                printListItems(out, completeHandle, queryString);
                out.print(determineNext(getLink(completeHandle, queryString, _actPage + 1)));
                out.print("</ul>\n");
                if (_encapsulate) {
                    out.print("</div>");
                }
            }
        } catch (IOException e) {
            throw new NestableRuntimeException(e);
        }
        return super.doEndTag();
    }

    private void printListItems(JspWriter out, String completeHandle, String queryString) throws IOException {
        int startPage = 1;
        if (_pages > _linkedPages && _actPage > ((_linkedPages / 2) + 1)) {
            out.print(determineLinkedPage(getLink(completeHandle, queryString, 1), 1));
            out.print("<li>" + PADDING_SEQUENZ + "</li>");
            startPage = Math.min(_actPage - (_linkedPages / 2), _pages - _linkedPages + 1);
        }
        int lastPage = Math.min(startPage + _linkedPages - 1, _pages);
        for (int page = startPage; page <= lastPage; page++) {
            if (page == _actPage) {
                out.print("<li");
                if (isNotBlank(_activeClass)) {
                    out.print(" class=\"" + _activeClass + "\"");
                }
                out.print("><em>" + _actPageTitle + "</em><strong>" + page + "</strong></li>");
            } else {
                out.print(determineLinkedPage(getLink(completeHandle, queryString, page), page));
            }
        }
        if (lastPage < _pages) {
            out.print("<li>" + PADDING_SEQUENZ + "</li>");
            out.print(determineLinkedPage(getLink(completeHandle, queryString, _pages), _pages));
        }
    }

    /**
     * returns <li>tag for given page.
     *
     * @param link - handle for link
     * @param page - count of page
     * @return <li>tag
     * @throws IOException
     */
    public String determineLinkedPage(String link, int page) throws IOException {
        StringBuffer out = new StringBuffer();
        //out.append("<li><a href=\"").append(completeHandle).append("." + ResourceUtils.SELECTOR_PAGING_WITH_DELIMITER).append(page).append(".html").append(queryString);
        out.append("<li><a href=\"").append(link);
        if (_showTitle) {
            out.append("\" title=\"").append(_prefixTitle).append(page);
        }
        out.append("\" >");
        out.append(page).append("</a></li>");
        return out.toString();
    }

    /**
     * returns <li>tag for next button.
     *
     * @param link - handle for link
     * @return <li>tag
     * @throws IOException
     */
    public String determineNext(String link) throws IOException {
        StringBuffer out = new StringBuffer();
        if (_actPage < _pages) {
            out.append("<li class=\"next\">");
            //out.append("<a href=\"").append(completeHandle).append("." + ResourceUtils.SELECTOR_PAGING_WITH_DELIMITER).append(_actPage + 1).append(".html").append(queryString);
            out.append("<a href=\"").append(link);
            if (_showTitle) {
                out.append("\" title=\"").append(_nextPageTitle);
            }
            out.append("\">");
            out.append(_nextPage).append("</a></li>");
        }
        return out.toString();
    }

    /**
     * returns <li>tag for previous button.
     *
     * @param link - handle for link
     * @return <li>tag
     * @throws IOException
     */
    public String determinePrevious(String link) throws IOException {
        StringBuffer out = new StringBuffer();
        if (_actPage > 1) {
            out.append("<li class=\"previous\">");
            //out.append("<a href=\"").append(completeHandle).append("." + ResourceUtils.SELECTOR_PAGING_WITH_DELIMITER).append(_actPage - 1).append(".html").append(queryString);
            out.append("<a href=\"").append(link);
            if (_showTitle) {
                out.append("\" title=\"").append(_prevPageTitle);
            }
            out.append("\">");
            out.append(_prevPage).append("</a></li>");
        }
        return out.toString();
    }

    /**
     * returns link for given page.
     *
     * @param completeHandle - handle of active page
     * @param queryString    - query string
     * @param page           - count of page
     * @return link
     */
    public String getLink(String completeHandle, String queryString, int page) {
        StringBuffer out = new StringBuffer();
        out.append(completeHandle).append("." + ResourceUtils.SELECTOR_PAGING_WITH_DELIMITER).append(page).append(".html").append(queryString);
        return out.toString();
    }

    /**
     * returns handle of active page.
     *
     * @return handle of active page
     */
    public String getHandleFromActivePage() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String handle = request.getContextPath();
        handle += Resource.getCurrentActivePage().getHandle();
        String selector = Resource.getSelector();
        if (!isBlank(selector)) {
            String[] strings = split(selector, '.');
            for (String s : strings) {
                if (!s.startsWith(ResourceUtils.SELECTOR_PAGING_WITH_DELIMITER)) {
                    handle += "." + s;
                }
            }
        }
        return handle;
    }

    /**
     * init parameters.
     */
    public void init() {
        try {
            Locale locale;
            I18nContentSupport i18nSupport = I18nContentSupportFactory.getI18nSupport();
            if (i18nSupport != null) {
                locale = i18nSupport.getLocale();
            } else {
                locale = Locale.getDefault();
            }
            ResourceBundle resourceBundle = ResourceBundle.getBundle("language", locale);
            _prevPage = resourceBundle.getString("common.paging.prevPage");
            _nextPage = resourceBundle.getString("common.paging.nextPage");
            _actPageTitle = resourceBundle.getString("common.paging.actPageTitle");
            if (_showPrefix) {
                _prefix = resourceBundle.getString("common.paging.prefix");
            }
            if (_showTitle) {
                _prefixTitle = resourceBundle.getString("common.paging.prefixTitle");
                _prevPageTitle = resourceBundle.getString("common.paging.prevPageTitle");
                _nextPageTitle = resourceBundle.getString("common.paging.nextPageTitle");
            }            
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
        _showPrefix = true;
        _showTitle = true;
        _encapsulate = true;
        _includeHeadline = "";
        _activeClass = "";
    }
}