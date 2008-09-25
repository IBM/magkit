package com.aperto.magkit.taglib;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Resource;
import static org.apache.commons.lang.StringUtils.*;
import org.apache.commons.lang.exception.NestableRuntimeException;
import static org.apache.commons.lang.math.NumberUtils.toInt;
import org.apache.log4j.Logger;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
import static org.apache.taglibs.standard.tag.common.core.Util.escapeXml;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Draws a breadcrumbs with links to parents of a node if set or the current page.
 * Default output is: 
 * <code>
 * <ol>
 *  <li class="first"><a href="/layer_1.html">layer 1</a></li>
 *  <li><a href="/layer_1/layer_2.html">layer 2</a></li>
 * </ol>
 * </code>
 * @author frank.sommer (23.10.2007)
 */
@Tag(name = "breadcrumb", bodyContent = BodyContent.JSP)
public class BreadCrumbTag extends TagSupport {
    private static final Logger LOGGER = Logger.getLogger(BreadCrumbTag.class);

    /**
     * Breadcrumb start level.
     */
    private int _startLevel = 1;

    /**
     * Exclude current page from breadcrumb.
     */
    private boolean _excludeCurrent;

    /**
     * Output as _link. (default: true)
     */
    private boolean _link = true;

    /**
     * Output as _link. (default: true)
     */
    private boolean _lastlink = true;

    /**
     * Name for a page property which, if set, will make the page hidden in the breadcrumb.
     */
    private String _hideProperty;

    /**
     * Node to be used for resolving path.
     */
    private Content _node;

    /**
     * Flag to show breadcrumb as list (default: true).
     */
    private boolean _listStyle = true;

    /**
     * List type of breadcrumb (default: <code>ol</code>). Additional attributes like class are allowed.
     */
    private String _listType = "ol";

    /**
     * Additional separator between the breadcrumb items.
     */
    private String _separator;

    /**
     * An optional integer value to specify the number of the first element in the page hirachy to be included into the breadcrumb navigation.
     * Default is 1, the root node in content repository is 0.
     * @param startLevel breadcrumb start level. 
     */
    @TagAttribute
    public void setStartLevel(String startLevel) {
        _startLevel = toInt(startLevel, 1);
        if (_startLevel < 1) {
            _startLevel = 1;
        }
    }

    /**
     * An optional String value to give the name of a page property (Boolean).
     * The property value will be used to toggle on (true, default) or off (false) the visibility of any parent page in the breadcrumb bar.
     * @param hideProperty Name for a page property which, if set, will make the page hidden in the breadcrumb.
     */
    @TagAttribute
    public void setHideProperty(String hideProperty) {
        _hideProperty = hideProperty;
    }

    /**
     * An optional boolean value to toggle on (false, default) or off (true) the visibility of the curent page in the breadcrumb bar.
     * @param excludeCurrent if <code>true</code> the current (active) page is not included in breadcrumb.
     */
    @TagAttribute
    public void setExcludeCurrent(boolean excludeCurrent) {
        _excludeCurrent = excludeCurrent;
    }

    /**
     * An optional boolean value to toggle on (true, default) or off (false) the rendering of all page names as links.
     * @param link if <code>true</code> all pages are rendered as links.
     */
    @TagAttribute
    public void setLink(boolean link) {
        _link = link;
    }

    /**
     * An optional boolean value to toggle on (true, default) or off (false) the rendering of the last page names as link.
     * @param link if <code>true</code> the last showen breadcrumb entry will be rendered as link.
     */
    @TagAttribute
    public void setLastlink(boolean link) {
        _lastlink = link;
    }

    /**
     * An optional <code>info.magnolia.cms.core.Content</code> object.
     * If given, a breadcrumb navigation for this repository item will be computed.
     * @param node if set, used to resolve path instead of current page node.
     */
    @TagAttribute
    public void setNode(Content node) {
        _node = node;
    }

    /**
     * An optional boolean value for switching the list style. Default is true.
     */
    @TagAttribute
    public void setListStyle(boolean listStyle) {
        _listStyle = listStyle;
    }

    /**
     * An optional value for the list type. Ordered List is default. Class attributes are also possible.
     * If listStyle is false, listType will be used as delimiter.
     */
    @TagAttribute
    public void setListType(String listType) {
        _listType = listType;
    }

    /**
     * An optional value for the separator.
     * The seperator were rendered in the links.
     */
    @TagAttribute
    public void setSeparator(String separator) {
        _separator = separator;
    }

    /**
     * Renders the hirachy path to the actual repository element as ordered list as default. 
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        Content actpage = _node != null ? _node : Resource.getCurrentActivePage();
        try {
            int endLevel = actpage.getLevel();
            if (_excludeCurrent) {
                endLevel--;
            }
            JspWriter out = pageContext.getOut();
            if (_listStyle) {
                out.print("<" + _listType + ">");
            }
            out.print(iterateBreadcrumbContent(endLevel, actpage, request.getContextPath()));
            if (_listStyle) {
                out.print("</" + split(_listType, ' ')[0] + ">");
            }
        } catch (RepositoryException e) {
            LOGGER.warn("Exception caught: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new NestableRuntimeException(e);
        }
        return super.doStartTag();
    }

    private StringBuilder iterateBreadcrumbContent(int endLevel, Content breadcrumbContent, String contextPath) throws RepositoryException, IOException {
        StringBuilder out = new StringBuilder();
        boolean firstHidden = false;
        for (int j = _startLevel; j <= endLevel; j++) {
            Content page = breadcrumbContent.getAncestor(j);
            if (isNotEmpty(_hideProperty) && page.getNodeData(_hideProperty).getBoolean()) {
                firstHidden = true;
                continue;
            }
            if (_listStyle) {
                out.append("<li");
                if (j == _startLevel || firstHidden) {
                    out.append(" class=\"first\"");
                    firstHidden = false;
                }
                out.append(">");
            }
            out.append(renderHref(page, contextPath, j, endLevel));
            if (_listStyle) {
                out.append("</li>");
            } else if (j < endLevel) {
                out.append(_listType);
            }
        }
        return out;
    }

    private String renderHref(Content page, String contextPath, int j, int endLevel) {
        StringBuilder out = new StringBuilder();
        if (_link && (_lastlink || (j < endLevel))) {
            out.append("<a href=\"");
            out.append(contextPath);
            out.append(page.getHandle()).append('.').append(ServerConfiguration.getInstance().getDefaultExtension());
            out.append("\">");
        }
        String navTitle = getNavTitle(page);
        out.append(navTitle);
        if (isNotBlank(_separator) && (j < endLevel)) {
            out.append(_separator);
        }
        if (_link && (_lastlink || (j < endLevel))) {
            out.append("</a>");
        }
        return out.toString();
    }

    private String getNavTitle(Content page) {
        String navTitle = page.getNodeData("navTitle").getString().replaceAll("\\{-\\}", "");
        if (isBlank(navTitle)) {
            navTitle = page.getTitle();
            if (isBlank(navTitle)) {
                navTitle = page.getName();
            }
        }
        return escapeXml(navTitle);
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        _startLevel = 1;
        _excludeCurrent = false;
        _link = true;
        _lastlink = true;
        _hideProperty = null;
        _listType = null;
        _listStyle = true;
        _separator = null;
        super.release();
    }
}