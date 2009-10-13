package com.aperto.magkit.controller;

import com.aperto.magkit.beans.SearchHit;
import com.aperto.magkit.beans.Search;
import com.aperto.magkit.utils.SelectorUtils;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.util.Resource;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import static org.apache.commons.lang.ArrayUtils.*;
import static org.apache.commons.lang.StringUtils.*;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Controller for executing the search.
 * Supports paging, different query modes, ordering by jcr:score in sql mode and build snippets of defined node datas.
 * <code>
 *  &lt;bean id="searchController" class="com.aperto.degewo.mvc.controller.SearchController"&gt;
 *       &lt;property name="commandName" value="search"/&gt;
 *       &lt;property name="commandClass" value="com.aperto.magkit.beans.Search"/&gt;
 *       &lt;property name="successView" value="pages/searchResult"/&gt;
 *       &lt;property name="formView" value="pages/searchResult"/&gt;
 *       &lt;property name="validator" ref="searchValidator"/&gt;
 *       &lt;property name="snippetNodeNames" value="text,teaserText,searchContent,pressText"/&gt;
 *       &lt;!-- 100 characters are default --&gt;
 *       &lt;!--property name="numberOfChars" value="150" /--&gt;
 *       &lt;!-- 15 entries are default --&gt;
 *       &lt;!--property name="entriesPerPage" value="5" /--&gt;
 *       &lt;!-- level 2 is default --&gt;
 *       &lt;!--property name="startLevel" value="3" /--&gt;
 *       &lt;!-- true is default --&gt;
 *       &lt;!--property name="sqlQuery" value="false" /--&gt;
 *       &lt;!-- &lt;em&gt; is default --&gt;
 *       &lt;!--property name="highlightTag" value="&lt;em&gt;" /--&gt;
 *   &lt;/bean&gt;
 *   &lt;bean id="searchValidator" class="com.aperto.magkit.controller.SearchValidator"/&gt;
 * </code>
 *
 * @author frank.sommer (22.05.2008)
 */
public class SearchController extends SimpleFormController {
    private static final Logger LOGGER = Logger.getLogger(SearchController.class);
    /**
     * Reserved chars, stripped from query. @see SimpleSearchTag
     */
    private static final String RESERVED_CHARS = "()[]{}<>:/\\@*?=\"'&~";
    /**
     * keywords.
     */
    private static final String[] KEYWORDS = new String[]{"and", "or"};
    /**
     * For filtering.
     */
    private static final Predicate FILTER_SEARCHABLE_PREDICATE = new Predicate() {
        public boolean evaluate(Object obj){
            Content c = (Content) obj;
            return !excludeFromSearch(c);
        }

        private boolean excludeFromSearch(Content c){
            boolean exclude = false;
            try {
                if (c.hasNodeData("notSearchable")) {
                    exclude = c.getNodeData("notSearchable").getBoolean();
                }
                Content parent = c.getParent();
                if (!exclude && parent != null) {
                    exclude = excludeFromSearch(parent);
                }
            } catch (RepositoryException e) {
                // ignore, happens for root element
                //LOGGER.warn("Could not get nodeDate or parent of content element", e);
            }
            return exclude;
        }
    };
    /**
     * Entries for page.
     */
    public static final int DEFAULT_ENTRIES_PER_PAGE = 15;
    public static final Pattern HTML_TAG_PATTERN = Pattern.compile("\\<(.*?\\s*)*\\>");
    /**
     * Node data names for retrieving snippets.
     */
    private String[] _snippetNodeNames;
    private int _numberOfChars = 100;
    private int _entriesPerPage = DEFAULT_ENTRIES_PER_PAGE;
    private String _defaultSnippet = "";
    private String _snippet = "";
    private int _startLevel = 2;
    private boolean _sqlQuery = true;
    private String _highlightTag = "<em>";

    public String getHighlightTag() {
        return _highlightTag;
    }

    public void setHighlightTag(String highlightTag) {
        _highlightTag = highlightTag;
    }

    public void setSqlQuery(boolean sqlQuery) {
        _sqlQuery = sqlQuery;
    }

    public void setNumberOfChars(int numberOfChars) {
        _numberOfChars = numberOfChars;
    }

    public void setSnippetNodeNames(String[] snippetNodeNames) {
        _snippetNodeNames = snippetNodeNames;
    }

    public void setEntriesPerPage(int entriesPerPage) {
        _entriesPerPage = entriesPerPage;
    }

    public String[] getSnippetNodeNames() {
        return _snippetNodeNames;
    }

    public int getNumberOfChars() {
        return _numberOfChars;
    }

    public int getEntriesPerPage() {
        return _entriesPerPage;
    }

    /**
     * Setter for <code>startLevel</code>.
     * @param startLevel The startLevel to set.
     */
    public void setStartLevel(int startLevel) {
        _startLevel = startLevel;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        Search search = (Search) command;
        String queryString = _sqlQuery ? generateSqlQuery(search) : generateXPathQuery(search);
        boolean validQuery = !errors.hasFieldErrors();

        if (queryString == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("A valid query could not be built, skipping");
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Executing query " + queryString);
            }
            int actPage = SelectorUtils.retrieveActivePage();
            int offset = _entriesPerPage * (actPage - 1);
            search.setActPage(actPage);
            search.setEntriesPerPage(_entriesPerPage);

            try {
                Collection searchResults = executeQuery(queryString);
                List<Content> websiteQueryResult = IteratorUtils.toList(IteratorUtils.filteredIterator((Iterator<Content>) searchResults.iterator(), FILTER_SEARCHABLE_PREDICATE));
                List<SearchHit> resultList = new ArrayList<SearchHit>(_entriesPerPage);
                int numberOfHits = websiteQueryResult.size();
                if (numberOfHits == 0) {
                    errors.reject("search.search.noResult", "search.search.noResult");
                }
                search.setNumberOfHits(numberOfHits);
                for (int i = offset; i < websiteQueryResult.size() && i < offset + _entriesPerPage; i++) {
                    Content content = websiteQueryResult.get(i);
                    SearchHit searchHit = collectSearchHitData(content, search.getQ());
                    resultList.add(searchHit);
                }
                search.setHits(resultList);
            } catch (Exception e) {
                LOGGER.warn(MessageFormat.format(
                    "{0} caught while parsing query for search term [{1}] - query is [{2}]: {3}",
                        e.getClass().getName(), search.getQ(), queryString, e.getMessage()), e);
            }
        }

        Map map = errors.getModel();
        map.put("validQuery", validQuery);
        return new ModelAndView(getSuccessView(), map);
    }

    protected Collection executeQuery(String queryString) throws RepositoryException {
        Query q = MgnlContext.getQueryManager(ContentRepository.WEBSITE).createQuery(queryString, _sqlQuery ? Query.SQL : Query.XPATH);
        QueryResult result = q.execute();
        return result.getContent();
    }

    /**
     * Override this to fill extra search hit data.
     */
    protected SearchHit collectSearchHitData(Content content, String query) throws RepositoryException {
        SearchHit searchHit = new SearchHit();
        searchHit.setTitle(retrieveTitle(content));
        searchHit.setAbstract(getSnippet(query, content));
        searchHit.setHandle(content.getHandle() + retrieveSelector(content, query));
        return searchHit;
    }

    /**
     * Override me, if you need a special selector in the handle. E.g. to show dynamic content.
     */
    protected String retrieveSelector(Content content, String query) {
        return "";
    }

    private String retrieveTitle(Content content) throws RepositoryException {
        String title = retrieveHeadlineFromContent(content, "headline");
        if (isEmpty(title)) {
            title = retrieveHeadlineFromContent(content, "headlineText");
        }
        if (isEmpty(title) && content.hasNodeData("title")) {
            title = content.getTitle();
        }
        if (isEmpty(title)) {
            title = ResourceBundle.getBundle("language").getString("page.noTitle");
        }
        return title;
    }

    private String retrieveHeadlineFromContent(Content content, String contentName) throws RepositoryException {
        String title = "";
        if (content.hasContent(contentName)) {
            Content headline = content.getChildByName(contentName);
            if (headline.hasNodeData("headline")) {
                title = headline.getNodeData("headline").getString();
            }
        }
        return title;
    }

    /**
     * Extract a snippet from any paragraph in the given page.
     * The first find node data is the default snippet.
     */
    private String getSnippet(String query, Content page) {
        Collection allChilds = ContentUtil.collectAllChildren(page, ItemType.CONTENTNODE);
        _snippet = "";
        _defaultSnippet = "";

        for (Object contentObj : allChilds) {
            Content child = (Content) contentObj;
            Collection nodeDatas = child.getNodeDataCollection();
            if (checkNodeDatas(nodeDatas, query)) {
                break;
            }
        }
        return _snippet.length() > 0 ? _snippet : _defaultSnippet;
    }

    private boolean checkNodeDatas(Collection properties, String query) {
        boolean foundSnippet = false;
        Iterator iterator = properties.iterator();
        while (!foundSnippet && iterator.hasNext()) {
            NodeData property = (NodeData) iterator.next();
            if (property.getType() != PropertyType.BINARY) {
                if (contains(_snippetNodeNames, property.getName())) {
                    // strips out html tags using a regexp
                    String resultString = stripHtmlTags(property.getString());
                    if (isBlank(_defaultSnippet)) {
                        _defaultSnippet = substring(resultString, 0, Math.min(_numberOfChars, resultString.length()));
                    }
                    String[] searchTerms = split(query);
                    for (String searchTerm : searchTerms) {
                        String lowerTerm = lowerCase(searchTerm);

                        // exclude keywords and words with less than 2 chars
                        if (!contains(KEYWORDS, lowerTerm) && lowerTerm.length() > 2) {

                            // first check, avoid using heavy string replaceAll operations if the search term is not there
                            if (contains(resultString.toLowerCase(Locale.GERMAN), lowerTerm)) {

                                // only get first matching keyword
                                int pos = resultString.toLowerCase(Locale.GERMAN).indexOf(lowerTerm);
                                if (pos > -1) {
                                    resultString = buildSnippet(pos, resultString);
                                    resultString = markSearchTerms(resultString, searchTerms);                                    
                                    foundSnippet = true;
                                    _snippet = resultString;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return foundSnippet;
    }

    private String buildSnippet(int pos, String resultString) {
        String returnString = resultString;
        int from = Math.max((pos - _numberOfChars / 2), 0);
        int to = Math.min(from + _numberOfChars, returnString.length());
        returnString = substring(returnString, from, to).trim();
        if (from > 0) {
            int spacePos = returnString.indexOf(' ');
            if (spacePos > 0) {
                returnString = returnString.substring(spacePos);
            }
            returnString = "..." + returnString;
        }
        if (to < resultString.length()) {
            int spacePos = returnString.lastIndexOf(' ');
            if (spacePos > 0) {
                returnString = returnString.substring(0, spacePos);
            }
            returnString += "...";
        }
        return returnString;
    }

    private String markSearchTerms(String resultString, String[] searchTerms) {
        String returnString = resultString;
        StringBuffer stringBuffer = new StringBuffer();
        String closingTag = determineClosingTag(_highlightTag);

        stringBuffer.append("(\\b");
        for (String term : searchTerms) {
            if (stringBuffer.length() > 4) {
                stringBuffer.append("\\b|\\b");
            }
            stringBuffer.append(term);
        }
        stringBuffer.append("\\b)");

        try {
            Pattern pattern = Pattern.compile(stringBuffer.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            returnString = pattern.matcher(returnString).replaceAll(_highlightTag + "$1" + closingTag);
        } catch (PatternSyntaxException e) {
            LOGGER.info("Forbidden characters found.");
        }

        return returnString;
    }

    /**
     * TODO: move to util class e.g. aperto commons.
     */
    public static String determineClosingTag(String highlightTag) {
        String closingTag = highlightTag;
        if (isNotBlank(highlightTag) && highlightTag.charAt(0) == '<') {
            Pattern pattern = Pattern.compile("[ >]", Pattern.CASE_INSENSITIVE);
            closingTag = new StringBuilder(pattern.split(highlightTag)[0]).insert(1, '/').append('>').toString();
        }
        return closingTag;
    }

    private String stripHtmlTags(String resultString) {
        Matcher matcher = HTML_TAG_PATTERN.matcher(resultString);
        return matcher.replaceAll(EMPTY);
    }

    /**
     * Split search terms and build an xpath query. This is in the form:
     * <code>//*[@jcr:primaryType='mgnl:content']/\*\/\*[jcr:contains(., 'first') or jcr:contains(., 'second')]</code>
     * @return valid xpath expression or null if the given query doesn't contain at least one valid search term
     */
    protected String generateXPathQuery(Search search) {
        String startPath = retrieveStartPath();
        // strip reserved chars and split
        String[] tokens = split(lowerCase(replaceChars(search.getQ(), RESERVED_CHARS, null)));

        StringBuffer xpath = new StringBuffer(tokens.length * 20);
        if (isNotEmpty(startPath)) {
            xpath.append(startPath);
        }
        xpath.append("//*[@jcr:primaryType=\'mgnl:content\']//*[");
        String joinOperator = "and";
        boolean emptyQuery = true;

        for (String tkn : tokens) {
            if (contains(KEYWORDS, tkn)) {
                joinOperator = tkn;
            } else {
                if (!emptyQuery) {
                    xpath.append(" ");
                    xpath.append(joinOperator);
                    xpath.append(" ");
                }
                xpath.append("jcr:contains(., '");
                xpath.append(tkn);
                xpath.append("')");
                emptyQuery = false;
            }
        }
        xpath.append("]");

        return emptyQuery ? null : xpath.toString();
    }

    /**
     * Split search terms and build a sql query. This is in the form:
     * <code>SELECT * FROM mgnl:contentNode WHERE CONTAINS(*, 'Wohnungssuche') and jcr:path LIKE '/content/de/%' ORDER BY jcr:score DESC</code>
     * @return valid sql expression or null if the given query doesn't contain at least one valid search term
     */
    protected String generateSqlQuery(Search search) {
        String startPath = retrieveStartPath();
        // strip reserved chars and split
        String[] tokens = split(lowerCase(replaceChars(search.getQ(), RESERVED_CHARS, null)));

        StringBuffer sql = new StringBuffer(tokens.length * 20);
        sql.append("SELECT * FROM mgnl:contentNode WHERE ");
        String joinOperator = "and";
        boolean emptyQuery = true;

        for (String tkn : tokens) {
            if (contains(KEYWORDS, tkn)) {
                joinOperator = tkn;
            } else {
                if (!emptyQuery) {
                    sql.append(" ");
                    sql.append(joinOperator);
                    sql.append(" ");
                }
                sql.append("CONTAINS(*, '");
                sql.append(tkn);
                sql.append("')");
                emptyQuery = false;
            }
        }
        if (!isBlank(startPath)) {
            sql.append(" AND jcr:path LIKE '/").append(startPath).append("/%'");
        }
        sql.append(" ORDER BY jcr:score DESC");

        return emptyQuery ? null : sql.toString();
    }

    private String retrieveStartPath() {
        String startPath = "";
        // search only in a specific subtree
        if (_startLevel > 0) {
            try {
                Content activePage = Resource.getActivePage();
                if (activePage != null) {
                    startPath = strip(activePage.getAncestor(_startLevel).getHandle(), "/");
                }
            } catch (RepositoryException e) {
                LOGGER.warn("Could not get ancestor from actual page.");
            }
        }
        return startPath;
    }

    @Override
    protected boolean isFormSubmission(HttpServletRequest request) {
        return request.getParameter("q") != null;
    }
}
