package de.ibmix.magkit.ui.dialogs.fields;

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

import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.datasource.jcr.JcrSessionWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Workspace;
import java.util.UUID;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExtendedLinkConverter} ensuring correct conversion of paths with suffixes.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 2015-06-04
 */
public class ExtendedLinkConverterTest {

    private static final String IDENTIFIER = UUID.randomUUID().toString();
    private static final String PATH = "/path/to/node";
    private static final String PATH_NOT_FOUND = "/path/not/found";
    private static final String ANCHOR = "#anchor";
    private static final String EXTERNAL_URL = "https://www.aperto.com";
    private static final String IDENTIFIER_NOT_FOUND = UUID.randomUUID().toString();

    private ExtendedLinkConverter _converter;

    @BeforeEach
    public void setUp() throws Exception {
        final JcrDatasource jcrDatasource = mock(JcrDatasource.class);
        final JcrSessionWrapper jcrSessionWrapper = mock(JcrSessionWrapper.class);
        final Workspace workspace = mock(Workspace.class);
        when(workspace.getName()).thenReturn(WEBSITE);
        when(jcrSessionWrapper.getWorkspace()).thenReturn(workspace);
        final Node pageNode = mockPageNode(PATH, stubIdentifier(IDENTIFIER));
        when(jcrSessionWrapper.getNode(PATH)).thenReturn(pageNode);
        when(jcrSessionWrapper.getNode(PATH_NOT_FOUND)).thenThrow(PathNotFoundException.class);
        when(jcrSessionWrapper.getNodeByIdentifier(IDENTIFIER)).thenReturn(pageNode);
        when(jcrSessionWrapper.getNodeByIdentifier(IDENTIFIER_NOT_FOUND)).thenThrow(ItemNotFoundException.class);
        when(jcrDatasource.getJCRSession()).thenReturn(jcrSessionWrapper);
        _converter = new ExtendedLinkConverter(jcrDatasource);
    }

    @Test
    public void testConvertToModelNull() {
        _converter.convertToModel(null, null).ifOk(Assertions::assertNull);
    }

    @Test
    public void testConvertToModelPath() {
        _converter.convertToModel(PATH, null).ifOk(value -> assertEquals(IDENTIFIER, value));
        assertTrue(_converter.convertToModel(PATH_NOT_FOUND, null).isError());
    }

    @Test
    public void testConvertToModelAnchor() {
        _converter.convertToModel(ANCHOR, null).ifOk(value -> assertEquals(ANCHOR, value));
    }

    @Test
    public void testConvertToModelExternalUrl() {
        _converter.convertToModel(EXTERNAL_URL, null).ifOk(value -> assertEquals(EXTERNAL_URL, value));
    }

    @Test
    public void testConvertToPresentationNull() {
        assertNull(_converter.convertToPresentation(null, null));
    }

    @Test
    public void testConvertToPresentationUuid() {
        assertEquals(PATH, _converter.convertToPresentation(IDENTIFIER, null));
        assertEquals(IDENTIFIER_NOT_FOUND, _converter.convertToPresentation(IDENTIFIER_NOT_FOUND, null));
    }

    @Test
    public void testConvertToPresentationAnchor() {
        assertEquals(ANCHOR, _converter.convertToPresentation(ANCHOR, null));
    }

    @Test
    public void testConvertToPresentationExternalUrl() {
        assertEquals(EXTERNAL_URL, _converter.convertToPresentation(EXTERNAL_URL, null));
    }

    @Test
    public void testConvertToModelPathWithSelector() {
        final String selectorSuffix = ExtendedLinkConverter.TAG_SELECTOR + "detail";
        final String pathWithSelector = PATH + selectorSuffix;
        _converter.convertToModel(pathWithSelector, null).ifOk(value -> assertEquals(IDENTIFIER + selectorSuffix, value));
    }

    @Test
    public void testConvertToModelPathWithQuery() {
        final String querySuffix = "?a=b&c=d";
        final String pathWithQuery = PATH + querySuffix;
        _converter.convertToModel(pathWithQuery, null).ifOk(value -> assertEquals(IDENTIFIER + querySuffix, value));
    }

    @Test
    public void testConvertToModelPathWithSelectorQueryAnchorCompound() {
        final String selectorSuffix = ExtendedLinkConverter.TAG_SELECTOR + "detail";
        final String querySuffix = "?a=b";
        final String anchorSuffix = "#section";
        final String pathWithCompound = PATH + selectorSuffix + querySuffix + anchorSuffix;
        _converter.convertToModel(pathWithCompound, null).ifOk(value -> assertEquals(IDENTIFIER + selectorSuffix + querySuffix + anchorSuffix, value));
    }

    @Test
    public void testConvertToModelNotFoundWithSelector() {
        final String selectorSuffix = ExtendedLinkConverter.TAG_SELECTOR + "x";
        final String pathWithSelectorNotFound = PATH_NOT_FOUND + selectorSuffix;
        assertTrue(_converter.convertToModel(pathWithSelectorNotFound, null).isError());
    }

    @Test
    public void testConvertToPresentationIdentifierWithSelector() {
        final String selectorSuffix = ExtendedLinkConverter.TAG_SELECTOR + "detail";
        final String identifierWithSelector = IDENTIFIER + selectorSuffix;
        assertEquals(PATH + selectorSuffix, _converter.convertToPresentation(identifierWithSelector, null));
    }

    @Test
    public void testConvertToPresentationIdentifierWithQuery() {
        final String querySuffix = "?a=b&c=d";
        final String identifierWithQuery = IDENTIFIER + querySuffix;
        assertEquals(PATH + querySuffix, _converter.convertToPresentation(identifierWithQuery, null));
    }

    @Test
    public void testConvertToPresentationIdentifierWithSelectorQueryAnchorCompound() {
        final String selectorSuffix = ExtendedLinkConverter.TAG_SELECTOR + "detail";
        final String querySuffix = "?a=b";
        final String anchorSuffix = "#section";
        final String identifierCompound = IDENTIFIER + selectorSuffix + querySuffix + anchorSuffix;
        assertEquals(PATH + selectorSuffix + querySuffix + anchorSuffix, _converter.convertToPresentation(identifierCompound, null));
    }

    @Test
    public void testGetNodePartExtraction() {
        final String selectorSuffix = ExtendedLinkConverter.TAG_SELECTOR + "detail";
        final String querySuffix = "?a=b";
        final String anchorSuffix = "#sec";
        final String pathCompound = PATH + selectorSuffix + querySuffix + anchorSuffix;
        assertEquals(PATH, ExtendedLinkConverter.getNodePart(pathCompound));
    }

    @Test
    public void testGetNodePartIdentifierCompound() {
        final String selectorSuffix = ExtendedLinkConverter.TAG_SELECTOR + "detail";
        final String querySuffix = "?a=b";
        final String anchorSuffix = "#sec";
        final String identifierCompound = IDENTIFIER + selectorSuffix + querySuffix + anchorSuffix;
        assertEquals(IDENTIFIER, ExtendedLinkConverter.getNodePart(identifierCompound));
    }

    @Test
    public void testConvertToModelUnsupportedFormat() {
        final String unsupported = "path/to/node?x=y";
        assertTrue(_converter.convertToModel(unsupported, null).isError());
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }
}
