package com.aperto.magkit.paging.mvc.tags;

import info.magnolia.cms.util.SelectorUtil;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import org.apache.myfaces.tobago.apt.annotation.*;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Collections;
import java.util.List;

/**
 * This tag itterates over a collection of objects and provides status messages to controll the body evaluation of its nested tags.
 * Note, that the body will be evaluated bevore iteration, for each iteration step (paging unit) and after iteration.
 * All HTML content should only be provided as body content of the corresponding nested tags.
 * 
 * @author wolf.bubenik (Aperto AG)
 * @since 13.01.2009
 */
@Tag(name = "pagingIterator", bodyContent = BodyContent.JSP)
public class PagingIteratorTag extends TagSupport {

    private static final long serialVersionUID = 1L;

    // Tag attributes
    private List<?> _items = Collections.emptyList();
    private int _pageSize = 10;
    private int _windowSize = 2;
    private int _firstPages = 1;
    private int _lastPages = 1;
    private String _currentIndexKey = "PagingIteratorTag.currentIndexKey";
    private String _currentPageUnitItemsKey = "PagingIteratorTag.currentPageUnitItemsKey";
    private String _varStatus = "PagingIteratorTag.status";
    private int _scope = PageContext.REQUEST_SCOPE;

    // set via request:
    // 1 based index of current paging unit
    private int _currentPageIndex = 1;

    // standard loop status:
    // 0 based index of last item in Collection of paging unit whithin loop.
    private int _index = -1;
    // 1 based number of paging units
    private int _size = 0;
    // 1 based index of paging unit whithin loop
    private int _count = 0;
    private PagingLoopTagStatus _status;

    /**
     * The List of objects to create a paging navigation for. NULL will be ignored.
     * @param items a List<? extends Object>
     */
    @TagAttribute(required = true)
    public void setItems(List<?> items) {
        if (items != null) {
            _items = items;
        }
    }

    /**
     * The number of collection items per display page.
     * Default is  10.
     * @param pageSize the page size as int
     */
    @TagAttribute
    public void setPageSize(int pageSize) {
        _pageSize = pageSize < 1 ? 1 : pageSize;
    }

    /**
     * The number of pages to show before and after the current page.
     * Default is 2.
     * @param windowSize an integer denoting the number of preceeding and following pages.
     */
    @TagAttribute
    public void setWindowSize(int windowSize) {
        _windowSize = windowSize < 0 ? 0 : windowSize;
    }

    /**
     * The number of display pages that are allways shown at the beginning of the paging navigation.
     * Default is 1.
     * @param firstPages an integer
     */
    @TagAttribute
    public void setFirstPages(int firstPages) {
        _firstPages = firstPages < 0 ? 0 : firstPages;
    }

    /**
     * The number of display pages that are allways shown at the end of the paging navigation.
     * Default is 1.
     * @param lastPages an integer
     */
    @TagAttribute
    public void setLastPages(int lastPages) {
        _lastPages = lastPages < 0 ? 0 : lastPages;
    }


    String getCurrentIndexKey() {
        return _currentIndexKey;
    }

    /**
     * The key to use for reading the current paging unit index from the PageContext.
     * Default is "PagingIteratorTag.currentIndexKey".
     * @param currentIndexKey an String
     */
    @TagAttribute
    public void setCurrentIndexKey(String currentIndexKey) {
        _currentIndexKey = currentIndexKey;
    }

    /**
     * The key to use for reading the current paging unit items from the PageContext.
     * Default is "PagingIteratorTag.currentPageUnitItemsKey".
     * @param currentPageUnitItemsKey an String
     */
    @TagAttribute
    public void setCurrentPageUnitItemsKey(String currentPageUnitItemsKey) {
        _currentPageUnitItemsKey = currentPageUnitItemsKey;
    }

    /**
     * The key to use for reading the current LoopStatus object from the PageContext.
     * Default is "PagingIteratorTag.status".
     * @param varStatus an String
     */
    @TagAttribute
    public void setVarStatus(String varStatus) {
        _varStatus = varStatus;
    }

    /**
     * The scope used for writing the list of current page unit items to the page context.
     * Default is REQUEST_SCOPE (2). Supported values are:
     * <ul>
     *  <li>APPLICATION_SCOPE: 4</li>
     *  <li>SESSION_SCOPE: 3</li>
     *  <li>REQUEST_SCOPE: 2</li>
     *  <li>PAGE_SCOPE: 1</li>
     * </ul>
     * Note, that for usage of this tag in freemarker templates the PAGE_SCOPE must be used.
     * @param scope an integer for the pageContext attribute scope
     */
    @TagAttribute
    public void setScope(int scope) {
        if (scope < 1 || scope > 4) {
            throw new IllegalArgumentException("Invalid value for scope: " + scope);
        }
        _scope = scope;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        int result = EVAL_BODY_INCLUDE;
        if (_items.isEmpty()) {
            result = SKIP_BODY;
        } else {
            initSettings();
        }
        pageContext.setAttribute(_varStatus, getLoopStatus());
        return result;
    }

    private void initSettings() {
        _size = _items.size() / _pageSize;
        if ((_items.size() % _pageSize) > 0) {
            _size++;
        }
        _currentPageIndex = getCurrentPageIndex();
        _count = 0;
        _index = -1;
    }

    private int getCurrentPageIndex() {
        // get page index from requestParameter
        int cursorPosition = getCursorFromRequest();
        // get page index from url selector
        if (cursorPosition < 1) {
            cursorPosition = getCursorFromUrlSelector();
        }
        if (cursorPosition > _size) {
            cursorPosition = _size;
        } else if (cursorPosition < 1) {
            cursorPosition = 1;
        }
        return cursorPosition;
    }

    /**
     *
     * @return 1 (first page) if selector does not exist - page called for first time
     */
    private int getCursorFromUrlSelector() {
        int result = 1;
        String selector = SelectorUtil.getSelector();
        if (isNotEmpty(selector)) {
            result = Integer.parseInt(selector);
        }
        return result;
    }

    /**
     *
     * @return 0 (undefined) if request attribute is not set - try to get value form url selector
     */
    private int getCursorFromRequest() {
        int result = 0;
        Object indexObject = pageContext.getRequest().getAttribute(_currentIndexKey);
        if (indexObject != null) {
            result = Integer.parseInt(indexObject.toString());
        }
        return result;
    }

    /**
     * @return int EVAL_BODY_AGAIN or SKIP_BODY after last element.
     */
    public int doAfterBody() {
        return doIteration() ? EVAL_BODY_AGAIN : SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() throws JspException {
        pageContext.setAttribute(_currentPageUnitItemsKey, getCurrentPageUnitItems(), _scope);
        return super.doEndTag();
    }

    List<?> getCurrentPageUnitItems() {
        int beginn = (_currentPageIndex - 1) * _pageSize;
        int end = beginn + _pageSize;
        if (end > _items.size()) {
            end = _items.size();
        }
        return _items.subList(beginn, end);
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void reset() {
        _items = Collections.emptyList();
        _pageSize = 10;
        _currentPageIndex = 1;
        _currentIndexKey = "PagingIteratorTag.currentIndexKey";
        _windowSize = 2;
        _firstPages = 1;
        _lastPages = 1;
        _status = null;
        _count = 0;
        _index = -1;
        _scope = PageContext.REQUEST_SCOPE;
    }

    /**
     * @return true if next value exists or , false otherwise.
     */
    protected boolean doIteration() {
        _count++;
        _index += _pageSize;
        //Todo: Optimise: call doIteration again as long as no tag content should be rendered. Usefull for skipped pages and very lage lists.
        // note that we are 'overstepping' the paging list to allow rendering of paging end items (closing of html elements) whithin the iterator tag.
        return _count <= _size + 1;
    }

    PagingLoopTagStatus getLoopStatus() {
        /**
         * An implementation of the interface javax.servlet.jsp.jstl.core.LoopTagStatus
         */
        class Status implements PagingLoopTagStatus {
            /**
             * Stable serialVersionUID.
             */
            private static final long serialVersionUID = 222L;
            public Object getCurrent() {
                return null;
            }
            public int getIndex() {
                return _index;
            }
            public int getCount() {
                return _count;
            }
            public boolean isFirst() {
                return _count == 1;
            }
            public boolean isLast() {
                return _count == _size;
            }
            public Integer getBegin() {
                return 0;
            }
            public Integer getEnd() {
                return _size;
            }
            public Integer getStep() {
                return _pageSize;
            }
            public boolean isBegin() {
                return _count == 0;
            }
            public int getCurrentPageIndex() {
                return _currentPageIndex;
            }
        }
        if (_status == null) {
            _status = new Status();
        }
        return _status;
    }


    boolean isRenderCurrentPage() {
        return _currentPageIndex == _count;
    }

    boolean isRenderSkippedPages() {
        return isSkipPagesAfterWindow() || isSkipPagesBeforeWindow();
    }

    boolean isRenderStep() {
        return  !isPagingBeginn() && !isPagingEnd() && !isRenderCurrentPage() && isVisiblePagingUnit();
    }

    boolean isPagingBeginn() {
        return _count == 0;
    }

    boolean isPagingEnd() {
        return _count > _size;
    }

    boolean isRenderBack() {
        return _currentPageIndex > 1;
    }

    boolean isRenderNext() {
        return _currentPageIndex < _size;
    }

    boolean isSkipPagesBeforeWindow() {
        return hasSkippedPagesBeforeWindow() && _count == _currentPageIndex - _windowSize - 1;
    }

    boolean isSkipPagesAfterWindow() {
        return hasSkippedPagesAfterWindow() && _count == _currentPageIndex + _windowSize + 1;
    }

    boolean isFirstPages() {
        return _count <= _firstPages;
    }

    boolean isLastPages() {
        return _count > _size - _lastPages;
    }

    boolean isWindowPage() {
        return _count >= _currentPageIndex - _windowSize && _count <= _currentPageIndex + _windowSize;
    }

    boolean hasSkippedPagesBeforeWindow() {
        return _currentPageIndex - _windowSize - _firstPages > 1;
    }

    boolean hasSkippedPagesAfterWindow() {
        return _currentPageIndex + _windowSize + _lastPages < _size;
    }

    boolean isVisiblePagingUnit() {
        return isFirstPages() || isLastPages() || isWindowPage();
    }
}