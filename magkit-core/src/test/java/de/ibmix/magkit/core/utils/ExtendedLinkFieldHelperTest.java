package de.ibmix.magkit.core.utils;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.init.MagnoliaConfigurationProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.net.URI;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.mockServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultBaseUrl;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultExtension;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubContextPath;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockMgnlNode;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing ExtendedLinkFieldHelper.
 *
 * @author Philipp Güttler (IBM iX)
 * @since 2015-06-03
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

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _helper = new ExtendedLinkFieldHelper();
    }

    @Test
    public void testGetBase() {
        assertNull(_helper.getBase(null));
        assertNull(_helper.getBase(EMPTY));
        assertEquals(PATH, _helper.getBase(PATH));
        assertEquals(PATH, _helper.getBase(PATH_WITH_ANCHOR));
        assertEquals(PATH, _helper.getBase(PATH_WITH_QUERY));
        assertEquals(PATH, _helper.getBase(PATH_WITH_SELECTOR));
        assertEquals(PATH, _helper.getBase(PATH_FULL));
        assertEquals(UUID, _helper.getBase(UUID));
        assertEquals(EMPTY, _helper.getBase("#" + ANCHOR));
    }

    @Test
    public void testGetSelectors() {
        assertNull(_helper.getSelectors(null));
        assertNull(_helper.getSelectors(EMPTY));
        assertNull(_helper.getSelectors(PATH));
        assertNull(_helper.getSelectors(PATH_WITH_ANCHOR));
        assertNull(_helper.getSelectors(PATH_WITH_QUERY));
        assertEquals(SELECTOR_FOO, _helper.getSelectors(PATH_WITH_SELECTOR));
        assertEquals(SELECTORS, _helper.getSelectors(PATH_FULL));
        assertNull(_helper.getSelectors("#" + ANCHOR));
    }

    @Test
    public void testCreateUriToNull() {
        assertNull(_helper.createUri(null));
        assertNull(_helper.createUri(EMPTY));
        assertNull(_helper.createUri(SELECTORS));
    }

    @Test
    public void testCreateUri() throws Exception {
        assertEquals(new URI(PATH), _helper.createUri(PATH));
        assertEquals(new URI(PATH_WITH_ANCHOR), _helper.createUri(PATH_WITH_ANCHOR));
        assertEquals(new URI(PATH_WITH_SELECTOR), _helper.createUri(PATH_WITH_SELECTOR));
        assertEquals(new URI(PATH_WITH_QUERY), _helper.createUri(PATH_WITH_QUERY));
        assertEquals(new URI(PATH_FULL), _helper.createUri(PATH_FULL));
        assertEquals(new URI("/" + UUID), _helper.createUri(UUID));
        assertEquals(new URI("#" + ANCHOR), _helper.createUri("#" + ANCHOR));
    }

    @Test
    public void testGetAnchor() {
        assertNull(_helper.getQuery(null));
        assertNull(_helper.getAnchor(PATH));
        assertNull(_helper.getAnchor(PATH_WITH_SELECTOR));
        assertNull(_helper.getAnchor(PATH_WITH_QUERY));
        assertEquals(ANCHOR, _helper.getAnchor(PATH_WITH_ANCHOR));
        assertEquals(ANCHOR, _helper.getAnchor(PATH_FULL));
        assertEquals(ANCHOR, _helper.getAnchor("#" + ANCHOR));
    }

    @Test
    public void testGetQuery() {
        assertNull(_helper.getQuery(null));
        assertNull(_helper.getQuery(PATH));
        assertNull(_helper.getQuery(PATH_WITH_ANCHOR));
        assertNull(_helper.getQuery(PATH_WITH_SELECTOR));
        assertEquals(QUERY, _helper.getQuery(PATH_WITH_QUERY));
        assertEquals(QUERY, _helper.getQuery(PATH_FULL));
        assertNull(_helper.getQuery("#" + ANCHOR));
    }

    @Test
    public void testContainsMoreSelectors() {
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
    public void testMergeComponentsForPath() {
        assertEquals(EMPTY, _helper.mergeComponents(null, null, null, null));
        assertEquals(PATH, _helper.mergeComponents(PATH, null, null, null));
        assertEquals(PATH_WITH_SELECTOR, _helper.mergeComponents(PATH, SELECTOR_FOO, null, null));
        assertEquals(PATH_WITH_QUERY, _helper.mergeComponents(PATH, null, QUERY, null));
        assertEquals(PATH_WITH_ANCHOR, _helper.mergeComponents(PATH, null, null, ANCHOR));
        assertEquals("#" + ANCHOR, _helper.mergeComponents(null, null, null, ANCHOR));
        assertEquals(PATH_FULL, _helper.mergeComponents(PATH, SELECTORS, QUERY, ANCHOR));
    }

    @Test
    public void testMergeComponentsForExternalLink() {
        assertEquals(EMPTY, _helper.mergeComponents(null, null, null, null));
        assertEquals(EXTERNAL_LINK, _helper.mergeComponents(EXTERNAL_LINK, null, null, null));
        assertEquals(EXTERNAL_WITH_SELECTOR, _helper.mergeComponents(EXTERNAL_LINK, SELECTORS, null, null));
        assertEquals(EXTERNAL_WITH_QUERY, _helper.mergeComponents(EXTERNAL_LINK, null, QUERY, null));
        assertEquals(EXTERNAL_WITH_ANCHOR, _helper.mergeComponents(EXTERNAL_LINK, null, null, ANCHOR));
        assertEquals("#" + ANCHOR, _helper.mergeComponents(null, null, null, ANCHOR));
        assertEquals(EXTERNAL_FULL, _helper.mergeComponents(EXTERNAL_LINK, SELECTORS, QUERY, ANCHOR));
    }

    @Test
    public void createExtendedLink() throws RepositoryException {
        mockComponentInstance(MagnoliaConfigurationProperties.class);
        assertNull(_helper.createExtendedLink(null, null, null, null));

        Node source = mockNode("source");
        assertNull(_helper.createExtendedLink(source, "link", "test", LinkTool.LinkType.EXTERNAL));

        mockWebContext(stubContextPath("/aperto"));
        mockServerConfiguration(stubDefaultBaseUrl("http://test.aperto.de"), stubDefaultExtension("html"));
        Node target = mockMgnlNode("test", "target", "aperto:test");
        stubProperty("link", target).of(source);
        assertEquals("http://test.aperto.de/target.html", _helper.createExtendedLink(source, "link", "test", LinkTool.LinkType.EXTERNAL));

        stubProperty("link_selector", SELECTOR_FOO).of(source);
        assertEquals("http://test.aperto.de/target~foo=bar~.html", _helper.createExtendedLink(source, "link", "test", LinkTool.LinkType.EXTERNAL));

        stubProperty("link_anchor", ANCHOR).of(source);
        assertEquals("http://test.aperto.de/target~foo=bar~.html#anchor", _helper.createExtendedLink(source, "link", "test", LinkTool.LinkType.EXTERNAL));

        stubProperty("link_query", QUERY).of(source);
        assertEquals("http://test.aperto.de/target~foo=bar~.html?param=value#anchor", _helper.createExtendedLink(source, "link", "test", LinkTool.LinkType.EXTERNAL));
    }

    // Additional tests for uncovered branches and edge cases.

    @Test
    public void testGetBaseForUuidWithComponents() {
        String value = UUID + "~" + SELECTORS + "~" + "?" + QUERY + "#" + ANCHOR;
        assertEquals(UUID, _helper.getBase(value));
    }

    @Test
    public void testCreateUriForUuidWithComponents() {
        String value = UUID + "~" + SELECTOR_FOO + "~" + "?" + QUERY + "#" + ANCHOR;
        URI uri = _helper.createUri(value);
        assertEquals(URI.create("/" + value), uri);
    }

    @Test
    public void testMergeComponentsSelectorsOnly() {
        assertEquals("~" + SELECTOR_FOO + "~", _helper.mergeComponents(null, SELECTOR_FOO, null, null));
        assertEquals("~" + SELECTOR_FOO + "~" + "?" + QUERY + "#" + ANCHOR, _helper.mergeComponents(null, SELECTOR_FOO, QUERY, ANCHOR));
    }

    @Test
    public void testGetSelectorsWithExtension() {
        String pathWithSelectorAndExt = "/content/page" + "~" + SELECTOR_FOO + "~" + ".html";
        assertEquals(SELECTOR_FOO, _helper.getSelectors(pathWithSelectorAndExt));
    }

    @Test
    public void testCreateExtendedLinkExternalPropertyValue() throws RepositoryException {
        Node source = mockNode("sourceExternal");
        stubProperty("link", EXTERNAL_LINK).of(source);
        stubProperty("link_selector", SELECTORS).of(source);
        stubProperty("link_query", QUERY).of(source);
        stubProperty("link_anchor", ANCHOR).of(source);
        String expected = EXTERNAL_LINK.replace(".html", "~" + SELECTORS + "~.html") + "?" + QUERY + "#" + ANCHOR;
        assertEquals(expected, _helper.createExtendedLink(source, "link", "ignored", LinkTool.LinkType.EXTERNAL));
    }

    @Test
    public void testStripBase() {
        assertEquals(EMPTY, _helper.stripBase(null));
        assertEquals(EMPTY, _helper.stripBase(PATH));
        assertEquals("~" + SELECTOR_FOO + "~", _helper.stripBase(PATH_WITH_SELECTOR));
        assertEquals("?" + QUERY, _helper.stripBase(PATH_WITH_QUERY));
        assertEquals("#" + ANCHOR, _helper.stripBase(PATH_WITH_ANCHOR));
        assertEquals("~" + SELECTORS + "~" + "?" + QUERY + "#" + ANCHOR, _helper.stripBase(PATH_FULL));
        assertEquals("#" + ANCHOR, _helper.stripBase("#" + ANCHOR));
        String uuidFull = UUID + "~" + SELECTORS + "~" + "?" + QUERY + "#" + ANCHOR;
        assertEquals("~" + SELECTORS + "~" + "?" + QUERY + "#" + ANCHOR, _helper.stripBase(uuidFull));
    }

    @AfterAll
    public static void tearDown() throws Exception {
        cleanContext();
    }
}
