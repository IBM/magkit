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

import static de.ibmix.magkit.core.utils.LinkTool.isUuid;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the LinkTool class.
 *
 * @author frank.sommer
 * @since 17.01.13
 */
public class LinkToolTest {

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void testIsUuid() {
        assertFalse(isUuid(null));
        assertFalse(isUuid(""));
        assertFalse(isUuid("www.aperto.de"));
        assertFalse(isUuid("12345-45454-54545"));
        assertFalse(isUuid("dc307c08-5a19-4260-a304-a5611d1ca900 1"));
        assertTrue(isUuid("dc307c08-5a19-4260-a304-a5611d1ca900"));
    }

    @Test
    public void isExternalLink() {
        assertFalse(LinkTool.isExternalLink(null));
        assertFalse(LinkTool.isExternalLink(""));
        assertFalse(LinkTool.isExternalLink("  \t "));
        assertFalse(LinkTool.isExternalLink("test"));
        assertFalse(LinkTool.isExternalLink("/test"));
        assertTrue(LinkTool.isExternalLink("http://test.aperto.de"));
        assertTrue(LinkTool.isExternalLink("https://test.aperto.de"));
        assertTrue(LinkTool.isExternalLink("HTTPS://test.aperto.de"));
    }

    @Test
    public void isPath() {
        assertFalse(LinkTool.isPath(null));
        assertFalse(LinkTool.isPath(""));
        assertFalse(LinkTool.isPath("  \t "));
        assertFalse(LinkTool.isPath("test"));
        assertTrue(LinkTool.isPath("/test"));
        assertFalse(LinkTool.isPath("http://test.aperto.de/test"));
    }

    @Test
    public void isAnchor() {
        assertFalse(LinkTool.isAnchor(null));
        assertFalse(LinkTool.isAnchor(""));
        assertFalse(LinkTool.isAnchor("  \t "));
        assertFalse(LinkTool.isAnchor("test"));
        assertTrue(LinkTool.isAnchor("#test"));
        assertFalse(LinkTool.isAnchor("/test#anchor"));
        assertFalse(LinkTool.isAnchor("http://test.aperto.de/test#anchor"));
    }

    @Test
    public void createLinkForReference() throws RepositoryException {
        mockComponentInstance(MagnoliaConfigurationProperties.class);
        assertNull(LinkTool.createLinkForReference(null, null, null, null));
        assertNull(LinkTool.createLinkForReference(null, " ", null, null));
        assertNull(LinkTool.createLinkForReference(null, "test", null, null));

        Node source = mockNode("source");
        assertNull(LinkTool.createLinkForReference(source, "link", null, null));

        stubProperty("link", "").of(source);
        assertNull(LinkTool.createLinkForReference(source, "link", null, null));

        stubProperty("link", "test").of(source);
        assertNull(LinkTool.createLinkForReference(source, "link", null, null));
        assertNull(LinkTool.createLinkForReference(source, "link", null, LinkTool.LinkType.INTERNAL));

        // External link value unchanged even if LinkType provided
        String external = "https://test.aperto.de";
        stubProperty("link", external).of(source);
        assertEquals(external, LinkTool.createLinkForReference(source, "link", null, null));
        assertEquals(external, LinkTool.createLinkForReference(source, "link", null, LinkTool.LinkType.EXTERNAL), "External link should be returned unchanged even with LinkType");

        mockWebContext(stubContextPath("/aperto"));
        mockServerConfiguration(stubDefaultBaseUrl("http://test.aperto.de"), stubDefaultExtension("html"));
        Node target = mockMgnlNode("test", "target", "aperto:test");
        stubProperty("link", target).of(source);
        assertNull(LinkTool.createLinkForReference(source, "link", null, null));
        assertEquals("/aperto/target.html", LinkTool.createLinkForReference(source, "link", "test", LinkTool.LinkType.INTERNAL));
        assertEquals("/target.html", LinkTool.createLinkForReference(source, "link", "test", LinkTool.LinkType.REDIRECT));
        assertEquals("http://test.aperto.de/target.html", LinkTool.createLinkForReference(source, "link", "test", LinkTool.LinkType.EXTERNAL));
    }

    @Test
    public void createExternalLinkForPath() throws RepositoryException {
        mockComponentInstance(MagnoliaConfigurationProperties.class);
        mockWebContext(stubContextPath("/aperto"));
        mockServerConfiguration(stubDefaultBaseUrl("http://test.aperto.de"), stubDefaultExtension("html"));
        Node site = mockMgnlNode("test", "target", "aperto:test");
        String result = LinkTool.createExternalLinkForPath(site, "/resources/image.png");
        assertEquals("http://test.aperto.de/aperto/resources/image.png", result);
    }
}
