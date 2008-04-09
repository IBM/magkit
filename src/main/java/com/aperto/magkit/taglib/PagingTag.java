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

/**
 * Draws a paging div container.
 * @author frank.sommer (15.11.2007)
 */
@Tag(name = "paging", bodyContent = BodyContent.JSP)
public class PagingTag extends TagSupport {
    private static final Logger LOGGER = Logger.getLogger(PagingTag.class);

    private int _pages;
    private int _actPage;

    @TagAttribute(required = true)
    public void setPages(int pages) {
        _pages = pages;
    }

    @TagAttribute(required = true)
    public void setActPage(int actPage) {
        _actPage = actPage;
    }

    /**
     * Produce the paging.
     * @return jsp output
     * @throws JspException
     */
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();
        String completeHandle = getHandleFromActivePage();
        String prefix = "Seite ";
        String selector = "|";
        String aureal = "Sie sind auf Seite: ";
        try {
            prefix = ResourceBundle.getBundle("language").getString("common.paging.prefix");
            selector = ResourceBundle.getBundle("language").getString("common.paging.selector");
            aureal = ResourceBundle.getBundle("language").getString("common.paging.aureal");
        } catch (MissingResourceException mre) {
            LOGGER.info("Can not find resource key. Using default value.");
        }
        try {
            if (_pages > 1) {
                out.print("<div class=\"paging\">\n<div>");
                out.print("<h3>" + prefix + "</h3>");
                out.print("<p class=\"nav-index\">");
                for (int i = 0; i < _pages; i++) {
                    int page = i + 1;
                    if (page == _actPage) {
                        out.print("<strong><span class=\"aural\">" + aureal + "</span> " + page + "</strong>");
                    } else {
                        out.print("<a href=\"" + completeHandle + ".pid-" + page + ".html\" title=\"Seite " + page + "\" >");
                        out.print(page);
                        out.print("</a>");
                    }
                    if (page < _pages) {
                        out.print(" " + selector + " ");
                    }
                }
                out.print("</p>");
                out.print("</div>\n</div>");
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
                if (!s.startsWith("pid-")) {
                    handle += "." + s;
                }
            }
        }
        return handle;
    }
}
