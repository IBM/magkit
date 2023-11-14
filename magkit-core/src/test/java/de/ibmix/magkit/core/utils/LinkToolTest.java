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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.core.utils.LinkTool.isUuid;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.mockServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultBaseUrl;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultExtension;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubContextPath;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockMgnlNode;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Test the LinkTool class.
 *
 * @author frank.sommer
 * @since 17.01.13
 */
public class LinkToolTest {

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void testIsUuid() {
        assertThat(isUuid(null), is(false));
        assertThat(isUuid(""), is(false));
        assertThat(isUuid("www.aperto.de"), is(false));
        assertThat(isUuid("12345-45454-54545"), is(false));
        assertThat(isUuid("dc307c08-5a19-4260-a304-a5611d1ca900 1"), is(false));
        assertThat(isUuid("dc307c08-5a19-4260-a304-a5611d1ca900"), is(true));
    }

    @Test
    public void isExternalLink() {
        assertThat(LinkTool.isExternalLink(null), is(false));
        assertThat(LinkTool.isExternalLink(""), is(false));
        assertThat(LinkTool.isExternalLink("  \t "), is(false));
        assertThat(LinkTool.isExternalLink("test"), is(false));
        assertThat(LinkTool.isExternalLink("/test"), is(false));
        assertThat(LinkTool.isExternalLink("http://test.aperto.de"), is(true));
        assertThat(LinkTool.isExternalLink("https://test.aperto.de"), is(true));
        assertThat(LinkTool.isExternalLink("HTTPS://test.aperto.de"), is(true));
    }

    @Test
    public void isPath() {
        assertThat(LinkTool.isPath(null), is(false));
        assertThat(LinkTool.isPath(""), is(false));
        assertThat(LinkTool.isPath("  \t "), is(false));
        assertThat(LinkTool.isPath("test"), is(false));
        assertThat(LinkTool.isPath("/test"), is(true));
        assertThat(LinkTool.isPath("http://test.aperto.de/test"), is(false));
    }

    @Test
    public void isAnchor() {
        assertThat(LinkTool.isAnchor(null), is(false));
        assertThat(LinkTool.isAnchor(""), is(false));
        assertThat(LinkTool.isAnchor("  \t "), is(false));
        assertThat(LinkTool.isAnchor("test"), is(false));
        assertThat(LinkTool.isAnchor("#test"), is(true));
        assertThat(LinkTool.isAnchor("/test#anchor"), is(false));
        assertThat(LinkTool.isAnchor("http://test.aperto.de/test#anchor"), is(false));
    }

    @Test
    public void createLinkForReference() throws RepositoryException {
        assertThat(LinkTool.createLinkForReference(null, null, null, null), nullValue());
        assertThat(LinkTool.createLinkForReference(null, " ", null, null), nullValue());
        assertThat(LinkTool.createLinkForReference(null, "test", null, null), nullValue());

        Node source = mockNode("source");
        assertThat(LinkTool.createLinkForReference(source, "link", null, null), nullValue());

        stubProperty("link", "").of(source);
        assertThat(LinkTool.createLinkForReference(source, "link", null, null), nullValue());

        stubProperty("link", "test").of(source);
        assertThat(LinkTool.createLinkForReference(source, "link", null, null), nullValue());
        assertThat(LinkTool.createLinkForReference(source, "link", null, LinkTool.LinkType.INTERNAL), nullValue());

        stubProperty("link", "https://test.aperto.de").of(source);
        assertThat(LinkTool.createLinkForReference(source, "link", null, null), is("https://test.aperto.de"));

        mockWebContext(stubContextPath("/aperto"));
        mockServerConfiguration(stubDefaultBaseUrl("http://test.aperto.de"), stubDefaultExtension("html"));
        Node target = mockMgnlNode("test", "target", "aperto:test");
        stubProperty("link", target).of(source);
        assertThat(LinkTool.createLinkForReference(source, "link", null, null), nullValue());
        assertThat(LinkTool.createLinkForReference(source, "link", "test", LinkTool.LinkType.INTERNAL), is("/aperto/target.html"));
        assertThat(LinkTool.createLinkForReference(source, "link", "test", LinkTool.LinkType.REDIRECT), is("/target.html"));
        assertThat(LinkTool.createLinkForReference(source, "link", "test", LinkTool.LinkType.EXTERNAL), is("http://test.aperto.de/target.html"));
    }
}
