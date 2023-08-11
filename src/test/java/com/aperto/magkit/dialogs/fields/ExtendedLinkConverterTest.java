package com.aperto.magkit.dialogs.fields;

import com.aperto.magkit.mockito.ContextMockUtils;
import com.aperto.magkit.utils.ExtendedLinkFieldHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubIdentifier;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static java.util.Locale.GERMAN;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for converter of extended link field.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 04.06.2015
 */
public class ExtendedLinkConverterTest {

    private static final String IDENTIFIER = UUID.randomUUID().toString();
    private static final String PATH = "/path/to/node";
    private static final String PATH_NOT_FOUND = "/path/not/found";
    private static final String ANCHOR = "#anchor";
    private static final String EXTERNAL_URL = "http://www.aperto.com";
    private static final String IDENTIFIER_NOT_FOUND = UUID.randomUUID().toString();

    private ExtendedLinkConverter _converter;

    @Before
    public void setUp() throws Exception {
        _converter = new ExtendedLinkConverter();
        _converter.setExtendedLinkFieldHelper(new ExtendedLinkFieldHelper());
        _converter.setWorkspaceName(WEBSITE);
        ContextMockUtils.cleanContext();
        mockPageNode(PATH, stubIdentifier(IDENTIFIER));
    }

    @Test
    public void testConvertToModelNull() throws Exception {
        assertThat(_converter.convertToModel(null, String.class, GERMAN), nullValue());
    }

    @Test
    public void testConvertToModelPath() throws Exception {
        assertThat(_converter.convertToModel(PATH, String.class, GERMAN), equalTo(IDENTIFIER));
        assertThat(_converter.convertToModel(PATH_NOT_FOUND, String.class, GERMAN), equalTo(PATH_NOT_FOUND));
    }

    @Test
    public void testConvertToModelAnchor() throws Exception {
        assertThat(_converter.convertToModel(ANCHOR, String.class, GERMAN), equalTo(ANCHOR));
    }

    @Test
    public void testConvertToModelExternalUrl() throws Exception {
        assertThat(_converter.convertToModel(EXTERNAL_URL, String.class, GERMAN), equalTo(EXTERNAL_URL));
    }

    @Test
    public void testConvertToPresentationNull() throws Exception {
        assertThat(_converter.convertToPresentation(null, String.class, GERMAN), equalTo(EMPTY));
    }

    @Test
    public void testConvertToPresentationUuid() throws Exception {
        assertThat(_converter.convertToPresentation(IDENTIFIER, String.class, GERMAN), equalTo(PATH));
        assertThat(_converter.convertToPresentation(IDENTIFIER_NOT_FOUND, String.class, GERMAN), equalTo(IDENTIFIER_NOT_FOUND));
    }

    @Test
    public void testConvertToPresentationAnchor() throws Exception {
        assertThat(_converter.convertToPresentation(ANCHOR, String.class, GERMAN), equalTo(ANCHOR));
    }

    @Test
    public void testConvertToPresentationExternalUrl() throws Exception {
        assertThat(_converter.convertToPresentation(EXTERNAL_URL, String.class, GERMAN), equalTo(EXTERNAL_URL));
    }
}
