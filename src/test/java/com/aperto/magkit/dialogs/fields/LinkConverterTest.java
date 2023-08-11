package com.aperto.magkit.dialogs.fields;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubIdentifier;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test the {@link LinkConverter}.
 *
 * @author frank.sommer
 * @since 3.1.3
 */
public class LinkConverterTest {

    @Test
    public void testConvertToModel() throws Exception {
        final LinkConverter linkConverter = new LinkConverter();
        linkConverter.setWorkspaceName(WEBSITE);
        assertThat(linkConverter.convertToModel(null, null, null), nullValue());
        assertThat(linkConverter.convertToModel("", null, null), equalTo(""));
        assertThat(linkConverter.convertToModel("#test", null, null), equalTo("#test"));
        assertThat(linkConverter.convertToModel("http://www.aperto.de", null, null), equalTo("http://www.aperto.de"));
        assertThat(linkConverter.convertToModel("https://www.aperto.de", null, null), equalTo("https://www.aperto.de"));
        assertThat(linkConverter.convertToModel("/path/to/page", null, null), equalTo("123-133"));
    }

    @Test
    public void testConvertToPresentation() throws Exception {
        final LinkConverter linkConverter = new LinkConverter();
        linkConverter.setWorkspaceName(WEBSITE);
        assertThat(linkConverter.convertToPresentation(null, null, null), nullValue());
        assertThat(linkConverter.convertToPresentation("", null, null), equalTo(""));
        assertThat(linkConverter.convertToPresentation("#test", null, null), equalTo("#test"));
        assertThat(linkConverter.convertToPresentation("http://www.aperto.de", null, null), equalTo("http://www.aperto.de"));
        assertThat(linkConverter.convertToPresentation("https://www.aperto.de", null, null), equalTo("https://www.aperto.de"));
        assertThat(linkConverter.convertToPresentation("123-133", null, null), equalTo("/path/to/page"));

    }

    @Before
    public void prepareMgnl() throws RepositoryException {
        cleanContext();
        mockPageNode("/path/to/page", stubIdentifier("123-133"));
    }

    @After
    public void cleanMgnl() throws RepositoryException {
        cleanContext();
    }
}