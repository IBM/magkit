package com.aperto.magkit.utils;

import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockMgnlNode;
import static com.aperto.magkit.mockito.ServerConfigurationMockUtils.mockServerConfiguration;
import static com.aperto.magkit.mockito.ServerConfigurationStubbingOperation.stubDefaultBaseUrl;
import static com.aperto.magkit.mockito.ServerConfigurationStubbingOperation.stubDefaultExtension;
import static com.aperto.magkit.mockito.WebContextStubbingOperation.stubContextPath;
import static com.aperto.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubIdentifier;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubProperty;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * @author Philipp Güttler (Aperto AG)
 * @since 03.06.2015
 */
public class ExtendedLinkFieldHelperTest {

    private static final String ANCHOR = "anchor";
    private static final String SELECTOR_FOO = "foo=bar";
    private static final String SELECTOR_FOT = "fot=baz";
    private static final String SELECTORS = SELECTOR_FOO + "~" + SELECTOR_FOT;
    private static final String QUERY = "param=value";
    private static final String UUID = randomUUID().toString();

    private static final String PATH = "/path/to/node";
    private static final String PATH_WITH_ANCHOR = PATH + "#" + ANCHOR;
    private static final String PATH_WITH_SELECTOR = PATH + "~" + SELECTOR_FOO + "~";
    private static final String PATH_WITH_QUERY = PATH + "?" + QUERY;
    private static final String PATH_FULL = PATH + "~" + SELECTORS + "~" + "?" + QUERY + "#" + ANCHOR;
    private static final String EXTERNAL_LINK = "http://test.aperto.de/path/to/node.html";
    private static final String EXTERNAL_WITH_ANCHOR = EXTERNAL_LINK + "#" + ANCHOR;
    private static final String EXTERNAL_WITH_SELECTOR = "http://test.aperto.de/path/to/node" + "~" + SELECTORS + "~" + ".html";
    private static final String EXTERNAL_WITH_QUERY = EXTERNAL_LINK + "?" + QUERY;
    private static final String EXTERNAL_FULL = EXTERNAL_WITH_SELECTOR + "?" + QUERY + "#" + ANCHOR;

    private ExtendedLinkFieldHelper _helper;

    @Before
    public void setUp() throws Exception {
        _helper = new ExtendedLinkFieldHelper();
    }

    @Test
    public void testGetBase() throws Exception {
        assertThat(_helper.getBase(null), nullValue());
        assertThat(_helper.getBase(EMPTY), nullValue());
        assertThat(_helper.getBase(PATH), equalTo(PATH));
        assertThat(_helper.getBase(PATH_WITH_ANCHOR), equalTo(PATH));
        assertThat(_helper.getBase(PATH_WITH_QUERY), equalTo(PATH));
        assertThat(_helper.getBase(PATH_WITH_SELECTOR), equalTo(PATH));
        assertThat(_helper.getBase(PATH_FULL), equalTo(PATH));
        assertThat(_helper.getBase(UUID), equalTo(UUID));
        // should we allow this?
        assertThat(_helper.getBase("#" + ANCHOR), equalTo(EMPTY));
    }

    @Test
    public void testGetSelectors() throws Exception {
        assertThat(_helper.getSelectors(null), nullValue());
        assertThat(_helper.getSelectors(EMPTY), nullValue());
        assertThat(_helper.getSelectors(PATH), nullValue());
        assertThat(_helper.getSelectors(PATH_WITH_ANCHOR), nullValue());
        assertThat(_helper.getSelectors(PATH_WITH_QUERY), nullValue());
        assertThat(_helper.getSelectors(PATH_WITH_SELECTOR), equalTo(SELECTOR_FOO));
        assertThat(_helper.getSelectors(PATH_FULL), equalTo(SELECTORS));
        assertThat(_helper.getSelectors("#" + ANCHOR), nullValue());
    }

    @Test
    public void testCreateUriToNull() throws Exception {
        assertThat(_helper.createUri(null), nullValue());
        assertThat(_helper.createUri(EMPTY), nullValue());
        assertThat(_helper.createUri(SELECTORS), nullValue());
    }

    @Test
    public void testCreateUri() throws Exception {
        assertThat(_helper.createUri(PATH), equalTo(new URI(PATH)));
        assertThat(_helper.createUri(PATH_WITH_ANCHOR), equalTo(new URI(PATH_WITH_ANCHOR)));
        assertThat(_helper.createUri(PATH_WITH_SELECTOR), equalTo(new URI(PATH_WITH_SELECTOR)));
        assertThat(_helper.createUri(PATH_WITH_QUERY), equalTo(new URI(PATH_WITH_QUERY)));
        assertThat(_helper.createUri(PATH_FULL), equalTo(new URI(PATH_FULL)));
        assertThat(_helper.createUri(UUID), equalTo(new URI("/" + UUID)));
        assertThat(_helper.createUri("#" + ANCHOR), equalTo(new URI("#" + ANCHOR)));
    }

    @Test
    public void testGetAnchor() throws Exception {
        assertThat(_helper.getQuery(null), nullValue());
        assertThat(_helper.getAnchor(PATH), nullValue());
        assertThat(_helper.getAnchor(PATH_WITH_SELECTOR), nullValue());
        assertThat(_helper.getAnchor(PATH_WITH_QUERY), nullValue());
        assertThat(_helper.getAnchor(PATH_WITH_ANCHOR), equalTo(ANCHOR));
        assertThat(_helper.getAnchor(PATH_FULL), equalTo(ANCHOR));
        assertThat(_helper.getAnchor("#" + ANCHOR), equalTo(ANCHOR));
    }

    @Test
    public void testGetQuery() throws Exception {
        assertThat(_helper.getQuery(null), nullValue());
        assertThat(_helper.getQuery(PATH), nullValue());
        assertThat(_helper.getQuery(PATH_WITH_ANCHOR), nullValue());
        assertThat(_helper.getQuery(PATH_WITH_SELECTOR), nullValue());
        assertThat(_helper.getQuery(PATH_WITH_QUERY), equalTo(QUERY));
        assertThat(_helper.getQuery(PATH_FULL), equalTo(QUERY));
        assertThat(_helper.getQuery("#" + ANCHOR), nullValue());
    }

    @Test
    public void testContainsMoreSelectors() throws Exception {
        assertFalse(_helper.containsMoreSelectors(null));
        assertFalse(_helper.containsMoreSelectors(EMPTY));
        assertFalse(_helper.containsMoreSelectors(PATH));
        assertFalse(_helper.containsMoreSelectors(PATH_WITH_ANCHOR));
        assertFalse(_helper.containsMoreSelectors(PATH_WITH_QUERY));
        assertFalse(_helper.containsMoreSelectors(".html?query=123~123"));
        assertFalse(_helper.containsMoreSelectors("#" + ANCHOR));
        assertTrue(_helper.containsMoreSelectors(PATH_WITH_SELECTOR));
    }

    @Test
    public void testMergeComponentsForPath() throws Exception {
        assertThat(_helper.mergeComponents(null, null, null, null), equalTo(EMPTY));
        assertThat(_helper.mergeComponents(PATH, null, null, null), equalTo(PATH));
        assertThat(_helper.mergeComponents(PATH, SELECTOR_FOO, null, null), equalTo(PATH_WITH_SELECTOR));
        assertThat(_helper.mergeComponents(PATH, null, QUERY, null), equalTo(PATH_WITH_QUERY));
        assertThat(_helper.mergeComponents(PATH, null, null, ANCHOR), equalTo(PATH_WITH_ANCHOR));
        assertThat(_helper.mergeComponents(null, null, null, ANCHOR), equalTo("#" + ANCHOR));
        assertThat(_helper.mergeComponents(PATH, SELECTORS, QUERY, ANCHOR), equalTo(PATH_FULL));
    }

    @Test
    public void testMergeComponentsForExternalLink() throws Exception {
        assertThat(_helper.mergeComponents(null, null, null, null), equalTo(EMPTY));
        assertThat(_helper.mergeComponents(EXTERNAL_LINK, null, null, null), equalTo(EXTERNAL_LINK));
        assertThat(_helper.mergeComponents(EXTERNAL_LINK, SELECTORS, null, null), equalTo(EXTERNAL_WITH_SELECTOR));
        assertThat(_helper.mergeComponents(EXTERNAL_LINK, null, QUERY, null), equalTo(EXTERNAL_WITH_QUERY));
        assertThat(_helper.mergeComponents(EXTERNAL_LINK, null, null, ANCHOR), equalTo(EXTERNAL_WITH_ANCHOR));
        assertThat(_helper.mergeComponents(null, null, null, ANCHOR), equalTo("#" + ANCHOR));
        assertThat(_helper.mergeComponents(EXTERNAL_LINK, SELECTORS, QUERY, ANCHOR), equalTo(EXTERNAL_FULL));
    }

    @Test
    public void createExtendedLink() throws RepositoryException {
        assertThat(_helper.createExtendedLink(null, null, null, null), equalTo(null));

        Node source = mockNode("source");
        mockWebContext(stubContextPath("/aperto"));
        mockServerConfiguration(stubDefaultBaseUrl("http://test.aperto.de"), stubDefaultExtension("html"));
        Node target = mockMgnlNode("target", "test", "aperto:test", stubIdentifier(java.util.UUID.randomUUID().toString()));
        stubProperty("link", target).of(source);
        assertThat(_helper.createExtendedLink(source, "link", "test", LinkTool.LinkType.EXTERNAL), equalTo("http://test.aperto.de/target.html"));

        stubProperty("link_selector", SELECTOR_FOO).of(source);
        assertThat(_helper.createExtendedLink(source, "link", "test", LinkTool.LinkType.EXTERNAL), equalTo("http://test.aperto.de/target~foo=bar~.html"));

        stubProperty("link_anchor", ANCHOR).of(source);
        assertThat(_helper.createExtendedLink(source, "link", "test", LinkTool.LinkType.EXTERNAL), equalTo("http://test.aperto.de/target~foo=bar~.html#anchor"));

        stubProperty("link_query", QUERY).of(source);
        assertThat(_helper.createExtendedLink(source, "link", "test", LinkTool.LinkType.EXTERNAL), equalTo("http://test.aperto.de/target~foo=bar~.html?param=value#anchor"));
    }
}
