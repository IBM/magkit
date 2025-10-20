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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Workspace;
import java.util.UUID;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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

    @Before
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
        _converter.convertToModel(null, null).ifOk(value -> assertThat(value, nullValue()));
    }

    @Test
    public void testConvertToModelPath() {
        _converter.convertToModel(PATH, null).ifOk(value -> assertThat(value, equalTo(IDENTIFIER)));
        assertThat(_converter.convertToModel(PATH_NOT_FOUND, null).isError(), is(true));
    }

    @Test
    public void testConvertToModelAnchor() {
        _converter.convertToModel(ANCHOR, null).ifOk(value -> assertThat(value, equalTo(ANCHOR)));
    }

    @Test
    public void testConvertToModelExternalUrl() {
        _converter.convertToModel(EXTERNAL_URL, null).ifOk(value -> assertThat(value, equalTo(EXTERNAL_URL)));
    }

    @Test
    public void testConvertToPresentationNull() {
        assertThat(_converter.convertToPresentation(null, null), nullValue());
    }

    @Test
    public void testConvertToPresentationUuid() {
        assertThat(_converter.convertToPresentation(IDENTIFIER, null), equalTo(PATH));
        assertThat(_converter.convertToPresentation(IDENTIFIER_NOT_FOUND, null), equalTo(IDENTIFIER_NOT_FOUND));
    }

    @Test
    public void testConvertToPresentationAnchor() {
        assertThat(_converter.convertToPresentation(ANCHOR, null), equalTo(ANCHOR));
    }

    @Test
    public void testConvertToPresentationExternalUrl() {
        assertThat(_converter.convertToPresentation(EXTERNAL_URL, null), equalTo(EXTERNAL_URL));
    }

    @After
    public void tearDown() {
        cleanContext();
    }
}
