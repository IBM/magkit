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
import static com.aperto.magkit.utils.LinkTool.isUuid;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.UUID;

/**
 * Test the LinkTool class.
 *
 * @author frank.sommer
 * @since 17.01.13
 */
public class LinkToolTest {

    @Test
    public void testIsUuid() throws Exception {
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
        Node target = mockMgnlNode("target", "test", "aperto:test", stubIdentifier(UUID.randomUUID().toString()));
        stubProperty("link", target).of(source);
        assertThat(LinkTool.createLinkForReference(source, "link", null, null), nullValue());
        assertThat(LinkTool.createLinkForReference(source, "link", "test", LinkTool.LinkType.INTERNAL), is("/aperto/target.html"));
        assertThat(LinkTool.createLinkForReference(source, "link", "test", LinkTool.LinkType.REDIRECT), is("/target.html"));
        assertThat(LinkTool.createLinkForReference(source, "link", "test", LinkTool.LinkType.EXTERNAL), is("http://test.aperto.de/target.html"));
    }
}
